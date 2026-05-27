package com.spaceres;

import com.spaceres.dto.request.ReservationRequest;
import com.spaceres.entity.Space;
import com.spaceres.entity.User;
import com.spaceres.exception.BusinessException;
import com.spaceres.repository.ReservationRepository;
import com.spaceres.repository.SpaceRepository;
import com.spaceres.repository.UserRepository;
import com.spaceres.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시 예약 충돌 방지 테스트
 *
 * Pessimistic Lock이 제대로 동작하는지 검증:
 * - 10명이 동시에 같은 공간/시간을 예약 시도
 * - 정확히 1건만 성공해야 함
 * - 나머지 9건은 RESERVATION_CONFLICT 예외 발생
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrentReservationTest {

    @Autowired private ReservationService reservationService;
    @Autowired private SpaceRepository spaceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Space testSpace;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        spaceRepository.deleteAll();

        // 테스트 공간 생성
        testSpace = spaceRepository.save(Space.builder()
                .name("테스트 회의실")
                .capacity(10)
                .location("2층")
                .status(Space.SpaceStatus.AVAILABLE)
                .build());

        // 테스트 사용자 10명 생성
        testUsers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            testUsers.add(userRepository.save(User.builder()
                    .email("user" + i + "@test.com")
                    .password(passwordEncoder.encode("password123"))
                    .name("테스트유저" + i)
                    .role(User.Role.USER)
                    .build()));
        }
    }

    @Test
    @DisplayName("10명 동시 예약 시도 → 정확히 1건만 성공 (Pessimistic Lock)")
    void concurrentReservation_onlyOneSucceeds() throws InterruptedException {
        int threadCount = 10;
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end   = start.plusHours(1);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready  = new CountDownLatch(threadCount); // 모두 준비될 때까지 대기
        CountDownLatch go     = new CountDownLatch(1);           // 동시에 출발
        CountDownLatch done   = new CountDownLatch(threadCount); // 모두 완료될 때까지 대기

        AtomicInteger successCount  = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                ready.countDown();  // 준비 완료 신호
                try {
                    go.await();     // 출발 신호 대기 (동시 시작 보장)

                    ReservationRequest request = new ReservationRequest();
                    setField(request, "spaceId",   testSpace.getId());
                    setField(request, "startTime", start);
                    setField(request, "endTime",   end);
                    setField(request, "purpose",   "동시 테스트 " + idx);

                    reservationService.createReservation(
                            testUsers.get(idx).getEmail(), request);

                    successCount.incrementAndGet();

                } catch (BusinessException e) {
                    if (e.getErrorCode().name().equals("RESERVATION_CONFLICT")) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // 기타 예외
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();  // 10개 스레드 모두 준비될 때까지 대기
        go.countDown(); // 동시 출발!
        done.await(10, TimeUnit.SECONDS); // 최대 10초 대기

        executor.shutdown();

        // ── 검증 ───────────────────────────────────────────
        System.out.println("✅ 성공: " + successCount.get() + "건");
        System.out.println("❌ 충돌: " + conflictCount.get() + "건");

        // 핵심: 정확히 1건만 성공
        assertThat(successCount.get()).isEqualTo(1);
        // DB에도 1건만 저장
        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("다른 시간대 예약은 동시에 모두 성공")
    void differentTimeSlots_allSucceed() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            // 각자 다른 1시간 슬롯
            LocalDateTime start = LocalDateTime.now().plusDays(1)
                    .withHour(9 + idx).withMinute(0);
            LocalDateTime end = start.plusHours(1);

            executor.submit(() -> {
                try {
                    ReservationRequest request = new ReservationRequest();
                    setField(request, "spaceId",   testSpace.getId());
                    setField(request, "startTime", start);
                    setField(request, "endTime",   end);

                    reservationService.createReservation(
                            testUsers.get(idx).getEmail(), request);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        done.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // 다른 시간대이므로 모두 성공
        assertThat(successCount.get()).isEqualTo(threadCount);
    }

    // Reflection으로 DTO 필드 설정 (테스트용)
    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
