package com.losMatus.estadoCamaras.monitoreo;

public record ResultadoCheck(
        boolean exitoso, String valor, String detalle
) {

    public static ResultadoCheck ok(String detalle) {
        return new ResultadoCheck(true, null, detalle);
    }

    public static ResultadoCheck fallo(String detalle) {
        return new ResultadoCheck(false, null, detalle);
    }
}
