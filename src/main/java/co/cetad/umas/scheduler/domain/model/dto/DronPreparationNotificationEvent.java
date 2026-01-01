package co.cetad.umas.scheduler.domain.model.dto;

import java.time.LocalDateTime;

/**
 * Evento de dominio para notificar la preparación de dron vía SMTP
 * Este evento se publica X minutos antes de la ejecución de la misión
 *
 * REFACTORIZACIÓN: Ahora incluye información completa del dron y operador
 */
public record DronPreparationNotificationEvent(
        String missionId,
        String missionName,
        String vehicleId,
        String vehicleName,
        LocalDateTime scheduledExecutionTime,
        Integer minutesBeforeExecution,
        LocalDateTime publishedAt,
        String recipientEmail
) {
    public DronPreparationNotificationEvent {
        if (missionId == null || missionId.isBlank()) {
            throw new IllegalArgumentException("Mission ID cannot be null or empty");
        }
        if (scheduledExecutionTime == null) {
            throw new IllegalArgumentException("Scheduled execution time cannot be null");
        }
        if (minutesBeforeExecution == null || minutesBeforeExecution <= 0) {
            throw new IllegalArgumentException("Minutes before execution must be positive");
        }
        if (publishedAt == null) {
            throw new IllegalArgumentException("Published at cannot be null");
        }
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }
    }

    public static DronPreparationNotificationEvent of(
            String missionId,
            String missionName,
            String vehicleId,
            String vehicleName,
            LocalDateTime scheduledExecutionTime,
            Integer minutesBeforeExecution,
            String recipientEmail
    ) {
        return new DronPreparationNotificationEvent(
                missionId,
                missionName != null ? missionName : "Scheduled Mission",
                vehicleId,
                vehicleName,
                scheduledExecutionTime,
                minutesBeforeExecution,
                LocalDateTime.now(),
                recipientEmail
        );
    }

}