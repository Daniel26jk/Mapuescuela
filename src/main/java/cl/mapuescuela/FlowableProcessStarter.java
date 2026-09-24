package cl.mapuescuela;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FlowableProcessStarter {

    private static final String FLOWABLE_PROCESS_API =
            obtenerConfiguracion(
                    "FLOWABLE_PROCESS_API",
                    "https://iplacex.cloud.flowable.com/sandbox/process-api"
            );

    private static final String FLOWABLE_TOKEN =
            System.getenv("FLOWABLE_TOKEN");

    private static final String PROCESS_KEY =
            System.getenv("FLOWABLE_PROCESS_KEY");

    public static void iniciarProceso(Pedido pedido) throws Exception {

        if (FLOWABLE_TOKEN == null ||
                FLOWABLE_TOKEN.trim().isEmpty()) {

            throw new Exception(
                    "No se encontro FLOWABLE_TOKEN."
            );
        }

        if (PROCESS_KEY == null ||
                PROCESS_KEY.trim().isEmpty()) {

            throw new Exception(
                    "No se encontro FLOWABLE_PROCESS_KEY."
            );
        }

        URL url = new URL(
                FLOWABLE_PROCESS_API
                        + "/runtime/process-instances"
        );

        HttpURLConnection conexion =
                (HttpURLConnection) url.openConnection();

        conexion.setRequestMethod("POST");

        conexion.setRequestProperty(
                "Authorization",
                "Bearer " + FLOWABLE_TOKEN.trim()
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

        String json =
                "{"
                        + "\"processDefinitionKey\":\""
                        + escapar(PROCESS_KEY)
                        + "\","

                        + "\"businessKey\":\"PEDIDO-"
                        + pedido.getId()
                        + "\","

                        + "\"variables\":["

                        + "{"
                        + "\"name\":\"pedidoId\","
                        + "\"type\":\"integer\","
                        + "\"value\":"
                        + pedido.getId()
                        + "},"

                        + "{"
                        + "\"name\":\"codigoPedido\","
                        + "\"type\":\"string\","
                        + "\"value\":\""
                        + escapar(pedido.getCodigoPedido())
                        + "\""
                        + "},"

                        + "{"
                        + "\"name\":\"cliente\","
                        + "\"type\":\"string\","
                        + "\"value\":\""
                        + escapar(pedido.getCliente())
                        + "\""
                        + "},"

                        + "{"
                        + "\"name\":\"productoId\","
                        + "\"type\":\"integer\","
                        + "\"value\":"
                        + pedido.getProductoId()
                        + "},"

                        + "{"
                        + "\"name\":\"producto\","
                        + "\"type\":\"string\","
                        + "\"value\":\""
                        + escapar(pedido.getProducto())
                        + "\""
                        + "},"

                        + "{"
                        + "\"name\":\"cantidad\","
                        + "\"type\":\"integer\","
                        + "\"value\":"
                        + pedido.getCantidad()
                        + "},"

                        + "{"
                        + "\"name\":\"modalidadEntrega\","
                        + "\"type\":\"string\","
                        + "\"value\":\""
                        + escapar(pedido.getModalidadEntrega())
                        + "\""
                        + "},"

                        + "{"
                        + "\"name\":\"estadoPedido\","
                        + "\"type\":\"string\","
                        + "\"value\":\""
                        + escapar(pedido.getEstado())
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

        System.out.println(
                "Inicio Flowable HTTP: " + codigo
        );

        System.out.println(
                "Respuesta Flowable: " + respuesta
        );

        if (codigo < 200 || codigo >= 300) {

            throw new Exception(
                    "No se pudo iniciar Flowable. HTTP "
                            + codigo
                            + ": "
                            + respuesta
            );
        }

        System.out.println(
                "Proceso Flowable iniciado para pedido "
                        + pedido.getId()
        );
    }

    private static String obtenerConfiguracion(
            String nombre,
            String defecto) {

        String valor =
                System.getenv(nombre);

        if (valor == null ||
                valor.trim().isEmpty()) {

            return defecto;
        }

        return valor.trim();
    }

    private static String escapar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static String leerRespuesta(
            InputStream stream) throws Exception {

        if (stream == null) {
            return "";
        }

        StringBuilder respuesta =
                new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     stream,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            String linea;

            while ((linea =
                    reader.readLine()) != null) {

                respuesta.append(linea);
            }
        }

        return respuesta.toString();
    }
}