package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.entity.HistorialEstado;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import com.losMatus.estadoCamaras.enums.TipoEvento;
import com.losMatus.estadoCamaras.repository.CamaraRepository;
import com.losMatus.estadoCamaras.repository.HistorialEstadoRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;

@Service
public class MonitoreoService {

    private final CamaraRepository camaraRepository;
    private final HistorialEstadoRepository historialEstadoRepository;

    public MonitoreoService(CamaraRepository camaraRepository, HistorialEstadoRepository historialEstadoRepository) {
        this.camaraRepository = camaraRepository;
        this.historialEstadoRepository = historialEstadoRepository;
    }

    //Simplificamos a solo trabajar con TCP
    public boolean verificarConectividad(String ip, int puerto) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, puerto), 2000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    //NUEVO METODO PARA LA MAC
    public String obtenerMacPorIp(String ip) {
        try {
            Process proceso = new ProcessBuilder("arp", "-a", ip).start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream()));

            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().startsWith(ip)) {
                    // la línea suele verse así:
                    // 172.20.0.10          00-1a-2b-3c-4d-5e     dinámico
                    String[] partes = linea.trim().split("\\s+");
                    if (partes.length >= 2) {
                        return partes[1].replace("-", ":").toUpperCase();
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    //ACTUALIZAMOS EL METODO PARA GUARDAR LA MAC DETECTADA
    public void actualizarEstadoCamara(Camara camara) {
        EstadoCamara estadoAnterior = camara.getEstado();
        boolean macCoincidiaAntes = calcularMacCoincide(camara); // ANTES de actualizar

        boolean conectada = verificarConectividad(camara.getIp(), camara.getPuerto());
        EstadoCamara estadoNuevo = conectada ? EstadoCamara.ACTIVA : EstadoCamara.INACTIVA;
        String macDetectada = obtenerMacPorIp(camara.getIp());

        camara.setEstado(estadoNuevo);
        camara.setUltimaVerificacion(LocalDateTime.now());
        camara.setUltimaMacDetectada(macDetectada);
        camaraRepository.save(camara);

        boolean macCoincideAhora = calcularMacCoincide(camara); // DESPUÉS de actualizar

        // Evento 1: cambio de estado (activa/inactiva), igual que antes
        if (estadoAnterior != estadoNuevo) {
            HistorialEstado historial = new HistorialEstado();
            historial.setCamara(camara);
            historial.setEstadoAnterior(estadoAnterior);
            historial.setEstadoNuevo(estadoNuevo);
            historial.setTipoEvento(TipoEvento.CAMBIO_ESTADO);
            historial.setFechaCambio(LocalDateTime.now());
            historialEstadoRepository.save(historial);
        }

        // Evento 2: nueva alerta de discrepancia de MAC (solo cuando pasa de "ok" a "no coincide")
        if (macCoincidiaAntes && !macCoincideAhora) {
            HistorialEstado alerta = new HistorialEstado();
            alerta.setCamara(camara);
            alerta.setTipoEvento(TipoEvento.ALERTA_MAC);
            alerta.setFechaCambio(LocalDateTime.now());
            historialEstadoRepository.save(alerta);
        }
    }

    public boolean calcularMacCoincide(Camara camara) {
        if (camara.getMacEsperada() == null || camara.getUltimaMacDetectada() == null) {
            return true; // sin datos suficientes, no se considera una alerta (aún no hay nada que comparar)
        }
        return camara.getUltimaMacDetectada().equalsIgnoreCase(camara.getMacEsperada());
    }
}
