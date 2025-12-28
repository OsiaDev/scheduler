package co.cetad.umas.scheduler.infrastructure.persistence.adapter;

import co.cetad.umas.scheduler.domain.model.entity.MissionState;
import co.cetad.umas.scheduler.domain.model.vo.Mission;
import co.cetad.umas.scheduler.domain.ports.out.MissionRepository;
import co.cetad.umas.scheduler.infrastructure.persistence.postgresql.repository.R2dbcMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionPersistenceAdapter implements MissionRepository {

    private final R2dbcMissionRepository repository;

    @Override
    public CompletableFuture<List<Mission>> findAutoByState(MissionState state) {
        return null;
    }

}
