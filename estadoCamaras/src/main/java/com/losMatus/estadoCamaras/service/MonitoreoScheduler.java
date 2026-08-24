package com.losMatus.estadoCamaras.service;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.repository.CamaraRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MonitoreoScheduler {

    private final CamaraRepository camaraRepository;
    private final MonitoreoService monitoreoService;

    public MonitoreoScheduler(CamaraRepository camaraRepository, MonitoreoService monitoreoService) {
        this.camaraRepository = camaraRepository;
        this.monitoreoService = monitoreoService;
    }

    @Scheduled(fixedRate = 60000)
    public void chequearTodasLasCamaras(){
        List<Camara> camaras = camaraRepository.findAll();
        for(Camara camara : camaras){
            monitoreoService.actualizarEstadoCamara(camara);
        }
    }
}
