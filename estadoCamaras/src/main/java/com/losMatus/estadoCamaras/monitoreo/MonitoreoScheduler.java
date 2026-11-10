package com.losMatus.estadoCamaras.monitoreo;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.repository.CamaraRepository;
import com.losMatus.estadoCamaras.service.MonitoreoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class MonitoreoScheduler {

    private final MonitoreoService monitoreoService;

    public MonitoreoScheduler(MonitoreoService monitoreoService) {
        this.monitoreoService = monitoreoService;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 5000)
    public void ejecutar() {
        monitoreoService.monitorearTodas();
    }
}
