package cl.mapuescuela;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FlowableGenerarPedidoWorker {

    private static final String FLOWABLE_URL =
            "https://iplacex.cloud.flowable.com/sandbox/external-job-api";


//token momentaneo para acceso y pruebas de la evaluacion 2
    private static final String TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJmbG93YWJsZS1odWIiLCJzdWIiOiIwODU1NzQ0Yi04ZjhiLTExZjEtOTQxMy0yZTg3Yzc5MWY3OTEiLCJleHAiOjE3OTA4MjI2MjEsImlhdCI6MTc4ODIzMDYyMSwianRpIjoiZjE0MDk1M2EtYTVhZS0xMWYxLWJiMGEtZGFlZGZhM2EyY2NiIn0.LzReXvReMbxWgfkVh19JAdLfBE1HOVrIL3CBCYs61Ow";

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
            adquirirTrabajo();
        } catch (Exception e) {
            System.out.println("Error al conectar con Flowable:");
            e.printStackTrace();
        }
    }

    private static void adquirirTrabajo() throws Exception {

        URL url = new URL(FLOWABLE_URL + "/acquire/jobs");

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
                + "\"lockDuration\":\"PT1M\","
                + "\"numberOfTasks\":1"
                + "}";

        try (OutputStream os = conexion.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int codigo = conexion.getResponseCode();

        System.out.println("Respuesta Flowable: HTTP " + codigo);

        InputStream stream;

        if (codigo >= 200 && codigo < 300) {
            stream = conexion.getInputStream();
        } else {
            stream = conexion.getErrorStream();
        }

        if (stream != null) {

            BufferedReader lector =
                    new BufferedReader(
                            new InputStreamReader(
                                    stream,
                                    StandardCharsets.UTF_8
                            )
                    );

            String linea;
            StringBuilder respuesta = new StringBuilder();

            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }

            lector.close();

            System.out.println("Respuesta:");
            System.out.println(respuesta.toString());

        } else {

            System.out.println(
                    "Flowable no devolvio contenido adicional."
            );
        }

        if (codigo >= 200 && codigo < 300) {
            System.out.println(
                    "Conexion con Flowable realizada correctamente."
            );
        }

        conexion.disconnect();
    }
}