package com.losMatus.estadoCamaras.monitoreo;

import com.losMatus.estadoCamaras.entity.Camara;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PingVerificador implements Verificador{

    private static final int TIMEOUT_MS = 2000;

    @Override
    public ResultadoCheck verificar(Camara camara) {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        List<String> comando = windows
                ? List.of("ping", "-n", "1", "-w", String.valueOf(TIMEOUT_MS), camara.getIp())
                : List.of("ping", "-c", "1", "-W", String.valueOf(Math.max(1, TIMEOUT_MS / 1000)), camara.getIp());

        try {
            Process proceso = new ProcessBuilder(comando).redirectErrorStream(true).start();

            if (!proceso.waitFor(TIMEOUT_MS + 2000L, TimeUnit.MILLISECONDS)) {
                proceso.destroyForcibly();
                return ResultadoCheck.fallo("Ping sin respuesta (timeout)");
            }

            String salida = new String(proceso.getInputStream().readAllBytes()).toLowerCase();
            // En Windows, "host inaccesible" puede devolver código 0, por eso se busca ttl=
            return salida.contains("ttl=")
                    ? ResultadoCheck.ok("Ping respondió")
                    : ResultadoCheck.fallo("Ping sin respuesta");

        } catch (IOException e) {
            return ResultadoCheck.fallo("No se pudo ejecutar ping: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResultadoCheck.fallo("Ping interrumpido");
        }
    }
}
