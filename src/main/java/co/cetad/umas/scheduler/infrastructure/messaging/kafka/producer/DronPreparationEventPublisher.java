package co.cetad.umas.scheduler.infrastructure.messaging.kafka.producer;

import co.cetad.umas.scheduler.domain.model.dto.DronPreparationNotificationEvent;
import co.cetad.umas.scheduler.domain.ports.out.EventPublisher;
import co.cetad.umas.scheduler.domain.model.dto.DronPreparationMessage;
import co.cetad.umas.scheduler.infrastructure.messaging.kafka.config.KafkaTopicsProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Adaptador de infraestructura para publicación de eventos de preparación de dron en Kafka
 *
 * IMPLEMENTA: EventPublisher<DronPreparationNotificationEvent>
 *
 * RESPONSABILIDADES:
 * 1. Publicar eventos de notificación de preparación en topic de notificaciones
 * 2. Transformar eventos de dominio a mensajes DTO
 * 3. Serializar mensajes a JSON
 * 4. Manejo de errores de publicación
 *
 * CARACTERÍSTICAS:
 * - Operaciones asíncronas con @Async
 * - Serialización JSON con Jackson
 * - Logging detallado de eventos
 * - Manejo funcional de errores con CompletableFuture
 *
 * REFACTORIZACIÓN:
 * - Ahora incluye vehicleId, vehicleName y recipientEmail en el mensaje
 * - Sigue el mismo patrón que MissionEventPublisher
 */
@Slf4j
@Component("dronPreparationEventPublisher")
@RequiredArgsConstructor
public class DronPreparationEventPublisher implements EventPublisher<DronPreparationNotificationEvent> {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicsProperties topicsProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    public CompletableFuture<Void> publish(DronPreparationNotificationEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                DronPreparationMessage message = toPreparationMessage(event);
                String jsonPayload = serializeMessage(message);

                kafkaTemplate.send(
                        topicsProperties.getNotification(),
                        event.missionId(),
                        jsonPayload
                ).get(); // Wait for acknowledgment

                log.info("✅ Published dron preparation notification - Mission: {}, Vehicle: {} ({}), " +
                                "Scheduled: {}, Minutes before: {}, Recipient: {}, Topic: {}",
                        event.missionId(),
                        event.vehicleName(),
                        event.vehicleId(),
                        event.scheduledExecutionTime(),
                        event.minutesBeforeExecution(),
                        event.recipientEmail(),
                        topicsProperties.getNotification());

            } catch (JsonProcessingException e) {
                log.error("❌ Error serializing preparation notification for mission: {}",
                        event.missionId(), e);
                throw new RuntimeException("Failed to serialize preparation notification", e);
            } catch (Exception e) {
                log.error("❌ Error publishing preparation notification for mission: {}",
                        event.missionId(), e);
                throw new RuntimeException("Failed to publish preparation notification", e);
            }
        });
    }

    /**
     * Transforma evento de dominio a mensaje DTO para preparación
     * Incluye todos los campos necesarios: vehicleId, vehicleName, recipientEmail
     */
    private DronPreparationMessage toPreparationMessage(DronPreparationNotificationEvent event) {
        return DronPreparationMessage.of(
                event.missionId(),
                event.missionName(),
                event.vehicleId(),
                event.vehicleName(),
                event.scheduledExecutionTime(),
                event.minutesBeforeExecution(),
                event.publishedAt(),
                event.recipientEmail()
        );
    }

    /**
     * Serializa un mensaje a JSON
     */
    private String serializeMessage(Object message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(message);
    }

}