package com.losMatus.estadoCamaras.notificador;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.entity.Tecnico;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import com.losMatus.estadoCamaras.repository.TecnicoRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ConditionalOnProperty(name = "notificaciones.canal", havingValue = "whatsapp")
public class WhatsappNotificador implements Notificador{

    private static final Logger log = LoggerFactory.getLogger(WhatsappNotificador.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private final RestClient restClient;
    private final TecnicoRepository tecnicoRepository;

    @Value("${notificaciones.whatsapp.token}")
    private String token;

    @Value("${notificaciones.whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${notificaciones.whatsapp.plantilla}")
    private String plantilla;

    @Value("${notificaciones.whatsapp.api-version}")
    private String apiVersion;

    @Value("${notificaciones.whatsapp.idioma:es}")
    private String idioma;

    public WhatsappNotificador(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .baseUrl("https://graph.facebook.com")
                .requestFactory(factory)
                .build();
    }
    private void enviar(String destino, List<Map<String, String>> parametros) {
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", destino,
                "type", "template",
                "template", Map.of(
                        "name", plantilla,
                        "language", Map.of("code", idioma),
                        "components", List.of(Map.of(
                                "type", "body",
                                "parameters", parametros
                        ))
                )
        );

        restClient.post()
                .uri("/{version}/{phoneId}/messages", apiVersion, phoneNumberId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void notificar(Camara camara, EstadoCamara anterior, EstadoCamara nuevo, String motivo) {
        List<Tecnico> tecnicos = tecnicoRepository.findByActivoTrue();

        if (tecnicos.isEmpty()) {
            log.warn("Cámara {}: no hay técnicos activos para notificar", camara.getId());
            return;
        }

        List<Map<String, String>> parametros = List.of(
                parametro(camara.getNombre()),
                parametro(camara.getUbicacion()),
                parametro(camara.getIp()),
                parametro(nuevo.name()),
                parametro(motivo)
        );

        CompletableFuture<?>[] envios = tecnicos.stream()
                .map(t -> CompletableFuture.runAsync(() -> enviarATecnico(t, parametros), executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(envios).join();
    }

    private void enviarATecnico(Tecnico tecnico, List<Map<String, String>> parametros) {
        try {
            enviar(tecnico.getTelefono(), parametros);
        } catch (RestClientResponseException e) {
            log.error("WhatsApp: falló el envío al técnico {} ({}): {} {}",
                    tecnico.getId(), tecnico.getNombre(),
                    e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("WhatsApp: falló el envío al técnico {} ({}): {}",
                    tecnico.getId(), tecnico.getNombre(), e.getMessage());
        }
    }

    @PreDestroy
    void cerrar() {
        executor.shutdown();
    }


    // Los parámetros de una plantilla no pueden ir vacíos ni tener saltos de línea
    private Map<String, String> parametro(String valor) {
        String limpio = (valor == null || valor.isBlank()) ? "-" : valor.replaceAll("\\s+", " ").trim();
        return Map.of("type", "text", "text", limpio);
    }

}
