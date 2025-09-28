# 1. Etapa de build con Maven + JDK 21
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Copia POM y código fuente
COPY pom.xml mvnw ./
COPY src ./src

# Construye el JAR sin tests para acelerar
RUN mvn clean package -DskipTests

# 2. Etapa de runtime con JRE 21 Alpine
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Copia el JAR generado
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar

# Expone el puerto de la app (ajusta si usas otro)
EXPOSE 8080

# Arranque del contenedor
ENTRYPOINT ["java","-jar","/app/app.jar"]
