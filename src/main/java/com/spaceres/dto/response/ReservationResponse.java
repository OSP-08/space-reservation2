package com.spaceres.dto.response;

import com.spaceres.entity.Reservation;
import com.spaceres.entity.Space;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// ── 예약 응답 ──────────────────────────────────────────────
@Getter
@Builder
public class ReservationResponse {
    private Long id;
    private Long spaceId;
    private String spaceName;
    private String spaceLocation;
    private String userName;
    private String userEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private String status;
    private LocalDateTime createdAt;

    public static ReservationResponse from(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .spaceId(r.getSpace().getId())
                .spaceName(r.getSpace().getName())
                .spaceLocation(r.getSpace().getLocation())
                .userName(r.getUser().getName())
                .userEmail(r.getUser().getEmail())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .purpose(r.getPurpose())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
