package com.spaceres.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReservationRequest {

    @NotNull(message = "공간 ID를 입력해주세요")
    private Long spaceId;

    @NotNull(message = "시작 시간을 입력해주세요")
    @Future(message = "시작 시간은 현재 이후여야 합니다")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간을 입력해주세요")
    @Future(message = "종료 시간은 현재 이후여야 합니다")
    private LocalDateTime endTime;

    @Size(max = 300)
    private String purpose;
}
