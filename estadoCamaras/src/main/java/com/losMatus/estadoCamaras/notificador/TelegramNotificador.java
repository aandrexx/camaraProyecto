package com.losMatus.estadoCamaras.notificador;

import com.losMatus.estadoCamaras.entity.Camara;
import com.losMatus.estadoCamaras.enums.EstadoCamara;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Component
@ConditionalOnProperty(name = "notificaciones.canal", havingValue = "telegram")
public class TelegramNotificador implements Notificador{

    private final RestClient restClient;

    @Value("${notificaciones.telegram.token}")
    private String token;

    @Value("${notificaciones.telegram.chat-id}")
    private String chatId;

    public TelegramNotificador() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org")
                .requestFactory(factory)
                .build();
    }

    @Override
    public void notificar(Camara camara, EstadoCamara anterior, EstadoCamara nuevo, String motivo) {
        String mensaje = icono(nuevo) + " Cámara " + nuevo + "\n"
                + camara.getNombre() + " (" + camara.getIp() + ", "
                + Objects.toString(camara.getUbicacion(), "sin ubicación") + ")\n"
                + "Estado anterior: " + anterior + "\n"
                + "Motivo: " + motivo;

        restClient.post()
                .uri("/bot{token}/sendMessage", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("chat_id", chatId, "text", mensaje))
                .retrieve()
                .toBodilessEntity();
    }
    private String icono(EstadoCamara estado) {
        return switch (estado) {
            case ACTIVA -> "🟢";
            case INACTIVA -> "🔴";
            case DEGRADADA -> "🟠";
            case ALERTA -> "⚠️";
            default -> "⚪";
        };
    }
}
