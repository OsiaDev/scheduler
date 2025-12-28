package co.cetad.umas.scheduler.domain.ports.out;

import co.cetad.umas.scheduler.domain.model.entity.MissionState;
import co.cetad.umas.scheduler.domain.model.vo.Mission;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Puerto de salida para persistencia de misiones
 *
 * REFACTORIZACIÓN: Ahora trabaja con Mission independiente de drones
 */
public interface MissionRepository {

    CompletableFuture<List<Mission>> findAutoByState(MissionState state);

}
