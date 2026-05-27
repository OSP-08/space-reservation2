package com.spaceres.repository;

import com.spaceres.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // ── 핵심: 시간 중복 예약 검사 쿼리 ───────────────────
    //
    // 겹치는 조건 (새 예약의 시작/종료가 기존 예약과 겹치는 경우):
    //
    //   기존: |---------|
    //   케이스1:    |---------|   (새 예약 시작이 기존 중간)
    //   케이스2: |------|         (새 예약 종료가 기존 중간)
    //   케이스3:  |-----|         (새 예약이 기존 안에 포함)
    //   케이스4: |------------|   (새 예약이 기존을 포함)
    //
    // → 위 4가지를 한 줄로: start < endTime AND end > startTime
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.space.id = :spaceId
              AND r.status = 'CONFIRMED'
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    List<Reservation> findOverlappingReservations(
            @Param("spaceId") Long spaceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 특정 예약 제외한 중복 검사 (수정 시 사용)
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.space.id = :spaceId
              AND r.id <> :excludeId
              AND r.status = 'CONFIRMED'
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    List<Reservation> findOverlappingExcluding(
            @Param("spaceId") Long spaceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId
    );

    // 사용자의 예약 목록
    List<Reservation> findByUserIdOrderByStartTimeDesc(Long userId);

    // 공간의 예약 목록 (확정된 것만)
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.space.id = :spaceId
              AND r.status = 'CONFIRMED'
              AND r.endTime > :from
            ORDER BY r.startTime ASC
            """)
    List<Reservation> findUpcomingBySpaceId(
            @Param("spaceId") Long spaceId,
            @Param("from") LocalDateTime from
    );
}
