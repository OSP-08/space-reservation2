package com.spaceres.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 예약(Reservation) 엔티티
 *
 * Pessimistic Lock 대상 테이블
 * → 동시에 같은 공간/시간대 예약 시 충돌 방지
 */
@Entity
@Table(
    name = "reservations",
    indexes = {
        // 중복 예약 검사 쿼리 성능 최적화
        @Index(name = "idx_reservation_space_time",
               columnList = "space_id, start_time, end_time"),
        @Index(name = "idx_reservation_user",
               columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 예약 공간
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // 예약 목적/메모
    @Column(length = 300)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum ReservationStatus {
        CONFIRMED,   // 예약 확정
        CANCELLED    // 취소
    }
}
