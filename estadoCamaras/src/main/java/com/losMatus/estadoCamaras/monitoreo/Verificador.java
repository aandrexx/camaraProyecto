package com.losMatus.estadoCamaras.monitoreo;

import com.losMatus.estadoCamaras.entity.Camara;

public interface Verificador {

    ResultadoCheck verificar(Camara camara);
}
