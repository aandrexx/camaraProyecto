package com.losMatus.estadoCamaras.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Obtiene la MAC de un host a partir de su IP leyendo la tabla ARP del sistema.
 * <ul>
 *   <li>Linux: lee /proc/net/arp (sin lanzar procesos).</li>
 *   <li>Windows (y otros): ejecuta "arp -a <ip>" con timeout.</li>
 * </ul>
 * Limitación: ARP solo conoce la MAC real si la cámara está en la misma subred (capa 2)
 * que el servidor. Si hay un router en medio, la MAC que aparece es la del router.
 */
@Component
public class MacResolver {

    private static final Logger log = LoggerFactory.getLogger(MacResolver.class);

    private static final Path PROC_NET_ARP = Path.of("/proc/net/arp");
    private static final long TIMEOUT_COMANDO_SEG = 3;
    private static final String MAC_VACIA = "00:00:00:00:00:00";

    private static final Pattern IPV4 = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)$");
    private static final Pattern MAC = Pattern.compile("(?i)(?:[0-9a-f]{2}[:-]){5}[0-9a-f]{2}");

    /** @return la MAC en formato AA:BB:CC:DD:EE:FF, o vacío si no se pudo determinar. */
    public Optional<String> resolver(String ip) {
        // Validar antes de usar la IP: evita que un valor raro llegue como argumento al comando.
        if (ip == null || !IPV4.matcher(ip).matches()) {
            log.warn("IP inválida, no se consulta la MAC: {}", ip);
            return Optional.empty();
        }
        try {
            if (Files.isReadable(PROC_NET_ARP)) {
                return parsearProcNetArp(Files.readAllLines(PROC_NET_ARP, StandardCharsets.UTF_8), ip);
            }
            return resolverConComandoArp(ip);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException e) {
            log.warn("No se pudo leer la tabla ARP para {}: {}", ip, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> resolverConComandoArp(String ip) throws IOException, InterruptedException {
        Process proceso = new ProcessBuilder("arp", "-a", ip).redirectErrorStream(true).start();
        if (!proceso.waitFor(TIMEOUT_COMANDO_SEG, TimeUnit.SECONDS)) {
            proceso.destroyForcibly();
            log.warn("El comando arp excedió {}s para {}", TIMEOUT_COMANDO_SEG, ip);
            return Optional.empty();
        }
        String salida = new String(proceso.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return parsearSalidaArp(salida.lines().toList(), ip);
    }

    /**
     * Formato de /proc/net/arp:
     * IP address   HW type   Flags   HW address          Mask   Device
     * 192.168.1.5  0x1       0x2     aa:bb:cc:dd:ee:ff   *      eth0
     * Flags 0x0 = entrada incompleta (el host no respondió).
     */
    static Optional<String> parsearProcNetArp(List<String> lineas, String ip) {
        return lineas.stream()
                .skip(1) // encabezado
                .map(l -> l.trim().split("\\s+"))
                .filter(p -> p.length >= 4 && p[0].equals(ip) && !p[2].equals("0x0"))
                .map(p -> normalizar(p[3]))
                .filter(mac -> MAC.matcher(mac).matches() && !mac.equals(MAC_VACIA))
                .findFirst();
    }

    /**
     * Soporta la salida de Windows ("172.20.0.10   00-1a-2b-3c-4d-5e   dinámico")
     * y la de net-tools en Linux ("? (172.20.0.10) at 00:1a:2b:3c:4d:5e [ether] on eth0").
     */
    static Optional<String> parsearSalidaArp(List<String> lineas, String ip) {
        // La IP debe aparecer completa: 192.168.1.1 no debe coincidir con 192.168.1.10
        Pattern ipExacta = Pattern.compile("(?<![\\d.])" + Pattern.quote(ip) + "(?![\\d.])");
        for (String linea : lineas) {
            if (!ipExacta.matcher(linea).find()) {
                continue;
            }
            Matcher m = MAC.matcher(linea);
            if (m.find()) {
                String mac = normalizar(m.group());
                if (!mac.equals(MAC_VACIA)) {
                    return Optional.of(mac);
                }
            }
        }
        return Optional.empty();
    }

    static String normalizar(String mac) {
        return mac.replace('-', ':').toUpperCase();
    }
}
