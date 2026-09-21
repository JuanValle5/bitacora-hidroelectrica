# AGENTS.md — bitacora-hidroelectrica

## Proyecto
Aplicación web para la gestión y registro operativo de una central hidroeléctrica. Arquitectura dividida en dos capas principales:
- **Backend (`backend/`):** API REST construida con Java Spring Boot, JPA/Hibernate y persistencia en base de datos PostgreSQL.
- **Frontend (`frontend/`):** Single Page Application (SPA) desarrollada con React, TypeScript y estilizada con Tailwind CSS.

## Comandos
- **Backend:** 
  - Ejecutar: `./mvnw spring-boot:run`
  - Tests: `./mvnw test`
- **Frontend:** 
  - Ejecutar: `npm run dev`
  - Tests: `npm run test`

## Estilo
- **Backend:** Java 17+ (o superior). Controladores REST limpios, manejo de excepciones global y uso de DTOs para la transferencia de datos.
- **Frontend:** Componentes funcionales (Hooks), tipado estricto en TypeScript. Estilos manejados exclusivamente con clases de Tailwind CSS (evitar CSS personalizado).
- Identificadores (variables, clases, endpoints, tablas BD) en inglés; mensajes de usuario e interfaz (UI) en español.

## Reglas
- Lee `docs/constitution.md` y la spec activa en `specs/` antes de tocar código.
- No añadas dependencias (en `pom.xml` o `package.json`) ni cambies el esquema de la base de datos sin actualizar antes la spec.
- No modifiques archivos dentro de `specs/` salvo petición explícita.

## Control de Versiones (Git / GitHub)
- Repositorio remoto en GitHub: `git@github.com:JuanValle5/bitacora-hidroelectrica.git`.
- Registrar cada corrección, mejora o funcionalidad mediante commits atómicos y claros siguiendo la convención de Conventional Commits (ej. `feat:`, `fix:`, `docs:`, `refactor:`, `chore:`).
- Al completar y validar cada tarea, enviar los cambios al repositorio remoto (`git push origin main` o la rama activa).
- Asegurarse de que el `.gitignore` proteja archivos de entorno con secretos (`.env`), carpetas de dependencias (`node_modules/`) y compilados (`target/`, `dist/`).

## Al terminar cualquier tarea
1. Ejecuta los tests del backend (`./mvnw test`) y del frontend (`npm run test`) y confirma en tu respuesta que ambos pasan correctamente y sin errores de tipado.
2. Realiza el commit descriptivo de los cambios y súbelo al repositorio remoto en GitHub (`git push origin <rama>`).