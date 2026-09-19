# =========================================================
# ETAPA 1 - COMPILAR MAPUESCUELA
# =========================================================

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


# =========================================================
# ETAPA 2 - JAVA + GLASSFISH 8.0.4
# =========================================================

FROM eclipse-temurin:21-jdk

USER root

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt

# GlassFish 8.0.4 oficial
RUN curl -fL \
    https://download.eclipse.org/ee4j/glassfish/glassfish-8.0.4.zip \
    -o glassfish.zip \
    && unzip glassfish.zip \
    && rm glassfish.zip

ENV PATH="/opt/glassfish8/bin:${PATH}"

# Mejora el arranque de GlassFish 8.0.4
ENV AS_HOSTNAME=localhost

RUN mkdir -p /opt/mapuescuela

# Aplicación
COPY --from=build \
    /app/target/mapuescuela-api.war \
    /opt/mapuescuela/mapuescuela-api.war

# Driver MySQL
COPY --from=build \
    /root/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar \
    /opt/glassfish8/glassfish/domains/domain1/lib/mysql-connector-j.jar

# Script de inicio
COPY docker/glassfish/start.sh \
    /opt/mapuescuela/start.sh

RUN chmod +x /opt/mapuescuela/start.sh

EXPOSE 8080
EXPOSE 4848

ENTRYPOINT ["/opt/mapuescuela/start.sh"]