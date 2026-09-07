# Inventario QR · avance académico

Aplicación web de inventario construida completamente con Spring Boot MVC y Thymeleaf. La navegación, los formularios y el CRUD se procesan en el servidor; el proyecto no necesita JavaScript para funcionar.

## Funcionalidades del avance

Implementadas:

- Dashboard con productos registrados, unidades disponibles y alertas de stock bajo.
- Catálogo con búsqueda, creación, consulta, edición y eliminación de productos.
- Vista básica de inventario y niveles mínimos.
- Generación y descarga de códigos QR en PNG desde Spring Boot.
- Vistas demostrativas de pedidos y reportes.
- Interfaz responsive renderizada con Thymeleaf.

Planificadas para próximas etapas:

- Entradas, salidas e historial de movimientos.
- Lectura y validación de códigos QR.
- Flujo completo de pedidos y reportes exportables.
- Persistencia real de productos, usuarios y autenticación.

Los productos se almacenan temporalmente en memoria y recuperan sus datos de demostración cuando se reinicia la aplicación. Se conserva la entidad `Usuario` aportada en el repositorio original; H2 permite iniciar el avance sin instalar PostgreSQL, y el driver PostgreSQL queda disponible para una etapa posterior.

## Requisitos

- JDK 21
- Maven Wrapper incluido o Maven 3.9+

## Ejecución local

En Windows PowerShell:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.3.9-hotspot'
.\mvnw.cmd clean package
java -jar target\app.jar
```

Abre `http://localhost:8080`.

## Ejecución con Docker

```bash
docker compose up --build
```

La imagen usa JDK 21, compila y ejecuta las pruebas durante la etapa de construcción y levanta la aplicación en `http://localhost:8080`. No requiere un contenedor de base de datos para este avance.
