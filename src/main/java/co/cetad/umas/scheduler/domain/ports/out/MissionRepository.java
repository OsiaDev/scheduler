package co.cetad.umas.scheduler.domain.ports.out;

import co.cetad.umas.scheduler.domain.model.entity.MissionState;
import co.cetad.umas.scheduler.domain.model.vo.Mission;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Puerto de salida para persistencia de misiones
 *
 * REFACTORIZACIÓN: Ahora trabaja con Mission independiente de drones
 */
public interface MissionRepository {

    CompletableFuture<List<Mission>> findAutoByState(MissionState state);

    /**
     * Busca misiones automáticas aprobadas que deben ejecutarse
     *
     * @param state Estado de la misión (APROBADA)
     * @param isAutomatic Filtro por ejecución automática
     * @param estimatedDateBefore Fecha límite para buscar misiones
     * @return Lista de misiones que cumplen los criterios
     */
    CompletableFuture<List<Mission>> findByStateAndIsAutomaticAndEstimatedDateBefore(
            MissionState state,
            Boolean isAutomatic,
            LocalDateTime estimatedDateBefore
    );

    /**
     * Busca misiones automáticas aprobadas programadas para un rango de tiempo
     * Usado para enviar notificaciones de preparación de dron
     *
     * @param state Estado de la misión
     * @param isAutomatic Filtro por ejecución automática
     * @param estimatedDateAfter Fecha de inicio del rango
     * @param estimatedDateBefore Fecha de fin del rango
     * @return Lista de misiones en el rango especificado
     */
    CompletableFuture<List<Mission>> findByStateAndIsAutomaticAndEstimatedDateBetween(
            MissionState state,
            Boolean isAutomatic,
            LocalDateTime estimatedDateAfter,
            LocalDateTime estimatedDateBefore
    );

}