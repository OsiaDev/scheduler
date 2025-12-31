package co.cetad.umas.scheduler.infrastructure.messaging.kafka.producer;

import co.cetad.umas.scheduler.domain.model.dto.DronPreparationMessage;
import co.cetad.umas.scheduler.domain.model.dto.DronPreparationNotificationEvent;
import co.cetad.umas.scheduler.domain.model.dto.MissionExecutionMessage;
import co.cetad.umas.scheduler.domain.model.dto.MissionExecutionScheduledEvent;
import co.cetad.umas.scheduler.domain.ports.out.EventPublisher;
import co.cetad.umas.scheduler.infrastructure.messaging.kafka.config.KafkaTopicsProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Adaptador de Kafka para publicación de eventos de misión
 *
 * RESPONSABILIDADES:
 * 1. Transformar eventos de dominio a mensajes Kafka
 * 2. Publicar mensajes en los topics correspondientes
 * 3. Manejo de errores de serialización y publicación
 *
 * CARACTERÍSTICAS:
 * - Operaciones asíncronas
 * - Serialización JSON con Jackson
 * - Manejo funcional de errores
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MissionEventPublisher implements EventPublisher<MissionExecutionScheduledEvent> {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicsProperties topicsProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    public CompletableFuture<Void> publish(MissionExecutionScheduledEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                MissionExecutionMessage message = toExecutionMessage(event);
                String jsonPayload = serializeMessage(message);

                kafkaTemplate.send(
                        topicsProperties.getExecute(),
                        event.missionId(),
                        jsonPayload
                );

                log.info("Published mission execution event for mission: {}", event.missionId());

            } catch (JsonProcessingException e) {
                log.error("Error serializing mission execution event", e);
                throw new RuntimeException("Failed to serialize mission execution event", e);
            } catch (Exception e) {
                log.error("Error publishing mission execution event", e);
                throw new RuntimeException("Failed to publish mission execution event", e);
            }
        });
    }

    /**
     * Transforma evento de dominio a mensaje DTO
     */
    private MissionExecutionMessage toExecutionMessage(MissionExecutionScheduledEvent event) {
        return MissionExecutionMessage.of(
                event.missionId(),
                event.missionName(),
                event.scheduledAt(),
                event.publishedAt()
        );
    }

    /**
     * Transforma evento de dominio a mensaje DTO
     */
    private DronPreparationMessage toPreparationMessage(DronPreparationNotificationEvent event) {
        return DronPreparationMessage.of(
                event.missionId(),
                event.missionName(),
                event.scheduledExecutionTime(),
                event.minutesBeforeExecution(),
                event.publishedAt()
        );
    }

    /**
     * Serializa un mensaje a JSON
     */
    private String serializeMessage(Object message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(message);
    }

}