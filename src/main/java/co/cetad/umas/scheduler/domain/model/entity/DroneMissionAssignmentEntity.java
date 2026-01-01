package co.cetad.umas.scheduler.domain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla DRONE_MISSION_ASSIGNMENT
 *
 * Representa la asignación de drones a misiones
 * Relación muchos a muchos entre DRONE y MISSION
 */
@Getter
@Setter
@Entity
@Table(name = "drone_mission_assignment")
public class DroneMissionAssignmentEntity implements Serializable, Persistable<UUID> {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "drone_id", nullable = false)
    private UUID droneId;

    @Column(name = "mission_id", nullable = false)
    private UUID missionId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    private boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew;
    }

}