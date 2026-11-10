package com.losMatus.estadoCamaras.monitoreo;

import com.losMatus.estadoCamaras.entity.Camara;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

@Component
public class TcpVerificador implements Verificador{

    @Value("${monitoreo.timeout-ms:2000}")
    private int timeoutMs;

    @Override
    public ResultadoCheck verificar(Camara camara) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(camara.getIp(), camara.getPuerto()), timeoutMs);
            return ResultadoCheck.ok("Puerto " + camara.getPuerto() + " abierto");
        } catch (SocketTimeoutException e) {
            return ResultadoCheck.fallo("Puerto " + camara.getPuerto() + ": timeout");
        } catch (IOException e) {
            return ResultadoCheck.fallo("Puerto " + camara.getPuerto() + ": conexión rechazada o sin ruta");
        }
    }
}
