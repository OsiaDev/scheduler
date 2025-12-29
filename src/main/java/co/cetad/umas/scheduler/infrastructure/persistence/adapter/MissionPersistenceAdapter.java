package co.cetad.umas.scheduler.infrastructure.persistence.adapter;

import co.cetad.umas.scheduler.domain.model.entity.MissionEntity;
import co.cetad.umas.scheduler.domain.model.entity.MissionState;
import co.cetad.umas.scheduler.domain.model.vo.Mission;
import co.cetad.umas.scheduler.domain.ports.out.MissionRepository;
import co.cetad.umas.scheduler.infrastructure.persistence.postgresql.repository.R2dbcMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Adaptador de persistencia para misiones
 * Implementa el patrón Repository del puerto de salida
 *
 * CARACTERÍSTICAS:
 * - Operaciones asíncronas con @Async
 * - Transformación entre entidad JPA y VO de dominio
 * - Programación funcional
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MissionPersistenceAdapter implements MissionRepository {

    private final R2dbcMissionRepository repository;

    @Override
    @Async
    public CompletableFuture<List<Mission>> findAutoByState(MissionState state) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Finding missions with state: {}", state);
            return repository.findAll().stream()
                    .filter(entity -> entity.getState() == state)
                    .map(this::toDomain)
                    .toList();
        });
    }

    @Override
    @Async
    public CompletableFuture<List<Mission>> findByStateAndIsAutomaticAndEstimatedDateBefore(
            MissionState state,
            Boolean isAutomatic,
            LocalDateTime estimatedDateBefore
    ) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Finding automatic missions with state: {}, before: {}", state, estimatedDateBefore);

            return repository.findByStateAndIsAutomaticAndEstimatedDateBefore(
                            state,
                            isAutomatic,
                            estimatedDateBefore
                    ).stream()
                    .map(this::toDomain)
                    .toList();
        });
    }

    @Override
    @Async
    public CompletableFuture<List<Mission>> findByStateAndIsAutomaticAndEstimatedDateBetween(
            MissionState state,
            Boolean isAutomatic,
            LocalDateTime estimatedDateAfter,
            LocalDateTime estimatedDateBefore
    ) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Finding automatic missions between {} and {}", estimatedDateAfter, estimatedDateBefore);

            return repository.findByStateAndIsAutomaticAndEstimatedDateBetween(
                            state,
                            isAutomatic,
                            estimatedDateAfter,
                            estimatedDateBefore
                    ).stream()
                    .map(this::toDomain)
                    .toList();
        });
    }

    /**
     * Transforma una entidad JPA en VO de dominio
     */
    private Mission toDomain(MissionEntity entity) {
        return new Mission(
                entity.getId().toString(),
                entity.getName(),
                entity.getOperatorId().toString(),
                entity.getMissionType(),
                entity.getState(),
                entity.getEstimatedDate(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getIsAutomatic(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isNew()
        );
    }

}