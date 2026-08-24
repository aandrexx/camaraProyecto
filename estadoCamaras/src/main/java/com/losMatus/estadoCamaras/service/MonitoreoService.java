package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.entity.HistorialEstado;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import com.losMatus.estadoCamaras.repository.CamaraRepository;
import com.losMatus.estadoCamaras.repository.HistorialEstadoRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    public boolean verificarConectividad(String ip, int puerto){

        //1: ping ICMP
        boolean pingOk;
        try{
            pingOk = InetAddress.getByName(ip).isReachable(2000); //timeout 2s
        } catch (IOException e) {
            pingOk = false;
        }

        //2: chequeo de puerto TCP
        boolean puertoOk = false;
        try (Socket socket = new Socket()){
            socket.connect(new InetSocketAddress(ip, puerto), 2000);
            puertoOk = true;
        } catch (IOException e) {
            puertoOk = false;
        }
        //CAMBIAMOS A "AND" PARA PROBAR EN DOCKER, POR EL PROBLEMA DE LA VIRTUALIZACION CREO
        //EN PRODUCCION SE PONE SI O SI "OR" ||
        return pingOk && puertoOk; // activa si uno de los dos responde
    }

    public void actualizarEstadoCamara(Camara camara){
        EstadoCamara estadoAnterior = camara.getEstado();
        boolean conectada = verificarConectividad(camara.getIp(), camara.getPuerto());
        EstadoCamara estadoNuevo = conectada ? EstadoCamara.ACTIVA : EstadoCamara.INACTIVA;

        camara.setEstado(estadoNuevo);
        camara.setUltimaVerificacion(LocalDateTime.now());
        camaraRepository.save(camara);

        //guarda en el historial si esl estado cambio
        if (estadoAnterior != estadoNuevo){
            HistorialEstado historial = new HistorialEstado();
            historial.setCamara(camara);
            historial.setEstadoAnterior(estadoAnterior);
            historial.setEstadoNuevo(estadoNuevo);
            historial.setFechaCambio(LocalDateTime.now());
            historialEstadoRepository.save(historial);
        }
    }
}
