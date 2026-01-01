package co.cetad.umas.scheduler.domain.model.entity;

import lombok.Getter;

@Getter
public enum DroneStatus {

    ACTIVE("Activo"),
    IN_MAINTENANCE("En mantenimiento"),
    REPAIRING("En reparación"),
    OUT_OF_SERVICE("Fuera de servicio"),
    DECOMMISSIONED("Retirado");

    private final String status;

    DroneStatus(String s) {
        this.status = s;
    }

}
