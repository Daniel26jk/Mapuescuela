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

public class FlowableInventarioWorker {

    private static final String FLOWABLE_URL =
            "https://iplacex.cloud.flowable.com/sandbox/external-job-api";
private static final String API_PEDIDOS =
        System.getenv().getOrDefault(
                "MAPUESCUELA_API_URL",
                "http://localhost:8081/mapuescuela-api/webapi/pedidos"
        );
    private static final String TOKEN =
            System.getenv("FLOWABLE_TOKEN");

private static final String TOPIC =
        "g4-actualizar-inventario";
        
    private static final String WORKER_ID =
            "mapuescuela-java-worker";


    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println(" Mapuescuela - Worker Inventario");
        System.out.println(" Topic: " + TOPIC);
        System.out.println(" Worker ID: " + WORKER_ID);
        System.out.println("======================================");

        try {

            if (TOKEN == null ||
                    TOKEN.trim().isEmpty()) {

                throw new Exception(
                        "No se encontro la variable de entorno FLOWABLE_TOKEN."
                );
            }

            procesarTrabajo();

        } catch (Exception e) {

            System.out.println(
                    "Error ejecutando Worker:"
            );

            e.printStackTrace();
        }
    }


    private static void procesarTrabajo()
            throws Exception {

        String respuesta =
                adquirirTrabajo();


        if (respuesta == null ||
                respuesta.trim().isEmpty() ||
                respuesta.trim().equals("[]")) {

            System.out.println(
                    "No existen trabajos pendientes para "
                            + TOPIC
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


        System.out.println(
                "Job ID: " + jobId
        );

        System.out.println(
                "Pedido ID: " + pedidoId
        );


        int codigoApi =
                actualizarInventario(
                        pedidoId
                );


        if (codigoApi < 200 ||
                codigoApi >= 300) {

            throw new Exception(
                    "No se pudo actualizar el inventario. HTTP "
                            + codigoApi
            );
        }


        completarTrabajo(
                jobId
        );


        System.out.println("======================================");
        System.out.println(" WORKER COMPLETADO");
        System.out.println(" Pedido: " + pedidoId);
        System.out.println(" Inventario actualizado correctamente");
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
                (HttpURLConnection)
                        url.openConnection();


        conexion.setRequestMethod(
                "POST"
        );


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


        conexion.setDoOutput(
                true
        );


        String json =
                "{"
                        + "\"topic\":\""
                        + TOPIC
                        + "\","
                        + "\"workerId\":\""
                        + WORKER_ID
                        + "\","
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

        if (codigo >= 200 &&
                codigo < 300) {

            stream =
                    conexion.getInputStream();

        } else {

            stream =
                    conexion.getErrorStream();
        }


        String respuesta =
                leerRespuesta(
                        stream
                );


        conexion.disconnect();


        System.out.println(
                "Respuesta Flowable: HTTP "
                        + codigo
        );


        if (codigo < 200 ||
                codigo >= 300) {

            throw new Exception(
                    "Error Flowable HTTP "
                            + codigo
                            + ": "
                            + respuesta
            );
        }


        return respuesta;
    }


    private static int actualizarInventario(
            int pedidoId)
            throws Exception {

        URL url =
                new URL(
                        API_PEDIDOS
                                + "/"
                                + pedidoId
                                + "/inventario"
                );


        HttpURLConnection conexion =
                (HttpURLConnection)
                        url.openConnection();


        conexion.setRequestMethod(
                "PUT"
        );


        conexion.setRequestProperty(
                "Accept",
                "application/json"
        );


        int codigo =
                conexion.getResponseCode();


        InputStream stream;

        if (codigo >= 200 &&
                codigo < 300) {

            stream =
                    conexion.getInputStream();

        } else {

            stream =
                    conexion.getErrorStream();
        }


        String respuesta =
                leerRespuesta(
                        stream
                );


        conexion.disconnect();


        System.out.println(
                "Actualizar inventario HTTP: "
                        + codigo
        );


        System.out.println(
                "Respuesta API: "
                        + respuesta
        );


        return codigo;
    }


    private static void completarTrabajo(
            String jobId)
            throws Exception {

        URL url =
                new URL(
                        FLOWABLE_URL
                                + "/acquire/jobs/"
                                + jobId
                                + "/complete"
                );


        HttpURLConnection conexion =
                (HttpURLConnection)
                        url.openConnection();


        conexion.setRequestMethod(
                "POST"
        );


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


        conexion.setDoOutput(
                true
        );


        String json =
                "{"
                        + "\"workerId\":\""
                        + WORKER_ID
                        + "\","
                        + "\"variables\":["
                        + "{"
                        + "\"name\":\"inventarioActualizado\","
                        + "\"type\":\"boolean\","
                        + "\"value\":true"
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

        if (codigo >= 200 &&
                codigo < 300) {

            stream =
                    conexion.getInputStream();

        } else {

            stream =
                    conexion.getErrorStream();
        }


        String respuesta =
                leerRespuesta(
                        stream
                );


        conexion.disconnect();


        if (codigo < 200 ||
                codigo >= 300) {

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

        Pattern patronNumero =
                Pattern.compile(
                        "\"name\"\\s*:\\s*\""
                                + Pattern.quote(nombreVariable)
                                + "\".*?"
                                + "\"value\"\\s*:\\s*(\\d+)",
                        Pattern.DOTALL
                );


        Matcher matcherNumero =
                patronNumero.matcher(
                        json
                );


        if (matcherNumero.find()) {

            return Integer.parseInt(
                    matcherNumero.group(1)
            );
        }


        Pattern patronTexto =
                Pattern.compile(
                        "\"name\"\\s*:\\s*\""
                                + Pattern.quote(nombreVariable)
                                + "\".*?"
                                + "\"value\"\\s*:\\s*\"(\\d+)\"",
                        Pattern.DOTALL
                );


        Matcher matcherTexto =
                patronTexto.matcher(
                        json
                );


        if (matcherTexto.find()) {

            return Integer.parseInt(
                    matcherTexto.group(1)
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
                patron.matcher(
                        json
                );


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


        StringBuilder respuesta =
                new StringBuilder();


        String linea;


        while ((linea =
                lector.readLine()) != null) {

            respuesta.append(
                    linea
            );
        }


        lector.close();


        return respuesta.toString();
    }
}