package com.spaceres.controller;

import com.spaceres.dto.request.SpaceRequest;
import com.spaceres.dto.response.ApiResponse;
import com.spaceres.dto.response.SpaceResponse;
import com.spaceres.entity.Space;
import com.spaceres.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
@Tag(name = "Space", description = "공간 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class SpaceController {

    private final SpaceService spaceService;

    @Operation(summary = "사용 가능한 공간 목록")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SpaceResponse>>> getAvailable() {
        return ResponseEntity.ok(ApiResponse.ok(spaceService.getAvailableSpaces()));
    }

    @Operation(summary = "공간 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpaceResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(spaceService.getSpace(id)));
    }

    @Operation(summary = "공간 생성 (관리자)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SpaceResponse>> create(
            @Valid @RequestBody SpaceRequest request) {
        SpaceResponse response = spaceService.createSpace(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("공간이 생성되었습니다.", response));
    }

    @Operation(summary = "공간 수정 (관리자)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SpaceResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SpaceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(spaceService.updateSpace(id, request)));
    }

    @Operation(summary = "공간 상태 변경 (관리자)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SpaceResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Space.SpaceStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(spaceService.updateStatus(id, status)));
    }
}
