# Build stage (Java 21 para máxima compatibilidad en cloud)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar archivos de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Compilar
RUN ./mvnw package -DskipTests -Djava.version=21 -B

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

# Puerto por defecto (Render/Railway/Fly inyectan PORT)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
