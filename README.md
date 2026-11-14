# EstadoCámaras

Sistema web para administrar y monitorear cámaras IP. El servidor comprueba periódicamente su conectividad, mantiene un historial y puede notificar por WhatsApp a los técnicos activos.

## Qué hace

- **Comprueba cámaras** mediante ping, un puerto TCP opcional y la MAC cuando la cámara responde y el servidor puede resolverla. La primera verificación ocurre unos segundos después del arranque; las siguientes se programan con un intervalo de un minuto.
- **Registra estados** (`ACTIVA`, `INACTIVA`, `DEGRADADA`, `ALERTA` o `DESCONOCIDO`) y cambios en el historial.
- **Notifica por WhatsApp** al entrar o salir de los estados `INACTIVA` o `ALERTA`; en la primera comprobación del servidor también puede avisar si una cámara ya está en uno de esos estados.
- **Administra técnicos** desde el panel web; los técnicos activos son los destinatarios de WhatsApp.
- **Protege la API** con JWT y roles `USER` y `ADMIN`.

## Tecnologías

- Java 17 y Spring Boot (Web, Data JPA, Security)
- MySQL
- JWT para autenticación
- API de WhatsApp Cloud (Meta); el backend también contempla Telegram y consola

## Requisitos

- **Java 17** (el proyecto compila con `java.version=17`; JDK para compilar, runtime compatible para ejecutar)
- **MySQL** con una base de datos creada (por ejemplo `estado_camaras`) y un usuario con permisos sobre ella
- Si se elige WhatsApp: una cuenta de WhatsApp Business en Meta con una plantilla aprobada (ver más abajo)

## Configuración

La configuración se define con variables de entorno. El archivo [`estadoCamaras/.env.example`](estadoCamaras/.env.example) es una plantilla con valores de ejemplo, no un archivo de configuración que la aplicación cargue por sí sola. **Spring Boot no lee automáticamente `.env`**: define las variables en el entorno del proceso, en el servicio que ejecuta el JAR o en la configuración de ejecución del IDE.

```dotenv
# Base de datos
DB_URL=jdbc:mysql://localhost:3306/estado_camaras
DB_USERNAME=usuario_app
DB_PASSWORD=reemplazar

# Seguridad
JWT_SECRET=reemplazar

# Canal opcional: whatsapp (predeterminado), telegram o consola
NOTIFICACIONES_CANAL=whatsapp

# Credenciales del canal elegido
WHATSAPP_TOKEN=reemplazar
WHATSAPP_PHONE_NUMBER_ID=reemplazar
TELEGRAM_TOKEN=reemplazar
TELEGRAM_CHAT_ID=reemplazar
```

| Variable | Descripción |
|---|---|
| `DB_URL` | URL JDBC de MySQL. El nombre de la base debe existir. |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales de MySQL. |
| `JWT_SECRET` | Secreto para firmar los tokens; usa al menos 32 bytes aleatorios. **Debe ser distinto en cada instalación y mantenerse privado.** |
| `WHATSAPP_TOKEN` | Token de acceso de la API de WhatsApp con los permisos requeridos y una vigencia controlada según la configuración de Meta. |
| `WHATSAPP_PHONE_NUMBER_ID` | ID del número emisor en Meta. |
| `NOTIFICACIONES_CANAL` | `whatsapp`, `telegram` o `consola`; si se omite, el valor predeterminado es `whatsapp`. |
| `TELEGRAM_TOKEN` / `TELEGRAM_CHAT_ID` | Credenciales requeridas si se selecciona Telegram. |

Los valores reales deben configurarse en el entorno donde se inicia la app. No subas secretos, archivos `.env` ni scripts de arranque con credenciales al repositorio.

Valores actuales en `application.properties`: plantilla de WhatsApp `alerta_camara`, idioma `es`, API `v25.0` y expiración del JWT de 24 horas.

## Generar un JWT_SECRET

En PowerShell:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Guarda el resultado de forma segura como `JWT_SECRET`. No uses `Get-Random` para generar secretos.

## Configurar WhatsApp

1. En Meta for Developers, crea una app con el producto **WhatsApp** y obtén el **ID del número** (`WHATSAPP_PHONE_NUMBER_ID`).
2. Crea y aprueba una plantilla de categoría **Utilidad**, en español, llamada `alerta_camara`, con este cuerpo y 5 variables en este orden:

   ```
   Se detectó un cambio en el monitoreo de una cámara.

   Cámara: {{1}}
   Ubicación: {{2}}
   Dirección IP: {{3}}
   Estado actual: {{4}}
   Motivo: {{5}}

   Esta es una notificación automática del sistema de monitoreo. Revisa el estado del equipo en el panel.
   ```

3. Genera un token de acceso para la app y concede los permisos requeridos por la configuración vigente de Meta:
   - En Meta Business, ve a *Configuración del negocio, Usuarios, Usuarios del sistema* y crea o selecciona un usuario del sistema.
   - Asígnale acceso a la app, cuenta y número de WhatsApp que requiere el envío; evita conceder permisos que la aplicación no necesita.
   - Pulsa *Generar token* y selecciona los permisos requeridos, incluidos `whatsapp_business_messaging` y `whatsapp_business_management`.
   - Configura el token en `WHATSAPP_TOKEN` y renuévalo antes de su vencimiento o revocación, según la expiración que indique Meta.

> Los números de prueba y de producción tienen restricciones de destinatarios, plantillas, consentimiento y ventanas de conversación definidas por Meta. Registrar un técnico en la aplicación no omite esas reglas.

## Compilar

```bash
cd estadoCamaras
./mvnw clean package
```

En Windows PowerShell, desde la raíz del repositorio:

```powershell
cd estadoCamaras
.\mvnw.cmd clean package
```

El comando genera el JAR en `estadoCamaras/target/` y ejecuta las pruebas; la prueba de contexto necesita que la base de datos configurada esté disponible. La compilación usa Java 17 (`java.version` en `estadoCamaras/pom.xml`).

## Instalación en Windows Server

Este procedimiento describe la ejecución directa del JAR en Windows; no hay una configuración Docker incluida. El proceso nativo puede consultar la red y la tabla ARP del servidor, pero esto no garantiza que pueda resolver la MAC de cada cámara: ambas deben estar en la misma subred de capa 2. El ping, el firewall y las reglas de red también afectan el resultado.

1. **Instalar un runtime Java 17** compatible con la versión concreta de Windows Server y comprobar con `java -version`. Verifica la compatibilidad del proveedor de Java con Windows Server 2012 antes de elegirlo; si no está soportado, actualiza el sistema operativo o usa una plataforma mantenida.
2. **Instalar MySQL** y crear la base de datos y el usuario:

   ```sql
   CREATE DATABASE estado_camaras CHARACTER SET utf8mb4;
   CREATE USER 'monitoreo'@'localhost' IDENTIFIED BY 'una_clave_segura';
   GRANT ALL PRIVILEGES ON estado_camaras.* TO 'monitoreo'@'localhost';
   ```

3. **Copiar el `.jar`** a una carpeta, por ejemplo `C:\monitoreo\`.
4. **Crear el archivo de arranque** `C:\monitoreo\iniciar.bat` con los datos reales de ese servidor:

   ```bat
   @echo off
   cd /d C:\monitoreo
   set "DB_URL=jdbc:mysql://localhost:3306/estado_camaras"
   set "DB_USERNAME=monitoreo"
   set "DB_PASSWORD=REEMPLAZAR"
   set "JWT_SECRET=REEMPLAZAR"
   set "NOTIFICACIONES_CANAL=whatsapp"
   set "WHATSAPP_TOKEN=REEMPLAZAR"
   set "WHATSAPP_PHONE_NUMBER_ID=REEMPLAZAR"
   java -jar estadoCamaras-0.0.1-SNAPSHOT.jar
   ```

   Sustituye todos los valores de ejemplo. Este archivo contiene secretos: restríngele el acceso en Windows y no lo subas al repositorio. Un `.env` no se carga automáticamente con este procedimiento.

5. **Probar** ejecutando `iniciar.bat` y revisando que la aplicación arranque sin errores.
6. **Configurar inicio y recuperación del servicio** con una herramienta aprobada por tu organización. Si usas NSSM o el Programador de tareas, configura reinicio tras fallos, cuenta de servicio con permisos mínimos y acceso restringido a los secretos.
7. **Firewall:** abrir el puerto de la API (por defecto `8080`) si se consultará desde otros equipos.

## Primer uso

1. Crea una cuenta desde el formulario de registro; el endpoint público asigna siempre el rol `USER`.
2. Para crear el administrador inicial, cambia el rol de esa cuenta directamente en MySQL:

   ```sql
   UPDATE usuarios
   SET rol = 'ADMIN'
   WHERE email = 'admin@ejemplo.com';
   ```

3. Inicia sesión de nuevo con esa cuenta. El panel obtiene el token JWT y el rol actualizado.
4. Registra cámaras desde el panel con IP, puerto opcional, MAC esperada y ubicación.
5. Registra técnicos activos desde **Técnicos** para que puedan recibir las notificaciones.

Para consumir la API fuera del panel, envía el token obtenido en `/auth/login` en cada solicitud protegida:

```http
Authorization: Bearer <token>
```

### Teléfonos de los técnicos

Entre 8 y 15 dígitos, incluido el código de país, **sin `+` ni espacios**. Por ejemplo, para un teléfono peruano: `51987654321`. Confirma que el número pueda recibir mensajes de WhatsApp según las reglas de Meta.

## API principal

| Ruta | Descripción | Acceso |
|---|---|---|
| `POST /auth/register` | Registro; crea siempre `USER` | Público |
| `POST /auth/login` | Inicio de sesión; responde token y rol | Público |
| `/api/camaras` | Gestión de cámaras | Lectura: autenticado. Cambios: `ADMIN` |
| `/api/tecnicos` | Gestión de técnicos | Solo `ADMIN` |

Operaciones de cámaras: `GET /api/camaras`, `GET /api/camaras/retiradas`, `GET /api/camaras/{id}`, `GET /api/camaras/{id}/historial`, `POST /api/camaras`, `PUT /api/camaras/{id}`, `DELETE /api/camaras/{id}` y `POST /api/camaras/{id}/restaurar`. Eliminar retira la cámara y conserva su historial.

Operaciones de técnicos:

- `POST /api/tecnicos` crea un técnico.
- `GET /api/tecnicos` y `GET /api/tecnicos/{id}` consultan.
- `PUT /api/tecnicos/{id}` edita.
- `DELETE /api/tecnicos/{id}` desactiva (deja de recibir alertas).
- `POST /api/tecnicos/{id}/reactivar` vuelve a activarlo.

## Notas operativas

- Si no hay técnicos activos, el sistema registra un aviso en el log y no envía alertas.
- Un fallo al enviar a un técnico queda registrado y no impide intentar el envío a los demás técnicos.
- Si las alertas dejan de llegar y el log indica un error de autorización, revisa el token y permisos configurados en Meta; reinicia la aplicación después de cambiar las variables.
- Al cambiar cualquier variable de entorno, hay que reiniciar la aplicación.
- El servidor necesita salida a Internet para comunicarse con las APIs de WhatsApp o Telegram.
- La detección de MAC mediante ARP normalmente solo identifica cámaras en la misma subred que el servidor. Un router puede hacer que se observe la MAC del gateway en lugar de la cámara.

## Base de datos y migraciones

La aplicación tiene configurado `spring.jpa.hibernate.ddl-auto=update`: Hibernate puede actualizar automáticamente el esquema al iniciar. Esto no sustituye migraciones versionadas ni un respaldo antes de actualizar una base con datos reales.

El script [`estadoCamaras/db/migrate_empresa_to_single_company.sql`](estadoCamaras/db/migrate_empresa_to_single_company.sql) es para bases antiguas que todavía tengan `camara.empresa_id` y su relación con `empresa`. Antes de ejecutarlo, respalda la base y confirma que esa columna exista. No se requiere para una base nueva.

## Mejoras previstas

- Migraciones de base de datos versionadas; actualmente Hibernate usa `ddl-auto=update`.
- Configuración de despliegue reproducible, respaldos y comprobación de salud del servicio.
- Verificación RTSP para confirmar que el servicio de video responde.