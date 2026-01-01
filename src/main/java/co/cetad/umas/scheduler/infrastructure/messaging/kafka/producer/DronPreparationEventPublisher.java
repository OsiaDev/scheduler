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
 * 1. Transformar eventos de dominio a mensajes DTO para Kafka
 * 2. Serializar mensajes a JSON
 * 3. Publicar en el topic umas.dron.preparation.notification
 * 4. Manejo de errores de serialización y publicación
 *
 * IMPORTANTE:
 * - Usa @Qualifier("dronPreparationEventPublisher") para inyección
 * - Espera acknowledgment de Kafka con .get()
 * - Logging detallado para debugging
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
                // Transformar evento a mensaje DTO
                DronPreparationMessage message = toPreparationMessage(event);

                // Serializar a JSON
                String jsonPayload = serializeMessage(message);

                // Publicar en Kafka
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
     * Transforma evento de dominio a mensaje DTO para Kafka
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