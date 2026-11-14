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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "notificaciones.canal", havingValue = "whatsapp")
public class WhatsappNotificador implements Notificador{

    private final RestClient restClient;

    @Value("${notificaciones.whatsapp.token}")
    private String token;

    @Value("${notificaciones.whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${notificaciones.whatsapp.destino}")
    private String destino;

    @Value("${notificaciones.whatsapp.plantilla}")
    private String plantilla;

    @Value("${notificaciones.whatsapp.api-version}")
    private String apiVersion;

    @Value("${notificaciones.whatsapp.idioma:es}")
    private String idioma;

    public WhatsappNotificador() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .baseUrl("https://graph.facebook.com")
                .requestFactory(factory)
                .build();
    }

    @Override
    public void notificar(Camara camara, EstadoCamara anterior, EstadoCamara nuevo, String motivo) {

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", destino,
                "type", "template",
                "template", Map.of(
                        "name", plantilla,
                        "language", Map.of("code", idioma),
                        "components", List.of(Map.of(
                                "type", "body",
                                "parameters", List.of(
                                        parametro(camara.getNombre()),
                                        parametro(camara.getUbicacion()),
                                        parametro(camara.getIp()),
                                        parametro(nuevo.name()),
                                        parametro(motivo)
                                )
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

    // Los parámetros de una plantilla no pueden ir vacíos ni tener saltos de línea
    private Map<String, String> parametro(String valor) {
        String limpio = (valor == null || valor.isBlank()) ? "-" : valor.replaceAll("\\s+", " ").trim();
        return Map.of("type", "text", "text", limpio);
    }



//    //prueba de mensaje
//    @Override
//    public void notificar(Camara camara, EstadoCamara anterior, EstadoCamara nuevo, String motivo) {
//        Map<String, Object> template = new HashMap<>();
//        template.put("name", plantilla);
//        template.put("language", Map.of("code", idioma));
//
//        // hello_world no lleva parámetros; las plantillas propias sí
//        if (!"hello_world".equals(plantilla)) {
//            template.put("components", List.of(Map.of(
//                    "type", "body",
//                    "parameters", List.of(
//                            parametro(camara.getNombre()),
//                            parametro(camara.getUbicacion()),
//                            parametro(camara.getIp()),
//                            parametro(nuevo.name()),
//                            parametro(motivo)
//                    )
//            )));
//        }
//
//        Map<String, Object> body = Map.of(
//                "messaging_product", "whatsapp",
//                "to", destino,
//                "type", "template",
//                "template", template
//        );
//
//        restClient.post()
//                .uri("/{version}/{phoneId}/messages", apiVersion, phoneNumberId)
//                .header("Authorization", "Bearer " + token)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(body)
//                .retrieve()
//                .toBodilessEntity();
//    }

}
