package co.cetad.umas.scheduler.infrastructure.messaging.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {

    /**
     * Topic para ejecutar misiones
     */
    private String execute = "umas.mission.execute";

    /**
     * Topic para notificaciones de preparación de dron (SMTP)
     */
    private String notification = "umas.dron.preparation.notification";

}