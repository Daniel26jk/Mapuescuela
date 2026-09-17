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

public class FlowableEstadoPedidoWorker {

    private static final String FLOWABLE_URL =
            "https://iplacex.cloud.flowable.com/sandbox/external-job-api";

    private static final String TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJmbG93YWJsZS1odWIiLCJzdWIiOiIwODU1NzQ0Yi04ZjhiLTExZjEtOTQxMy0yZTg3Yzc5MWY3OTEiLCJleHAiOjE3OTA4MjI2MjEsImlhdCI6MTc4ODIzMDYyMSwianRpIjoiZjE0MDk1M2EtYTVhZS0xMWYxLWJiMGEtZGFlZGZhM2EyY2NiIn0.LzReXvReMbxWgfkVh19JAdLfBE1HOVrIL3CBCYs61Ow";

    private static final String WORKER_ID =
            "mapuescuela-java-worker";

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println(
                    "Uso: FlowableEstadoPedidoWorker <topic> <estado>"
            );
            return;
        }

        String topic = args[0];
        String nuevoEstado = args[1];

        System.out.println("======================================");
        System.out.println(" Mapuescuela - Worker Estado");
        System.out.println(" Topic: " + topic);
        System.out.println(" Estado: " + nuevoEstado);
        System.out.println("======================================");

        try {
            procesarTrabajo(topic, nuevoEstado);
        } catch (Exception e) {
            System.out.println("Error ejecutando Worker:");
            e.printStackTrace();
        }
    }

    private static void procesarTrabajo(
            String topic,
            String nuevoEstado)
            throws Exception {

        String respuesta =
                adquirirTrabajo(topic);

        if (respuesta == null
                || respuesta.trim().isEmpty()
                || respuesta.trim().equals("[]")) {

            System.out.println(
                    "No existen trabajos pendientes para: " + topic
            );

            return;
        }

        String jobId =
                extraerCampoString(
                        respuesta,
                        "id"
                );

        Integer pedidoId =
                extraerVariableEntera(
                        respuesta,
                        "pedidoId"
                );

        if (jobId == null) {
            throw new Exception(
                    "No se pudo obtener el ID del job."
            );
        }

        if (pedidoId == null) {
            throw new Exception(
                    "No se pudo obtener pedidoId desde Flowable."
            );
        }

        System.out.println("Job ID: " + jobId);
        System.out.println("Pedido ID: " + pedidoId);

        int codigo =
                ActualizarEstadoPedidoWorker.actualizarEstado(
                        pedidoId,
                        nuevoEstado
                );

        if (codigo < 200 || codigo >= 300) {
            throw new Exception(
                    "No se pudo actualizar el pedido. HTTP " + codigo
            );
        }

        completarTrabajo(
                jobId,
                nuevoEstado
        );

        System.out.println("======================================");
        System.out.println(" WORKER COMPLETADO");
        System.out.println(" Pedido: " + pedidoId);
        System.out.println(" Estado: " + nuevoEstado);
        System.out.println("======================================");
    }

    private static String adquirirTrabajo(
            String topic)
            throws Exception {

        URL url =
                new URL(
                        FLOWABLE_URL + "/acquire/jobs"
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
                + "\"topic\":\"" + topic + "\","
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
            String nuevoEstado)
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
                + "\"name\":\"estadoPedido\","
                + "\"type\":\"string\","
                + "\"value\":\""
                + nuevoEstado
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
                    "No se pudo completar el job. HTTP "
                            + codigo
                            + ": "
                            + respuesta
            );
        }

        System.out.println(
                "External Job completado en Flowable."
        );
    }

    private static Integer extraerVariableEntera(
            String json,
            String nombreVariable) {

        Pattern patron =
                Pattern.compile(
                        "\"name\"\\s*:\\s*\""
                                + Pattern.quote(nombreVariable)
                                + "\".*?"
                                + "\"value\"\\s*:\\s*(\\d+)",
                        Pattern.DOTALL
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
}
