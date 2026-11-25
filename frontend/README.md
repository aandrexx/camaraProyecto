# EstadoCámaras · Frontend (React + Vite)

Panel web de EstadoCámaras hecho con React 19 y Vite. Consume la API de Spring Boot de [`../estadoCamaras`](../estadoCamaras).

## Requisitos

- Node.js 20.19 o superior (o 22.12+)
- El backend corriendo (por defecto en `http://localhost:8080`)

## Desarrollo

```bash
cd frontend
npm install
npm run dev
```

Abre <http://localhost:5173>. En desarrollo, Vite reenvía `/api/**` y `/auth/**` al backend (proxy en `vite.config.js`), así que **no hace falta configurar CORS**.

Si el backend corre en otra dirección, crea `frontend/.env.local`:

```dotenv
BACKEND_URL=http://localhost:9090
```

## Scripts

| Comando | Qué hace |
|---|---|
| `npm run dev` | Servidor de desarrollo con recarga en caliente |
| `npm run build` | Genera la versión de producción en `dist/` |
| `npm run preview` | Sirve `dist/` para probar el build |
| `npm run lint` | Revisa el código con oxlint |

## Producción

`npm run build` genera archivos estáticos en `dist/`. Dos formas de publicarlos:

1. **Mismo servidor que Spring** (recomendado): copiar el contenido de `dist/` a `estadoCamaras/src/main/resources/static/` antes de compilar el JAR. Así la API y el panel comparten origen y no hay CORS. En ese caso, `SecurityConfig` debe permitir `GET /assets/**` (hoy solo permite `/css/**` y `/js/**`).
2. **Servidor aparte**: compilar con `VITE_API_URL=http://servidor:8080 npm run build` y agregar el origen del frontend a `CorsConfig` (también para `/auth/**`, que hoy no está incluido).

## Estructura

```
src/
  api/client.js              Llamadas HTTP, token JWT y manejo de errores de la API
  context/AuthContext.jsx    Sesión (login, registro, logout) guardada en sessionStorage
  components/
    AuthScreen.jsx           Inicio de sesión y registro
    Dashboard.jsx            Panel principal, carga y refresco automático cada minuto
    Sidebar.jsx              Navegación y estado de conexión con la API
    StatsGrid.jsx            Tarjetas de resumen
    CameraTable.jsx          Tabla de cámaras con búsqueda y filtro por estado
    RetiredCameras.jsx       Cámaras retiradas y restauración
    CameraFormModal.jsx      Alta y edición de cámaras
    HistoryModal.jsx         Historial de eventos de una cámara
    TechniciansSection.jsx   CRUD de técnicos (solo ADMIN)
    Modal.jsx                Envoltorio de <dialog>
  utils/format.js            Fechas, estados y búsqueda
  styles/                    Estilos del panel
```

## Roles

- **USER**: ve cámaras, retiradas e historial.
- **ADMIN**: además crea, edita, retira y restaura cámaras, y administra técnicos.

Las cuentas creadas desde "Crear cuenta" son `USER`; el rol `ADMIN` se asigna en la base de datos.
