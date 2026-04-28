package com.circleguard.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "spring.main.allow-bean-definition-overriding=true"
})
@EmbeddedKafka(partitions = 1, topics = {"promotion.status.changed"})
@DirtiesContext
class StatusChangeNotificationIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private NotificationDispatcher dispatcher;

    @MockBean
    private LmsService lmsService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private SmsService smsService;

    @MockBean
    private PushService pushService;

    @MockBean
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @MockBean
    private org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder;

    @Test
    void shouldDispatchNotificationWhenSuspectStatusChangeReceived() throws Exception {
        String event = "{\"anonymousId\": \"status-test-user\", \"status\": \"SUSPECT\"}";

        kafkaTemplate.send("promotion.status.changed", event);

        TimeUnit.SECONDS.sleep(2);

        verify(dispatcher, timeout(3000)).dispatch("status-test-user", "SUSPECT");
    }

    @Test
    void shouldNotDispatchNotificationWhenActiveStatusChangeReceived() throws Exception {
        String event = "{\"anonymousId\": \"active-user\", \"status\": \"ACTIVE\"}";

        kafkaTemplate.send("promotion.status.changed", event);

        TimeUnit.SECONDS.sleep(2);

        verify(dispatcher, never()).dispatch(anyString(), anyString());
    }
}
