package co.cetad.umas.scheduler.domain.ports.in;

import java.util.concurrent.CompletableFuture;

/**
 * Puerto de entrada para el scheduling de misiones automáticas
 * Define las operaciones para ejecutar misiones programadas
 */
public interface MissionSchedulerUseCase {

    /**
     * Ejecuta el proceso de scheduling de misiones automáticas
     * - Busca misiones aprobadas y automáticas cuya hora de ejecución ha llegado
     * - Publica eventos para iniciar su ejecución
     * - Evita ejecuciones concurrentes mediante lock
     *
     * @return CompletableFuture con el número de misiones programadas
     */
    CompletableFuture<Integer> scheduleReadyMissions();

    /**
     * Ejecuta el proceso de notificación de preparación de drones
     * - Busca misiones que se ejecutarán en X minutos
     * - Publica eventos para notificar vía SMTP
     *
     * @return CompletableFuture con el número de notificaciones enviadas
     */
    CompletableFuture<Integer> notifyUpcomingMissions();

}