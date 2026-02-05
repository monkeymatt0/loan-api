# Loan API

API REST para la gestión de solicitudes de préstamos personales desarrollada con Spring Boot.

## Instrucciones para Ejecutar el Proyecto

### Requisitos Previos

- **Java 11** o superior
- **Maven 3.6+**

### Pasos de Ejecución

1. **Compilar el proyecto**:
```bash
mvn clean install
```

2. **Ejecutar la aplicación**:
```bash
mvn spring-boot:run
```

3. **Verificar el inicio**:
   Al iniciar, la aplicación mostrará en la consola los tokens de autenticación:
```
=== Predefined Users ===
   CLIENTE Token: <token-generado>
   GESTORE Token: <token-generado>
========================
```

   Los tokens también se guardan en el archivo `tokens.txt` en la raíz del proyecto.

4. **Acceder a Swagger UI**:
   Una vez iniciada la aplicación, puedes acceder a la documentación interactiva de la API en:
   
   **http://localhost:8080/swagger-ui.html**
   
   Desde Swagger UI puedes:
   - Ver todos los endpoints disponibles
   - Probar las llamadas directamente desde el navegador
   - Ver ejemplos de payloads preconfigurados
   - Autenticarte usando el botón "Authorize" e insertando el token en formato: `Bearer <token>`

La aplicación estará disponible en **http://localhost:8080**

## Arquitectura y Decisiones Técnicas

### Arquitectura

La aplicación sigue una **arquitectura en capas** (Controller → Service → Repository) con separación clara de responsabilidades:

- **Controller Layer**: Gestiona las peticiones HTTP, validación de entrada y respuestas
- **Service Layer**: Contiene la lógica de negocio, validación de transiciones de estado, filtros y paginación
- **Repository Layer**: Abstrae la persistencia (actualmente en memoria con `ConcurrentHashMap`)
- **Mapper Layer**: Convierte entre DTOs (límites de la aplicación) y Modelos (dominio)
- **AOP Aspect**: Gestiona la autorización basada en roles mediante anotaciones

### Principios Arquitectónicos

- **Separación de responsabilidades**: Cada capa tiene una responsabilidad específica
- **DTOs en los límites**: Los DTOs se usan solo para entrada/salida, nunca dentro del dominio
- **Modelo protegido**: El modelo nunca se expone directamente al cliente
- **Inyección de dependencias**: Uso de constructores para la inyección de dependencias
- **AOP para cross-cutting concerns**: Autorización gestionada mediante Aspect

### Decisiones Técnicas

1. **Autenticación simplificada**: Implementación de autenticación mediante tokens Bearer con `UserContext` estático. Adecuado para prototipos, en producción se recomendaría JWT u OAuth2.

2. **Swagger/OpenAPI**: Integración de Swagger UI para documentación automática de la API y testing interactivo, facilitando la demostración y pruebas de los endpoints.

3. **AOP para autorización**: Uso de Aspect-Oriented Programming con la anotación `@RequiresRole` para gestionar la autorización de forma declarativa y flexible, separando las preocupaciones de autorización del código de negocio.

## Mejoras y Extensiones

### Mejoras Funcionales

1. **Persistencia con base de datos**: Reemplazar `ConcurrentHashMap` con JPA/Hibernate y una base de datos relacional (PostgreSQL/MySQL), incluyendo migraciones con Flyway/Liquibase.

2. **Autenticación JWT**: Implementar tokens JWT firmados en lugar de tokens simples, con soporte para refresh tokens y gestión de expiración y revocación.

3. **Filtros de búsqueda avanzados**: Añadir filtros por fecha, importe, currency, ordenamiento personalizable y búsqueda full-text.

4. **Audit trail**: Implementar trazabilidad de cambios (quién, cuándo, qué), historial de estados y logging de operaciones.

### Mejoras Técnicas/Arquitectónicas

1. **Cobertura de tests**: Aumentar la cobertura de tests unitarios e de integración, incluyendo tests end-to-end y de rendimiento.

2. **Logging y monitoring**: Implementar logging estructurado (Logback/Log4j2), métricas con Micrometer/Prometheus y health checks con Actuator.

3. **Caching**: Implementar caché para usuarios y solicitudes frecuentes, utilizando Redis para entornos distribuidos.

4. **Validación avanzada**: Crear validadores personalizados para reglas de negocio complejas.

5. **Versionado de API**: Implementar versionamiento de la API (`/api/v1/`, `/api/v2/`) con gestión de compatibilidad hacia atrás.

6. **Rate limiting**: Implementar limitación de peticiones por usuario/IP para protegerse de abusos.

7. **Refactorización del mapper**: Utilizar MapStruct en lugar de mappers estáticos para generar el código de mapeo en tiempo de compilación, mejorando el rendimiento y reduciendo código boilerplate.
