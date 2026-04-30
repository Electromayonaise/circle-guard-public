package com.circleguard.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.util.concurrent.CompletableFuture;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.kafka.listener.auto-startup=false")
public class LmsServiceTest {

    @Autowired
    private LmsService lmsService;

    @Test
    void testRemoteAttendanceSync() {
        CompletableFuture<Void> future = lmsService.syncRemoteAttendance("student-123", "PROBABLE");
        future.join(); // Wait for completion
        assertThat(future).isCompleted();
    }
}
