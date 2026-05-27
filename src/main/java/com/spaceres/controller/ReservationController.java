package com.spaceres.controller;

import com.spaceres.dto.request.ReservationRequest;
import com.spaceres.dto.response.ApiResponse;
import com.spaceres.dto.response.ReservationResponse;
import com.spaceres.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "예약 API")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "예약 생성 (중복 방지 Lock 적용)")
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReservationRequest request) {

        ReservationResponse response = reservationService
                .createReservation(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("예약이 완료되었습니다.", response));
    }

    @Operation(summary = "내 예약 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ReservationResponse> list = reservationService
                .getMyReservations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @Operation(summary = "공간별 예약 현황 조회")
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getBySpace(
            @PathVariable Long spaceId) {

        List<ReservationResponse> list = reservationService.getUpcomingBySpace(spaceId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @Operation(summary = "예약 취소")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        reservationService.cancelReservation(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("예약이 취소되었습니다.", null));
    }
}
