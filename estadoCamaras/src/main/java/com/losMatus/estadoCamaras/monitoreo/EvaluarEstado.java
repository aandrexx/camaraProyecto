package com.losMatus.estadoCamaras.monitoreo;


import com.losMatus.estadoCamaras.enums.EstadoCamara;
import org.springframework.stereotype.Component;

@Component
public class EvaluarEstado {

    /**
     * Cada parámetro puede ser null si ese método no se ejecutó
     * (por ejemplo, la cámara no tiene puerto, o la MAC no se consultó).
     */
    public EstadoCamara evaluar(ResultadoCheck ping, ResultadoCheck tcp, ResultadoCheck mac) {
        boolean pingOk = ping != null && ping.exitoso();
        boolean tcpOk = tcp != null && tcp.exitoso();

        // Ni ping ni puerto responden: caída
        if (!pingOk && !tcpOk) {
            return EstadoCamara.INACTIVA;
        }

        // Responde, pero la MAC detectada es distinta a la esperada
        if (mac != null && !mac.exitoso() && mac.valor() != null) {
            return EstadoCamara.ALERTA;
        }

        // El equipo responde al ping pero el servicio (puerto) no
        if (pingOk && tcp != null && !tcpOk) {
            return EstadoCamara.DEGRADADA;
        }

        return EstadoCamara.ACTIVA;
    }
}
