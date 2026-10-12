package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.repository.CamaraRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class MonitoreoScheduler {

    private final CamaraRepository camaraRepository;
    private final MonitoreoService monitoreoService;

    private static final int HILOS = 20;

    public MonitoreoScheduler(CamaraRepository camaraRepository, MonitoreoService monitoreoService) {
        this.camaraRepository = camaraRepository;
        this.monitoreoService = monitoreoService;
    }

    @Scheduled(fixedDelay = 60000)
    public void chequearTodasLasCamaras() {
        List<Camara> camaras = camaraRepository.findAll();

        // close() del executor espera a que terminen todas las tareas (Java 19+)
        try (ExecutorService executor = Executors.newFixedThreadPool(HILOS)) {
            for (Camara camara : camaras) {
                executor.submit(() -> {
                    try {
                        monitoreoService.actualizarEstadoCamara(camara);
                    } catch (Exception e) {
                        log.error("Error chequeando la cámara id={} ip={}", camara.getId(), camara.getIp(), e);
                    }
                });
            }
        }
    }
}
