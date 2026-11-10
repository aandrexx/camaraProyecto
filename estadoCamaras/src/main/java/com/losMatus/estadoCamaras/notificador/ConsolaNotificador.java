package com.losMatus.estadoCamaras.notificador;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsolaNotificador implements Notificador{
    @Override
    public void notificar(Camara camara, EstadoCamara anterior, EstadoCamara nuevo) {
        log.warn("NOTIFICACIÓN | Cámara '{}' ({}, {}): {} -> {}",
                camara.getNombre(), camara.getIp(), camara.getUbicacion(), anterior, nuevo);
    }
}
