#!/bin/bash

set -e

echo "======================================"
echo " Mapuescuela - Inicio GlassFish"
echo "======================================"

ASADMIN="/opt/glassfish8/bin/asadmin"
DOMAIN="domain1"

DB_HOST="${DB_HOST:-mysql}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-mapuescuela}"
DB_USER="${DB_USER:-mapuescuela}"
DB_PASSWORD="${DB_PASSWORD:-mapuescuela123}"

WAR="/opt/mapuescuela/mapuescuela-api.war"

echo
echo "Base de datos:"
echo " Host: ${DB_HOST}"
echo " Puerto: ${DB_PORT}"
echo " Base: ${DB_NAME}"
echo " Usuario: ${DB_USER}"

echo
echo "Iniciando GlassFish..."

$ASADMIN start-domain "$DOMAIN"

echo
echo "Esperando GlassFish..."

sleep 8

echo
echo "Eliminando configuración JDBC anterior si existe..."

$ASADMIN delete-jdbc-resource \
    jdbc/MapuescuelaDS \
    >/dev/null 2>&1 || true

$ASADMIN delete-jdbc-connection-pool \
    MapuescuelaPool \
    >/dev/null 2>&1 || true

echo
echo "Creando pool JDBC MapuescuelaPool..."

$ASADMIN create-jdbc-connection-pool \
    --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
    --restype javax.sql.DataSource \
    --property "serverName=${DB_HOST}:portNumber=${DB_PORT}:databaseName=${DB_NAME}:user=${DB_USER}:password=${DB_PASSWORD}:sslMode=DISABLED:allowPublicKeyRetrieval=true" \
    MapuescuelaPool

echo
echo "Creando recurso jdbc/MapuescuelaDS..."

$ASADMIN create-jdbc-resource \
    --connectionpoolid MapuescuelaPool \
    jdbc/MapuescuelaDS

echo
echo "Probando conexión con MySQL..."

$ASADMIN ping-connection-pool \
    MapuescuelaPool

echo
echo "Eliminando despliegue anterior si existe..."

$ASADMIN undeploy \
    mapuescuela-api \
    >/dev/null 2>&1 || true

echo
echo "Desplegando Mapuescuela..."

$ASADMIN deploy \
    --name mapuescuela-api \
    --contextroot mapuescuela-api \
    "$WAR"

echo
echo "======================================"
echo " MAPUESCUELA INICIADO CORRECTAMENTE"
echo "======================================"

echo
echo "Aplicación Docker:"
echo "http://localhost:8081/mapuescuela-api/"

echo
echo "Manteniendo GlassFish en ejecución..."

$ASADMIN stop-domain "$DOMAIN"

exec $ASADMIN start-domain \
    --verbose \
    "$DOMAIN"