package co.cetad.umas.scheduler.infrastructure.persistence.postgresql.repository;

import co.cetad.umas.scheduler.domain.model.entity.MissionEntity;
import co.cetad.umas.scheduler.domain.model.entity.MissionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface R2dbcMissionRepository extends JpaRepository<MissionEntity, UUID> {

    /**
     * Busca misiones por estado, tipo automático y fecha estimada antes de un límite
     * Usado para encontrar misiones listas para ejecutar
     */
    @Query("""
            SELECT m FROM MissionEntity m
            WHERE m.state = :state
            AND m.isAutomatic = :isAutomatic
            AND m.estimatedDate <= :estimatedDateBefore
            ORDER BY m.estimatedDate ASC
            """)
    List<MissionEntity> findByStateAndIsAutomaticAndEstimatedDateBefore(
            @Param("state") MissionState state,
            @Param("isAutomatic") Boolean isAutomatic,
            @Param("estimatedDateBefore") LocalDateTime estimatedDateBefore
    );

    /**
     * Busca misiones en un rango de tiempo específico
     * Usado para encontrar misiones que necesitan notificación de preparación
     */
    @Query("""
            SELECT m FROM MissionEntity m
            WHERE m.state = :state
            AND m.isAutomatic = :isAutomatic
            AND m.estimatedDate > :estimatedDateAfter
            AND m.estimatedDate <= :estimatedDateBefore
            ORDER BY m.estimatedDate ASC
            """)
    List<MissionEntity> findByStateAndIsAutomaticAndEstimatedDateBetween(
            @Param("state") MissionState state,
            @Param("isAutomatic") Boolean isAutomatic,
            @Param("estimatedDateAfter") LocalDateTime estimatedDateAfter,
            @Param("estimatedDateBefore") LocalDateTime estimatedDateBefore
    );

}