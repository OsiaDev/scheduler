package co.cetad.umas.scheduler.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO para mensaje de preparación de dron en Kafka
 * Este mensaje será consumido por el servicio de notificaciones SMTP
 *
 * REFACTORIZACIÓN: Ahora incluye vehicleId, vehicleName y recipientEmail
 */
public record DronPreparationMessage(
        @JsonProperty("mission_id") String missionId,
        @JsonProperty("mission_name") String missionName,
        @JsonProperty("vehicleId") String vehicleId,
        @JsonProperty("vehicleName") String vehicleName,
        @JsonProperty("scheduled_execution_time") LocalDateTime scheduledExecutionTime,
        @JsonProperty("minutes_before_execution") Integer minutesBeforeExecution,
        @JsonProperty("published_at") LocalDateTime publishedAt,
        @JsonProperty("recipient_email") String recipientEmail
) {

    public static DronPreparationMessage of(
            String missionId,
            String missionName,
            String vehicleId,
            String vehicleName,
            LocalDateTime scheduledExecutionTime,
            Integer minutesBeforeExecution,
            LocalDateTime publishedAt,
            String recipientEmail
    ) {
        return new DronPreparationMessage(
                missionId,
                missionName != null ? missionName : "Scheduled Mission",
                vehicleId,
                vehicleName,
                scheduledExecutionTime,
                minutesBeforeExecution,
                publishedAt,
                recipientEmail
        );
    }

}