# Constitución del Proyecto: Bitácora Hidroeléctrica

1. **Stack Estricto**: Backend exclusivo en Java 21/Spring Boot y Frontend en React/TypeScript con Tailwind CSS.
2. **Spec como Fuente de Verdad**: El código implementa la especificación (`specs/` y `docs/`); prohibido alterar esquemas o añadir dependencias sin actualizar la spec antes.
3. **Separación de Capas**: Backend (API REST) y Frontend (SPA) son totalmente independientes; la comunicación es exclusivamente mediante HTTP/JSON.
4. **Política de Tests**: Todo endpoint o componente nuevo requiere pruebas automatizadas que pasen exitosamente (`./mvnw test` y `npm run test`).
5. **Persistencia Relacional**: Los datos core se almacenan exclusivamente en PostgreSQL, gestionados a través de JPA/Hibernate.
6. **Idioma**: Código e identificadores (variables, métodos, endpoints, tablas) estrictamente en inglés; interfaz de usuario y mensajes en español.
