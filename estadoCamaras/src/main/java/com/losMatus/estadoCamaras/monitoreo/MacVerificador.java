package com.losMatus.estadoCamaras.monitoreo;

import com.losMatus.estadoCamaras.entity.Camara;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MacVerificador implements Verificador{

    private final MacResolver macResolver;

    public MacVerificador(MacResolver macResolver) {
        this.macResolver = macResolver;
    }

    @Override
    public ResultadoCheck verificar(Camara camara) {

        Optional<String> detectada = macResolver.resolver(camara.getIp());

        if (detectada.isEmpty()) {
            return ResultadoCheck.fallo("No se pudo obtener la MAC de " + camara.getIp());
        }

        String macDetectada = detectada.get();
        String macEsperada = normalizar(camara.getMacEsperada());

        if (macEsperada == null) {
            return new ResultadoCheck(true, macDetectada,
                    "MAC detectada (la cámara no tiene MAC esperada registrada)");
        }

        if (macEsperada.equals(macDetectada)) {
            return new ResultadoCheck(true, macDetectada, "La MAC coincide");
        }

        return new ResultadoCheck(false, macDetectada,
                "MAC distinta: esperada " + macEsperada + ", detectada " + macDetectada);
    }

    // La esperada ya se guarda normalizada en el service, esto es solo por seguridad
    private String normalizar(String mac) {
        return (mac == null || mac.isBlank()) ? null : MacResolver.normalizar(mac.trim());
    }
}
