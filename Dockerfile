# 1. Etapa de compilación (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar archivos de dependencias para aprovechar la caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn package -DskipTests

# 2. Etapa de ejecución (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar solo el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto por defecto de Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]