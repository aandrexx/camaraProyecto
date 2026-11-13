package com.losMatus.estadoCamaras.notificador;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.enums.EstadoCamara;

public interface Notificador {

    void notificar(Camara camara, EstadoCamara anterior, EstadoCamara nuevo, String motivo);
}
