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

# curl/unzip para GlassFish
# ca-certificates para certificados públicos (incluye ISRG)
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        curl \
        unzip \
        ca-certificates \
    && update-ca-certificates \
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


# =========================================================
# CERTIFICADOS HTTPS PARA FLOWABLE
# =========================================================
#
# GlassFish utiliza su propio truststore:
# /opt/glassfish8/glassfish/domains/domain1/config/cacerts.p12
#
# Se agregan las raíces ISRG utilizadas por Let's Encrypt,
# necesarias para que Java pueda consumir Flowable vía HTTPS.
#
# Esto evita el error:
# PKIX path building failed
# =========================================================

RUN keytool -importcert \
        -noprompt \
        -trustcacerts \
        -alias isrg-root-x1 \
        -file /etc/ssl/certs/ISRG_Root_X1.pem \
        -keystore /opt/glassfish8/glassfish/domains/domain1/config/cacerts.p12 \
        -storetype PKCS12 \
        -storepass changeit \
    && keytool -importcert \
        -noprompt \
        -trustcacerts \
        -alias isrg-root-x2 \
        -file /etc/ssl/certs/ISRG_Root_X2.pem \
        -keystore /opt/glassfish8/glassfish/domains/domain1/config/cacerts.p12 \
        -storetype PKCS12 \
        -storepass changeit


# =========================================================
# MAPUESCUELA
# =========================================================

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