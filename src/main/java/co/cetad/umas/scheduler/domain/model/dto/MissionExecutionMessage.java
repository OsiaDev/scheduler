package co.cetad.umas.scheduler.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO para mensaje de ejecución de misión en Kafka
 */
public record MissionExecutionMessage(
        @JsonProperty("mission_id") String missionId,
        @JsonProperty("name") String name,
        @JsonProperty("scheduled_at") LocalDateTime scheduledAt,
        @JsonProperty("published_at") LocalDateTime publishedAt
) {

    public static MissionExecutionMessage of(
            String missionId,
            String name,
            LocalDateTime scheduledAt,
            LocalDateTime publishedAt
    ) {
        return new MissionExecutionMessage(
                missionId,
                name != null ? name : "Scheduled Automatic",
                scheduledAt,
                publishedAt
        );
    }

}