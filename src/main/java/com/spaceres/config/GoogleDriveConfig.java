package com.spaceres.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Google Drive API 클라이언트 설정
 *
 * Service Account 방식 사용 (서버 간 인증)
 * credentials JSON 파일은 환경변수 또는 classpath에서 읽음
 */
@Slf4j
@Configuration
public class GoogleDriveConfig {

    @Value("${google.drive.credentials-file}")
    private Resource credentialsFile;

    @Value("${google.drive.application-name}")
    private String applicationName;

    @Bean
    public Drive googleDriveService() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsFile.getInputStream())
                .createScoped(List.of(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(applicationName).build();
    }
}
