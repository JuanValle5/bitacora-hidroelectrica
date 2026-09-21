# Bitácora Hidroeléctrica

Sistema web integral para el registro y gestión operativa de una central hidroeléctrica, facilitando el control horario de generación, variables eléctricas/térmicas de generadores (G1 y G2), consumo de servicios auxiliares, relevo formal de turnos y trazabilidad por auditoría.

---

## Stack Tecnológico

- **Backend:** Java 21, Spring Boot 4, Spring Data JPA, Hibernate, JWT (JSON Web Tokens), iText (PDF).
- **Frontend:** React 19, TypeScript, Vite, Tailwind CSS v4.
- **Base de Datos:** PostgreSQL 17.
- **Contenedores:** Docker & Docker Compose (orquestación multi-servicio con Nginx como proxy reverso).

---

## Inicio Rápido con Docker Compose

El proyecto está configurado para levantarse completamente con un único comando:

```bash
# 1. Clonar el repositorio
git clone git@github.com:JuanValle5/bitacora-hidroelectrica.git
cd bitacora-hidroelectrica

# 2. Configurar variables de entorno (opcional, ya incluye .env por defecto)
cp .env.example .env

# 3. Levantar los servicios (Base de Datos + Backend + Frontend)
docker compose up -d
```

### URLs de Acceso

| Servicio | URL | Descripción |
| :--- | :--- | :--- |
| **Frontend Web** | [http://localhost:8443](http://localhost:8443) | Interfaz de usuario SPA |
| **Backend API** | [http://localhost:8081/api/v1](http://localhost:8081/api/v1) | Endpoints REST |
| **PostgreSQL** | `localhost:5434` (DB: `bitacora_db`, User: `bitacora_user`) | Base de datos relacional |

---

## Credenciales por Defecto

El sistema inicializa automáticamente los usuarios de prueba:

| Rol | Usuario | Contraseña | Funciones |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin` | `admin123` | Monitoreo KPIs, gestión de usuarios, pistas de auditoría y exportación a PDF. |
| **Operador en Turno** | `c.mendoza` | `op123` | Registro y edición justificada de lecturas horarias, entrega y relevo de turno. |

---

## Desarrollo Local (Sin Docker)

### Requisitos Previos
- Java 21+
- Node.js 20+ / 22+ y npm
- PostgreSQL corriendo localmente en el puerto `5432` con base de datos `bitacora_db`

### Backend
```bash
cd backend
./mvnw spring-boot:run
```
Pruebas automáticas:
```bash
./mvnw test
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```
Verificación de tipos:
```bash
npm run test
```

---

## Estructura del Proyecto

```text
├── backend/                # API REST Spring Boot (Java 21)
│   ├── src/                # Controladores, servicios, modelos JPA, seguridad JWT
│   ├── pom.xml             # Dependencias Maven
│   └── Dockerfile          # Multi-stage build para contenedor Java
├── frontend/               # Single Page Application React (TypeScript)
│   ├── src/                # Vistas (Admin, Operator, Login), componentes, cliente API
│   ├── nginx.conf          # Configuración proxy reverso Nginx
│   └── Dockerfile          # Multi-stage build Nginx + Vite
├── docs/                   # Constitución del stack y especificación de la API
├── docker-compose.yml      # Orquestación de contenedores (db, backend, frontend)
├── .env.example            # Plantilla de variables de entorno
└── README.md
```
