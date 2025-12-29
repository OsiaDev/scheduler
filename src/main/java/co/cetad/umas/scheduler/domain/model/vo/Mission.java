package co.cetad.umas.scheduler.domain.model.vo;

import co.cetad.umas.scheduler.domain.model.entity.MissionOrigin;
import co.cetad.umas.scheduler.domain.model.entity.MissionState;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa una MISIÓN general
 *
 * REFACTORIZACIÓN: Ahora una misión es independiente de los drones
 * Los drones se asignan a través de DroneMissionAssignment
 *
 * FECHAS:
 * - estimatedDate: Fecha estimada de ejecución (NOT NULL - siempre existe)
 * - startDate: Fecha real de inicio (NULL hasta que inicie)
 * - endDate: Fecha real de finalización (NULL hasta que termine)
 *
 * EJECUCIÓN AUTOMÁTICA:
 * - isAutomatic: Determina si la misión se ejecuta automáticamente al llegar la hora
 */
public record Mission(
        String id,
        String name,
        String operatorId,
        MissionOrigin missionType,
        MissionState state,
        LocalDateTime estimatedDate,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean isAutomatic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean isNew
) {

    /**
     * Constructor compacto con validaciones
     */
    public Mission {
        Objects.requireNonNull(id, "Mission ID cannot be null");
        Objects.requireNonNull(operatorId, "Operator ID cannot be null");
        Objects.requireNonNull(missionType, "Mission type cannot be null");
        Objects.requireNonNull(state, "Mission state cannot be null");
        Objects.requireNonNull(estimatedDate, "Estimated date cannot be null");
        Objects.requireNonNull(isAutomatic, "isAutomatic cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");
        Objects.requireNonNull(updatedAt, "Updated at cannot be null");

        if (id.isBlank()) {
            throw new IllegalArgumentException("Mission ID cannot be empty");
        }
        if (operatorId.isBlank()) {
            throw new IllegalArgumentException("Operator ID cannot be empty");
        }

        // Validar fechas
        if (startDate != null && startDate.isBefore(estimatedDate)) {
            throw new IllegalArgumentException("Start date cannot be before estimated date");
        }
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    /**
     * Factory method para crear una nueva misión MANUAL
     */
    public static Mission createManual(
            String name,
            String operatorId,
            LocalDateTime estimatedDate,
            Boolean isAutomatic
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Mission(
                UUID.randomUUID().toString(),
                name,
                operatorId,
                MissionOrigin.MANUAL,
                MissionState.PENDIENTE_APROBACION,
                estimatedDate,
                null,
                null,
                isAutomatic,
                now,
                now,
                true
        );
    }

    /**
     * Factory method para crear una nueva misión AUTOMATICA
     */
    public static Mission createAutomatic(
            String operatorId,
            LocalDateTime estimatedDate
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Mission(
                UUID.randomUUID().toString(),
                "Misión Automática",
                operatorId,
                MissionOrigin.AUTOMATICA,
                MissionState.EN_EJECUCION,
                estimatedDate,
                now,
                null,
                true,
                now,
                now,
                true
        );
    }

    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean isScheduledForFuture() {
        return estimatedDate.isAfter(LocalDateTime.now());
    }

    public boolean shouldHaveStarted() {
        return !estimatedDate.isAfter(LocalDateTime.now());
    }

    public boolean isManual() {
        return missionType == MissionOrigin.MANUAL;
    }

    public boolean isAutomaticType() {
        return missionType == MissionOrigin.AUTOMATICA;
    }

    public boolean isPendingApproval() {
        return state == MissionState.PENDIENTE_APROBACION;
    }

    public boolean isApproved() {
        return state == MissionState.APROBADA;
    }

    public boolean isInProgress() {
        return state == MissionState.EN_EJECUCION;
    }

    public boolean hasStarted() {
        return startDate != null;
    }

    public boolean hasEnded() {
        return endDate != null;
    }

    public boolean isFinished() {
        return state == MissionState.FINALIZADA ||
                state == MissionState.ABORTADA ||
                state == MissionState.FALLIDA ||
                state == MissionState.ARCHIVADA;
    }

    public Mission withState(MissionState newState) {
        Objects.requireNonNull(newState, "New state cannot be null");
        return new Mission(
                id, name, operatorId, missionType, newState,
                estimatedDate, startDate, endDate, isAutomatic,
                createdAt, LocalDateTime.now(), isNew
        );
    }

    public Mission withStarted(LocalDateTime startTime) {
        Objects.requireNonNull(startTime, "Start time cannot be null");
        return new Mission(
                id, name, operatorId, missionType, MissionState.EN_EJECUCION,
                estimatedDate, startTime, endDate, isAutomatic,
                createdAt, LocalDateTime.now(), isNew
        );
    }

    public Mission withEnded(LocalDateTime endTime, MissionState finalState) {
        Objects.requireNonNull(endTime, "End time cannot be null");
        Objects.requireNonNull(finalState, "Final state cannot be null");

        if (!finalState.equals(MissionState.FINALIZADA) &&
                !finalState.equals(MissionState.ABORTADA) &&
                !finalState.equals(MissionState.FALLIDA)) {
            throw new IllegalArgumentException(
                    "Final state must be FINALIZADA, ABORTADA, or FALLIDA");
        }

        return new Mission(
                id, name, operatorId, missionType, finalState,
                estimatedDate, startDate, endTime, isAutomatic,
                createdAt, LocalDateTime.now(), isNew
        );
    }

}