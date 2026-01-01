package co.cetad.umas.scheduler.application.service;

import co.cetad.umas.scheduler.domain.model.dto.DronPreparationNotificationEvent;
import co.cetad.umas.scheduler.domain.model.entity.DroneEntity;
import co.cetad.umas.scheduler.domain.model.entity.DroneMissionAssignmentEntity;
import co.cetad.umas.scheduler.domain.model.entity.OperatorEntity;
import co.cetad.umas.scheduler.domain.model.vo.Mission;
import co.cetad.umas.scheduler.infrastructure.persistence.postgresql.repository.R2dbcDroneMissionAssignmentRepository;
import co.cetad.umas.scheduler.infrastructure.persistence.postgresql.repository.R2dbcDroneRepository;
import co.cetad.umas.scheduler.infrastructure.persistence.postgresql.repository.R2dbcOperatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de dominio para enriquecer eventos de notificación
 *
 * RESPONSABILIDADES:
 * 1. Obtener información del dron asignado a una misión
 * 2. Obtener información del operador de la misión
 * 3. Construir el evento completo de notificación con todos los datos necesarios
 *
 * FLUJO:
 * Mission → buscar asignación → buscar dron → buscar operador → evento enriquecido
 *
 * VALORES POR DEFECTO:
 * - Si no hay dron asignado: vehicleId="UNKNOWN", vehicleName="Not Assigned"
 * - Si no se encuentra operador: recipientEmail="no-reply@umas.co"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventEnricher {

    private final R2dbcDroneMissionAssignmentRepository assignmentRepository;
    private final R2dbcDroneRepository droneRepository;
    private final R2dbcOperatorRepository operatorRepository;

    /**
     * Enriquece una misión con datos de dron y operador para crear el evento de notificación
     *
     * @param mission Misión a enriquecer
     * @param minutesBeforeExecution Minutos antes de la ejecución
     * @return CompletableFuture con el evento enriquecido
     */
    public CompletableFuture<DronPreparationNotificationEvent> enrichNotificationEvent(
            Mission mission,
            Integer minutesBeforeExecution
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Enriching notification event for mission: {}", mission.id());

                UUID missionId = UUID.fromString(mission.id());
                UUID operatorId = UUID.fromString(mission.operatorId());

                // Obtener asignación de dron
                Optional<DroneMissionAssignmentEntity> assignmentOpt =
                        assignmentRepository.findFirstByMissionId(missionId);

                // Obtener información del dron si está asignado
                String vehicleId = "UNKNOWN";
                String vehicleName = "Not Assigned";

                if (assignmentOpt.isPresent()) {
                    UUID droneId = assignmentOpt.get().getDroneId();
                    Optional<DroneEntity> droneOpt = droneRepository.findById(droneId);

                    if (droneOpt.isPresent()) {
                        DroneEntity drone = droneOpt.get();
                        vehicleId = drone.getVehicleId();
                        vehicleName = drone.getName();
                        log.debug("Found drone assignment - vehicleId: {}, vehicleName: {}",
                                vehicleId, vehicleName);
                    } else {
                        log.warn("Drone not found for mission: {}, droneId: {}", mission.id(), droneId);
                    }
                } else {
                    log.warn("No drone assignment found for mission: {}", mission.id());
                }

                // Obtener email del operador
                String recipientEmail = operatorRepository.findById(operatorId)
                        .map(OperatorEntity::getEmail)
                        .orElseGet(() -> {
                            log.warn("Operator not found for mission: {}, operatorId: {}",
                                    mission.id(), operatorId);
                            return "no-reply@umas.co";
                        });

                log.debug("Enriched notification - vehicleId: {}, vehicleName: {}, recipientEmail: {}",
                        vehicleId, vehicleName, recipientEmail);

                // Construir evento enriquecido
                return DronPreparationNotificationEvent.of(
                        mission.id(),
                        mission.name(),
                        vehicleId,
                        vehicleName,
                        mission.estimatedDate(),
                        minutesBeforeExecution,
                        recipientEmail
                );

            } catch (Exception e) {
                log.error("Error enriching notification event for mission: {}", mission.id(), e);
                throw new RuntimeException("Failed to enrich notification event", e);
            }
        });
    }

}