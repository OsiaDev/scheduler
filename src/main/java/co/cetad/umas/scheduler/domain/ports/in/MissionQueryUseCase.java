package co.cetad.umas.scheduler.domain.ports.in;

import co.cetad.umas.scheduler.domain.model.entity.MissionState;
import co.cetad.umas.scheduler.domain.model.vo.Mission;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Puerto de entrada para consultas de misiones (CQRS - Query Side)
 * Define operaciones de solo lectura sin modificar el estado
 *
 * REFACTORIZACIÓN: Ahora trabaja con Mission independiente de drones
 *
 * PATRÓN CQRS - QUERY SIDE:
 * - Retorna Optional<Mission> para permitir que el controller maneje "no encontrado"
 * - NO lanza excepciones de negocio (eso es responsabilidad del Command Side)
 * - Los errores técnicos se propagan como exceptionally() en CompletableFuture
 *
 * CONSISTENCIA:
 * - Mismo patrón que TelemetryQueryUseCase
 * - Delega al MissionRepository que también retorna Optional
 */
public interface MissionQueryUseCase {

    CompletableFuture<List<Mission>> findAutoByState(MissionState state);

}
