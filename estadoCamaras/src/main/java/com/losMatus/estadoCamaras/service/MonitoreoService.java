package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.entity.HistorialEstado;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import com.losMatus.estadoCamaras.enums.TipoEvento;
import com.losMatus.estadoCamaras.monitoreo.*;
import com.losMatus.estadoCamaras.notificador.Notificador;
import com.losMatus.estadoCamaras.repository.CamaraRepository;
import com.losMatus.estadoCamaras.repository.HistorialEstadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MonitoreoService {

    private final CamaraRepository camaraRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final PingVerificador pingVerificador;
    private final TcpVerificador tcpVerificador;
    private final MacVerificador macVerificador;
    private final EvaluarEstado evaluarEstado;
    private final Notificador notificador;

    //para verificar camaras inactivas guardadas
    private final Set<Long> verificadasDesdeArranque = ConcurrentHashMap.newKeySet();

    private static final int REINTENTOS = 3;
    private static final long ESPERA_REINTENTO_MS = 3000;

    public MonitoreoService(CamaraRepository camaraRepository, HistorialEstadoRepository historialEstadoRepository,
                            PingVerificador pingVerificador, TcpVerificador tcpVerificador, MacVerificador macVerificador,
                            EvaluarEstado evaluarEstado, Notificador notificador) {
        this.camaraRepository = camaraRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.pingVerificador = pingVerificador;
        this.tcpVerificador = tcpVerificador;
        this.macVerificador = macVerificador;
        this.evaluarEstado = evaluarEstado;
        this.notificador = notificador;
    }

    private record Chequeo(EstadoCamara estado, ResultadoCheck ping, ResultadoCheck tcp, ResultadoCheck mac) {}

    public void monitorearTodas() {
        for (Camara camara : camaraRepository.findAllByEliminadaFalse()) {
            try {
                monitorear(camara);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("Error monitoreando la cámara {}: {}", camara.getId(), e.getMessage());
            }
        }
    }

    private void monitorear(Camara camara) throws InterruptedException {
        EstadoCamara anterior = camara.getEstado();

        // 1. Primer chequeo
        Chequeo chequeo = chequear(camara);

        // 2. Si parece una caída nueva, reintentar antes de confirmarla
        int reintento = 0;
        while (chequeo.estado() == EstadoCamara.INACTIVA
                && anterior != EstadoCamara.INACTIVA
                && reintento < REINTENTOS) {
            Thread.sleep(ESPERA_REINTENTO_MS);
            chequeo = chequear(camara);
            reintento++;
        }

        EstadoCamara nuevo = chequeo.estado();
        camara.setFallosConsecutivos(nuevo == EstadoCamara.INACTIVA ? reintento + 1 : 0);

        // 3. Actualizar la cámara
        ResultadoCheck mac = chequeo.mac();
        if (mac != null && mac.valor() != null) {
            camara.setUltimaMacDetectada(mac.valor());
        }
        camara.setUltimaVerificacion(LocalDateTime.now());

        // 4. Historial: solo si el estado cambió
        boolean cambio = nuevo != anterior;
        String motivoCambio = null;
        if (cambio) {
            motivoCambio = describirCambio(camara, nuevo, chequeo);
            camara.setEstado(nuevo);
            historialEstadoRepository.save(HistorialEstado.builder()
                    .camara(camara)
                    .estadoAnterior(anterior)
                    .estadoNuevo(nuevo)
                    .tipoEvento(nuevo == EstadoCamara.ALERTA
                            ? TipoEvento.ALERTA_MAC
                            : TipoEvento.CAMBIO_ESTADO)
                    .motivo(motivoCambio)
                    .fechaCambio(LocalDateTime.now())
                    .build());
        }

        camaraRepository.save(camara);

        // 5. Notificar: solo cuando la cámara entra o sale de INACTIVA o ALERTA
        boolean primeraVerificacion = verificadasDesdeArranque.add(camara.getId());

        boolean avisoPorCambio = cambio && (esAvisable(nuevo) || esAvisable(anterior));
        boolean avisoAlArrancar = primeraVerificacion && !cambio && esAvisable(nuevo);

        if (avisoPorCambio || avisoAlArrancar) {
            String motivo = cambio ? motivoCambio : describirCambio(camara, nuevo, chequeo);
            notificar(camara, anterior, nuevo, motivo);
        }
    }

    private void notificar(Camara camara, EstadoCamara anterior, EstadoCamara nuevo, String motivo) {
        try {
            notificador.notificar(camara, anterior, nuevo, motivo);
        } catch (Exception e) {
            // un fallo al notificar no debe romper el monitoreo
            log.error("No se pudo enviar la notificación de la cámara {}: {}",
                    camara.getId(), e.getMessage());
        }
    }

    // Un chequeo completo: ping, puerto (si tiene), MAC (si respondió) y evaluación
    private Chequeo chequear(Camara camara) {
        ResultadoCheck ping = pingVerificador.verificar(camara);
        ResultadoCheck tcp = camara.getPuerto() != null ? tcpVerificador.verificar(camara) : null;

        boolean responde = ping.exitoso() || (tcp != null && tcp.exitoso());
        ResultadoCheck mac = responde ? macVerificador.verificar(camara) : null;

        return new Chequeo(evaluarEstado.evaluar(ping, tcp, mac), ping, tcp, mac);
    }

    private boolean esAvisable(EstadoCamara estado) {
        return estado == EstadoCamara.INACTIVA || estado == EstadoCamara.ALERTA;
    }

    private String describirCambio(Camara camara, EstadoCamara estado, Chequeo chequeo) {
        if (estado == EstadoCamara.ALERTA && chequeo.mac() != null && chequeo.mac().valor() != null) {
            return "MAC distinta: esperada " + camara.getMacEsperada()
                    + ", detectada " + chequeo.mac().valor() + ".";
        }

        if (estado == EstadoCamara.INACTIVA) {
            String tcp = chequeo.tcp() == null ? "no configurado" : chequeo.tcp().detalle();
            return "Sin respuesta. Ping: " + chequeo.ping().detalle() + "; TCP: " + tcp + ".";
        }

        if (estado == EstadoCamara.DEGRADADA) {
            return "Ping respondió, pero " + chequeo.tcp().detalle() + ".";
        }

        if (estado == EstadoCamara.ACTIVA && !chequeo.ping().exitoso()
                && chequeo.tcp() != null && chequeo.tcp().exitoso()) {
            return "Ping no respondió; TCP en el puerto " + camara.getPuerto() + " sí respondió.";
        }

        if (estado == EstadoCamara.ACTIVA && chequeo.ping().exitoso() && chequeo.tcp() != null
                && chequeo.tcp().exitoso()) {
            return "Ping y TCP en el puerto " + camara.getPuerto() + " respondieron.";
        }

        if (estado == EstadoCamara.ACTIVA && chequeo.ping().exitoso()) {
            return chequeo.tcp() == null
                    ? "Ping respondió; TCP no se verificó porque no hay un puerto configurado."
                    : "Ping respondió.";
        }

        return "Estado detectado: " + estado + ".";
    }
}
