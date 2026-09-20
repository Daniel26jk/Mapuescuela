package cl.mapuescuela;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlowableGenerarPedidoWorker {

    private static final String FLOWABLE_URL =
            "https://iplacex.cloud.flowable.com/sandbox/external-job-api";

    private static final String TOKEN =
            System.getenv("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJmbG93YWJsZS1odWIiLCJzdWIiOiIwODU1NzQ0Yi04ZjhiLTExZjEtOTQxMy0yZTg3Yzc5MWY3OTEiLCJleHAiOjE3OTA4MjI2MjEsImlhdCI6MTc4ODIzMDYyMSwianRpIjoiZjE0MDk1M2EtYTVhZS0xMWYxLWJiMGEtZGFlZGZhM2EyY2NiIn0.LzReXvReMbxWgfkVh19JAdLfBE1HOVrIL3CBCYs61Ow");

    private static final String TOPIC =
            "generar-pedido";

    private static final String WORKER_ID =
            "mapuescuela-java-worker";

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println(" Mapuescuela - External Worker");
        System.out.println(" Topic: " + TOPIC);
        System.out.println(" Worker ID: " + WORKER_ID);
        System.out.println("======================================");

        try {

            if (TOKEN == null || TOKEN.trim().isEmpty()) {
                throw new Exception(
                        "No se encontro la variable de entorno FLOWABLE_TOKEN."
                );
            }

            adquirirYProcesarTrabajo();

        } catch (Exception e) {

            System.out.println("Error ejecutando el Worker:");
            e.printStackTrace();
        }
    }

    private static void adquirirYProcesarTrabajo()
            throws Exception {

        String respuestaFlowable =
                adquirirTrabajo();

        if (respuestaFlowable == null
                || respuestaFlowable.trim().isEmpty()
                || respuestaFlowable.trim().equals("[]")) {

            System.out.println(
                    "No existen trabajos generar-pedido pendientes."
            );

            return;
        }

        String jobId =
                extraerCampoString(
                        respuestaFlowable,
                        "id"
                );

        String cliente =
                extraerVariableString(
                        respuestaFlowable,
                        "cliente"
                );

        Integer productoId =
                extraerVariableEntera(
                        respuestaFlowable,
                        "productoId"
                );

        Integer cantidad =
                extraerVariableEntera(
                        respuestaFlowable,
                        "cantidad"
                );

        String modalidadEntrega =
                extraerVariableString(
                        respuestaFlowable,
                        "modalidadEntrega"
                );

        if (jobId == null) {
            throw new Exception(
                    "No se pudo obtener el ID del job."
            );
        }

        if (cliente == null
                || productoId == null
                || cantidad == null
                || modalidadEntrega == null) {

            throw new Exception(
                    "No se pudieron obtener todas las variables del proceso."
            );
        }

        if (productoId <= 0) {
            throw new Exception(
                    "productoId debe ser mayor que cero."
            );
        }

        if (cantidad <= 0) {
            throw new Exception(
                    "cantidad debe ser mayor que cero."
            );
        }

        System.out.println();
        System.out.println("Trabajo encontrado en Flowable.");
        System.out.println("Job ID: " + jobId);
        System.out.println("Cliente: " + cliente);
        System.out.println("Producto ID: " + productoId);
        System.out.println("Cantidad: " + cantidad);
        System.out.println("Modalidad: " + modalidadEntrega);

        GenerarPedidoWorker.ResultadoPedido resultado =
                GenerarPedidoWorker.generarPedidoConRespuesta(
                        cliente,
                        productoId,
                        cantidad,
                        modalidadEntrega
                );

        int codigoApi =
                resultado.getCodigoHttp();

        if (codigoApi < 200 || codigoApi >= 300) {

            throw new Exception(
                    "Mapuescuela API no pudo crear el pedido. HTTP "
                            + codigoApi
            );
        }

        String jsonPedido =
                resultado.getRespuestaJson();

        Integer pedidoId =
                extraerCampoEntero(
                        jsonPedido,
                        "id"
                );

        String estadoPedido =
                extraerCampoString(
                        jsonPedido,
                        "estado"
                );

        String nombreProducto =
                extraerCampoString(
                        jsonPedido,
                        "producto"
                );

        if (pedidoId == null) {

            throw new Exception(
                    "La API no devolvio el ID del pedido."
            );
        }

        if (estadoPedido == null) {

            throw new Exception(
                    "La API no devolvio el estado del pedido."
            );
        }

        System.out.println();
        System.out.println("Pedido creado correctamente.");
        System.out.println("Pedido ID: " + pedidoId);
        System.out.println("Producto: " + nombreProducto);
        System.out.println("Cantidad: " + cantidad);
        System.out.println("Estado: " + estadoPedido);

        completarTrabajo(
                jobId,
                pedidoId,
                productoId,
                cantidad,
                estadoPedido
        );

        System.out.println();
        System.out.println("======================================");
        System.out.println(" WORKER COMPLETADO CORRECTAMENTE");
        System.out.println(" Pedido ID: " + pedidoId);
        System.out.println(" Producto ID: " + productoId);
        System.out.println(" Cantidad: " + cantidad);
        System.out.println(" Estado: " + estadoPedido);
        System.out.println("======================================");
    }

    private static String adquirirTrabajo()
            throws Exception {

        URL url =
                new URL(
                        FLOWABLE_URL
                                + "/acquire/jobs"
                );

        HttpURLConnection conexion =
                (HttpURLConnection) url.openConnection();

        conexion.setRequestMethod("POST");

        conexion.setRequestProperty(
                "Authorization",
                "Bearer " + TOKEN
        );

        conexion.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        conexion.setRequestProperty(
                "Accept",
                "application/json"
        );

        conexion.setDoOutput(true);

        String json = "{"
                + "\"topic\":\"" + TOPIC + "\","
                + "\"workerId\":\"" + WORKER_ID + "\","
                + "\"lockDuration\":\"PT5M\","
                + "\"numberOfTasks\":1,"
                + "\"scopeType\":\"bpmn\""
                + "}";

        try (OutputStream os =
                     conexion.getOutputStream()) {

            os.write(
                    json.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        }

        int codigo =
                conexion.getResponseCode();

        InputStream stream;

        if (codigo >= 200 && codigo < 300) {
            stream = conexion.getInputStream();
        } else {
            stream = conexion.getErrorStream();
        }

        String respuesta =
                leerRespuesta(stream);

        conexion.disconnect();

        System.out.println(
                "Respuesta Flowable: HTTP " + codigo
        );

        if (codigo < 200 || codigo >= 300) {

            throw new Exception(
                    "Error Flowable HTTP "
                            + codigo
                            + ": "
                            + respuesta
            );
        }

        return respuesta;
    }

    private static void completarTrabajo(
            String jobId,
            int pedidoId,
            int productoId,
            int cantidad,
            String estadoPedido)
            throws Exception {

        URL url =
                new URL(
                        FLOWABLE_URL
                                + "/acquire/jobs/"
                                + jobId
                                + "/complete"
                );

        HttpURLConnection conexion =
                (HttpURLConnection) url.openConnection();

        conexion.setRequestMethod("POST");

        conexion.setRequestProperty(
                "Authorization",
                "Bearer " + TOKEN
        );

        conexion.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        conexion.setRequestProperty(
                "Accept",
                "application/json"
        );

        conexion.setDoOutput(true);

        String json = "{"
                + "\"workerId\":\""
                + WORKER_ID
                + "\","
                + "\"variables\":["

                + "{"
                + "\"name\":\"pedidoId\","
                + "\"type\":\"integer\","
                + "\"value\":"
                + pedidoId
                + "},"

                + "{"
                + "\"name\":\"productoId\","
                + "\"type\":\"integer\","
                + "\"value\":"
                + productoId
                + "},"

                + "{"
                + "\"name\":\"cantidad\","
                + "\"type\":\"integer\","
                + "\"value\":"
                + cantidad
                + "},"

                + "{"
                + "\"name\":\"estadoPedido\","
                + "\"type\":\"string\","
                + "\"value\":\""
                + escaparJson(estadoPedido)
                + "\""
                + "}"

                + "]"
                + "}";

        try (OutputStream os =
                     conexion.getOutputStream()) {

            os.write(
                    json.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        }

        int codigo =
                conexion.getResponseCode();

        InputStream stream;

        if (codigo >= 200 && codigo < 300) {
            stream = conexion.getInputStream();
        } else {
            stream = conexion.getErrorStream();
        }

        String respuesta =
                leerRespuesta(stream);

        conexion.disconnect();

        if (codigo < 200 || codigo >= 300) {

            throw new Exception(
                    "No fue posible completar el job. HTTP "
                            + codigo
                            + ": "
                            + respuesta
            );
        }

        System.out.println(
                "External Job completado en Flowable."
        );
    }

    private static String extraerCampoString(
            String json,
            String campo) {

        Pattern patron =
                Pattern.compile(
                        "\""
                                + Pattern.quote(campo)
                                + "\"\\s*:\\s*\"([^\"]*)\""
                );

        Matcher matcher =
                patron.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static Integer extraerCampoEntero(
            String json,
            String campo) {

        Pattern patron =
                Pattern.compile(
                        "\""
                                + Pattern.quote(campo)
                                + "\"\\s*:\\s*(\\d+)"
                );

        Matcher matcher =
                patron.matcher(json);

        if (matcher.find()) {

            return Integer.parseInt(
                    matcher.group(1)
            );
        }

        return null;
    }

    private static String extraerVariableString(
            String json,
            String nombreVariable) {

        Pattern patron =
                Pattern.compile(
                        "\"name\"\\s*:\\s*\""
                                + Pattern.quote(nombreVariable)
                                + "\""
                                + "\\s*,\\s*"
                                + "\"type\"\\s*:\\s*\"[^\"]*\""
                                + "\\s*,\\s*"
                                + "\"value\"\\s*:\\s*\"([^\"]*)\"",
                        Pattern.DOTALL
                );

        Matcher matcher =
                patron.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static Integer extraerVariableEntera(
            String json,
            String nombreVariable) {

        Pattern patronNumero =
                Pattern.compile(
                        "\"name\"\\s*:\\s*\""
                                + Pattern.quote(nombreVariable)
                                + "\""
                                + "\\s*,\\s*"
                                + "\"type\"\\s*:\\s*\"[^\"]*\""
                                + "\\s*,\\s*"
                                + "\"value\"\\s*:\\s*(\\d+)",
                        Pattern.DOTALL
                );

        Matcher matcherNumero =
                patronNumero.matcher(json);

        if (matcherNumero.find()) {

            return Integer.parseInt(
                    matcherNumero.group(1)
            );
        }

        Pattern patronTexto =
                Pattern.compile(
                        "\"name\"\\s*:\\s*\""
                                + Pattern.quote(nombreVariable)
                                + "\""
                                + "\\s*,\\s*"
                                + "\"type\"\\s*:\\s*\"[^\"]*\""
                                + "\\s*,\\s*"
                                + "\"value\"\\s*:\\s*\"(\\d+)\"",
                        Pattern.DOTALL
                );

        Matcher matcherTexto =
                patronTexto.matcher(json);

        if (matcherTexto.find()) {

            return Integer.parseInt(
                    matcherTexto.group(1)
            );
        }

        return null;
    }

    private static String leerRespuesta(
            InputStream stream)
            throws Exception {

        if (stream == null) {
            return "";
        }

        BufferedReader lector =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                StandardCharsets.UTF_8
                        )
                );

        String linea;

        StringBuilder respuesta =
                new StringBuilder();

        while ((linea = lector.readLine()) != null) {
            respuesta.append(linea);
        }

        lector.close();

        return respuesta.toString();
    }

    private static String escaparJson(
            String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}