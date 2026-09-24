# Mapuescuela – Instrucciones de instalación y ejecución

Este proyecto integra una aplicación web con **Java/Jakarta REST**, **MySQL**, **Docker**, **Flowable** y **Cloudflare Tunnel** para ejecutar el proceso de compra, pago y entrega de pedidos del Grupo 4.

---

## 1. Requisitos

Antes de iniciar el proyecto se debe contar con:

- Git
- Docker Desktop
- Cloudflared
- Acceso a Flowable IPLACEX
- PowerShell
- Conexión a Internet

El repositorio debe estar descargado o clonado en el computador.

Ejemplo:

```powershell
git clone URL_DEL_REPOSITORIO
cd Mapuescuela

Si el proyecto ya existe localmente, actualizarlo con:

git pull
2. Configurar Flowable

Antes de levantar Docker se deben configurar las variables de entorno necesarias para que el backend pueda comunicarse con Flowable.

En PowerShell ejecutar:

$env:FLOWABLE_TOKEN="TOKEN_PERSONAL_DE_FLOWABLE"
$env:FLOWABLE_PROCESS_KEY="G4"

FLOWABLE_TOKEN corresponde al token personal generado desde Flowable.

FLOWABLE_PROCESS_KEY debe quedar configurado como:

G4
Verificar el token

Se puede comprobar que el token tenga acceso al Process API mediante:

curl.exe -sS -o "$env:TEMP\flowable_resp.txt" -w "HTTP=%{http_code}`n" -H "Authorization: Bearer $env:FLOWABLE_TOKEN" -H "Accept: application/json" "https://iplacex.cloud.flowable.com/sandbox/process-api/runtime/process-instances?size=1"

Si el token es correcto debe responder:

HTTP=200
3. Construir el proyecto con Docker
Primera ejecución

La primera vez que se utiliza el proyecto, o cuando se modifica el Dockerfile, dependencias o configuración de la imagen, se debe construir la imagen:

docker compose build --no-cache mapuescuela

Este comando se ejecuta una sola vez para construir la imagen.

Después se levantan los servicios:

docker compose up -d

Esto levantará principalmente:

mapuescuela-app
mapuescuela-mysql
Ejecuciones posteriores

Si el proyecto ya fue construido anteriormente, normalmente no es necesario volver a ejecutar build.

Solamente ejecutar:

docker compose up -d

En resumen:

Primera vez o cambios importantes:

docker compose build --no-cache mapuescuela
docker compose up -d

Uso normal:

docker compose up -d
4. Verificar Docker

Para comprobar que los contenedores están funcionando:

docker compose ps

Deben aparecer activos:

mapuescuela-app
mapuescuela-mysql

La aplicación utiliza:

Aplicación:
http://localhost:8081

MySQL:
localhost:3307

Administración GlassFish:
http://localhost:4849
5. Verificar la API

Para comprobar que el backend está funcionando:

Invoke-WebRequest -UseBasicParsing "http://localhost:8081/mapuescuela-api/webapi/productos"

La respuesta esperada es:

StatusCode : 200
6. Certificados HTTPS para Flowable

El Dockerfile configura automáticamente los certificados necesarios para que GlassFish pueda comunicarse con Flowable mediante HTTPS.

Se puede verificar con:

docker exec mapuescuela-app keytool -list -keystore /opt/glassfish8/glassfish/domains/domain1/config/cacerts.p12 -storetype PKCS12 -storepass changeit | Select-String "isrg-root"

Deben aparecer:

isrg-root-x1
isrg-root-x2

Por lo tanto, no es necesario copiar manualmente archivos de certificados al contenedor.

7. Levantar Cloudflare Tunnel

Flowable se encuentra en Internet y necesita acceder al backend que se ejecuta localmente.

Para permitir esta comunicación se utiliza Cloudflare Tunnel.

Abrir una segunda ventana de PowerShell y ejecutar:

cloudflared tunnel --url http://localhost:8081 --protocol http2

Cloudflare mostrará una URL similar a:

https://xxxxxxxxxxxxxxxx.trycloudflare.com

Esta ventana debe permanecer abierta mientras se esté utilizando el sistema.

Si se cierra Cloudflare, Flowable dejará de poder comunicarse con el backend local.

8. Actualizar apiBaseUrl en Flowable

Cada vez que se inicia un nuevo túnel de Cloudflare puede generarse una URL diferente.

Cuando esto ocurra se debe entrar a Flowable Design:

Flowable Design
→ mapuescuelaGrupo4
→ G4
→ Initialize variables

Buscar la variable:

apiBaseUrl

y reemplazar su valor por la nueva URL entregada por Cloudflare.

Ejemplo:

https://xxxxxxxxxxxxxxxx.trycloudflare.com

No agregar / al final de la URL.

Después realizar:

Save
↓
Publish

Es importante utilizar Publish, ya que solamente guardar los cambios no actualiza la versión que se ejecuta en Flowable Work.

9. Verificar Cloudflare

Se puede comprobar que Flowable podrá acceder al backend utilizando:

Invoke-WebRequest -UseBasicParsing "https://URL-CLOUDFLARE.trycloudflare.com/mapuescuela-api/webapi/productos"

Debe responder:

StatusCode : 200
10. Abrir la aplicación

El cliente web se encuentra disponible en:

http://localhost:8081/mapuescuela-api/cliente.html

Desde esta interfaz se puede crear un nuevo pedido.

Al crear el pedido ocurre automáticamente:

Cliente
↓
Frontend Mapuescuela
↓
Backend Java
↓
MySQL
↓
Se genera el pedido
↓
Backend obtiene el pedidoId
↓
Backend inicia el proceso G4
↓
Flowable Work

El usuario no necesita iniciar manualmente el proceso en Flowable.

11. Continuar el proceso en Flowable

Después de crear el pedido se debe ingresar a Flowable Work.

El proceso aparecerá como:

to-be

El flujo principal contempla:

Informar datos bancarios
↓
Adjuntar comprobante
↓
Revisar comprobante
↓
¿Pago aprobado?
↓
Confirmar pago
↓
Actualizar inventario
↓
Preparar pedido
↓
Retiro / Despacho
↓
Finalización

Las tareas automáticas se comunican con el backend mediante Cloudflare y actualizan la información almacenada en MySQL.

El pedidoId, cliente, producto, cantidad y modalidad de entrega son enviados automáticamente desde el backend hacia Flowable.

12. Detener el proyecto

Para detener los contenedores:

docker compose down

También se debe cerrar la ventana donde se está ejecutando Cloudflare Tunnel.

13. Volver a levantar el proyecto otro día

Cada vez que se cierre PowerShell, las variables configuradas mediante $env: se pierden.

Por lo tanto, primero se deben volver a configurar:

$env:FLOWABLE_TOKEN="TOKEN_PERSONAL_DE_FLOWABLE"
$env:FLOWABLE_PROCESS_KEY="G4"

Después levantar Docker:

docker compose up -d

Luego levantar nuevamente Cloudflare:

cloudflared tunnel --url http://localhost:8081 --protocol http2

Si Cloudflare entrega una URL diferente, actualizar nuevamente:

Flowable Design
→ mapuescuelaGrupo4
→ G4
→ Initialize variables
→ apiBaseUrl

y realizar:

Save → Publish

Finalmente abrir:

http://localhost:8081/mapuescuela-api/cliente.html
14. Resumen de inicio rápido
Primera vez
git pull

$env:FLOWABLE_TOKEN="TOKEN_PERSONAL_DE_FLOWABLE"
$env:FLOWABLE_PROCESS_KEY="G4"

docker compose build --no-cache mapuescuela
docker compose up -d

En otra ventana:

cloudflared tunnel --url http://localhost:8081 --protocol http2

Actualizar la URL de Cloudflare en:

G4 → Initialize variables → apiBaseUrl

y luego:

Save → Publish

Abrir:

http://localhost:8081/mapuescuela-api/cliente.html
Uso normal
$env:FLOWABLE_TOKEN="TOKEN_PERSONAL_DE_FLOWABLE"
$env:FLOWABLE_PROCESS_KEY="G4"

docker compose up -d

Después:

cloudflared tunnel --url http://localhost:8081 --protocol http2

Actualizar apiBaseUrl si cambió la URL de Cloudflare y comenzar a utilizar el sistema.