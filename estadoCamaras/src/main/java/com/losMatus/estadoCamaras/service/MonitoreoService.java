package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.entity.HistorialEstado;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import com.losMatus.estadoCamaras.enums.TipoEvento;
import com.losMatus.estadoCamaras.repository.CamaraRepository;
import com.losMatus.estadoCamaras.repository.HistorialEstadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class MonitoreoService {

    private static final int UMBRAL_FALLOS = 3;

    private final CamaraRepository camaraRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final MacResolver macResolver;

    public MonitoreoService(CamaraRepository camaraRepository,
                            HistorialEstadoRepository historialEstadoRepository,
                            MacResolver macResolver) {
        this.camaraRepository = camaraRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.macResolver = macResolver;
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

    public void actualizarEstadoCamara(Camara camara) {
        String ip = camara.getIp();
        Integer puerto = camara.getPuerto();
        if (ip == null || ip.isBlank() || puerto == null || puerto < 1 || puerto > 65535) {
            log.warn("Cámara id={} con ip/puerto inválido (ip={}, puerto={}), se omite el chequeo",
                    camara.getId(), ip, puerto);
            return;
        }

        EstadoCamara estadoAnterior = camara.getEstado();
        boolean macCoincidiaAntes = calcularMacCoincide(camara); // ANTES de actualizar

        boolean conectada = verificarConectividad(ip, puerto);
        int fallos = camara.getFallosConsecutivos() == null ? 0 : camara.getFallosConsecutivos();

        EstadoCamara estadoNuevo;
        if (conectada) {
            fallos = 0; // un solo éxito reinicia el contador
            estadoNuevo = EstadoCamara.ACTIVA;
        } else {
            fallos++;
            // Antes del umbral se conserva el estado que ya tenía la cámara
            estadoNuevo = (fallos >= UMBRAL_FALLOS) ? EstadoCamara.INACTIVA : estadoAnterior;
        }

        camara.setFallosConsecutivos(fallos);
        camara.setEstado(estadoNuevo);
        camara.setUltimaVerificacion(LocalDateTime.now());

        // Solo consultamos la MAC si la cámara respondió por TCP (así la entrada ARP es fresca).
        // Si no se logra leer, se conserva la última MAC conocida en vez de borrarla con null.
        if (conectada) {
            Optional<String> mac = macResolver.resolver(ip);
            mac.ifPresent(camara::setUltimaMacDetectada);
            if (mac.isEmpty() && camara.getMacEsperada() != null) {
                log.warn("Cámara id={} responde por TCP pero no se pudo leer su MAC (¿otra subred?)",
                        camara.getId());
            }
        }

        // Update dirigido: solo toca los campos del monitoreo, no pisa ediciones hechas por la API
        camaraRepository.actualizarEstadoMonitoreo(
                camara.getId(),
                camara.getEstado(),
                camara.getUltimaVerificacion(),
                camara.getUltimaMacDetectada(),
                camara.getFallosConsecutivos());

        boolean macCoincideAhora = calcularMacCoincide(camara); // DESPUÉS de actualizar

        // Evento 1: cambio de estado (activa/inactiva)
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