package com.spaceres.controller;

import com.google.api.services.drive.model.File;
import com.spaceres.dto.response.ApiResponse;
import com.spaceres.service.GoogleDriveBackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Backup", description = "구글 드라이브 백업 관리 API (관리자 전용)")
@SecurityRequirement(name = "bearerAuth")
public class BackupController {

    private final GoogleDriveBackupService backupService;

    @Operation(summary = "수동 백업 실행")
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<String>> runBackup() throws IOException {
        String fileId = backupService.backupReservationLogs();
        return ResponseEntity.ok(ApiResponse.ok("백업이 완료되었습니다.", fileId));
    }

    @Operation(summary = "백업 파일 목록 조회")
    @GetMapping("/files")
    public ResponseEntity<ApiResponse<List<File>>> listFiles() throws IOException {
        List<File> files = backupService.listBackupFiles();
        return ResponseEntity.ok(ApiResponse.ok(files));
    }

    @Operation(summary = "오래된 백업 파일 정리")
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<Void>> cleanup() throws IOException {
        backupService.cleanUpOldBackups();
        return ResponseEntity.ok(ApiResponse.ok("정리가 완료되었습니다.", null));
    }

    @Operation(summary = "특정 백업 파일 삭제")
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable String fileId) throws IOException {
        backupService.deleteBackupFile(fileId);
        return ResponseEntity.ok(ApiResponse.ok("삭제 완료", null));
    }
}
