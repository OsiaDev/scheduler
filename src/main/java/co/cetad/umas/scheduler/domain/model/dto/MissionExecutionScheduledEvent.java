package co.cetad.umas.scheduler.domain.model.dto;

import java.time.LocalDateTime;

/**
 * Evento de dominio que representa la necesidad de ejecutar una misión
 * Este evento se publica cuando llega la hora de ejecutar una misión automática
 */
public record MissionExecutionScheduledEvent(
        String missionId,
        String missionName,
        LocalDateTime scheduledAt,
        LocalDateTime publishedAt
) {
    public MissionExecutionScheduledEvent {
        if (missionId == null || missionId.isBlank()) {
            throw new IllegalArgumentException("Mission ID cannot be null or empty");
        }
        if (scheduledAt == null) {
            throw new IllegalArgumentException("Scheduled at cannot be null");
        }
        if (publishedAt == null) {
            throw new IllegalArgumentException("Published at cannot be null");
        }
    }

    public static MissionExecutionScheduledEvent of(String missionId, String missionName, LocalDateTime scheduledAt) {
        return new MissionExecutionScheduledEvent(
                missionId,
                missionName != null ? missionName : "Scheduled Mission",
                scheduledAt,
                LocalDateTime.now()
        );
    }

}