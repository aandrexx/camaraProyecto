package com.losMatus.estadoCamaras.notificador;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "notificaciones.canal", havingValue = "consola", matchIfMissing = true)
public class ConsolaNotificador implements Notificador{
    @Override
    public void notificar(Camara camara, EstadoCamara anterior, EstadoCamara nuevo, String motivo) {
        log.warn("NOTIFICACIÓN | Cámara '{}' ({}, {}): {} -> {} | Motivo: {}",
                camara.getNombre(), camara.getIp(), camara.getUbicacion(), anterior, nuevo, motivo);
    }
}
