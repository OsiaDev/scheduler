package co.cetad.umas.scheduler.infrastructure.persistence.postgresql.repository;

import co.cetad.umas.scheduler.domain.model.entity.DroneMissionAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface R2dbcDroneMissionAssignmentRepository extends JpaRepository<DroneMissionAssignmentEntity, UUID> {

    /**
     * Busca las asignaciones de drones para una misión específica
     */
    @Query("SELECT dma FROM DroneMissionAssignmentEntity dma WHERE dma.missionId = :missionId")
    List<DroneMissionAssignmentEntity> findByMissionId(@Param("missionId") UUID missionId);

    /**
     * Busca la primera asignación de dron para una misión
     * Útil para obtener el dron principal de una misión
     */
    @Query("SELECT dma FROM DroneMissionAssignmentEntity dma WHERE dma.missionId = :missionId ORDER BY dma.assignedAt ASC")
    Optional<DroneMissionAssignmentEntity> findFirstByMissionId(@Param("missionId") UUID missionId);

}