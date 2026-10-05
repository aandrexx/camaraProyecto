package com.losMatus.estadoCamaras;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.service.MonitoreoService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MonitoreoServiceTest{
    private final MonitoreoService monitoreoService = new MonitoreoService(null, null);
    // null porque este test no necesita los repositorios, solo probamos un método puro

    @Test
    void deberiaCoincidirCuandoMacsSonIguales() {
        Camara camara = new Camara();
        camara.setMacEsperada("AA:BB:CC:11:22:33");
        camara.setUltimaMacDetectada("AA:BB:CC:11:22:33");

        assertTrue(monitoreoService.calcularMacCoincide(camara));
    }

    @Test
    void noDeberiaCoincidirCuandoMacsSonDistintas() {
        Camara camara = new Camara();
        camara.setMacEsperada("AA:BB:CC:11:22:33");
        camara.setUltimaMacDetectada("FF:EE:DD:99:88:77");

        assertFalse(monitoreoService.calcularMacCoincide(camara));
    }

    @Test
    void deberiaConsiderarCoincidenciaCuandoFaltanDatos() {
        Camara camara = new Camara();
        camara.setMacEsperada(null);
        camara.setUltimaMacDetectada(null);

        assertTrue(monitoreoService.calcularMacCoincide(camara));
    }

    @Test
    void noDeberiaImportarMayusculasOMinusculas() {
        Camara camara = new Camara();
        camara.setMacEsperada("aa:bb:cc:11:22:33");
        camara.setUltimaMacDetectada("AA:BB:CC:11:22:33");

        assertTrue(monitoreoService.calcularMacCoincide(camara));
    }
}
