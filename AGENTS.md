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

## Al terminar cualquier tarea
- Ejecuta los tests del backend (`./mvnw test`) y del frontend (`npm run test`) y confirma en tu respuesta que ambos pasan correctamente y sin errores de tipado.