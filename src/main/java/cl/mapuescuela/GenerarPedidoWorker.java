package cl.mapuescuela;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GenerarPedidoWorker {

    private static final String API_PEDIDOS =
            "http://localhost:8080/mapuescuela-api/webapi/pedidos";

    public static class ResultadoPedido {

        private final int codigoHttp;
        private final String respuestaJson;

        public ResultadoPedido(int codigoHttp, String respuestaJson) {
            this.codigoHttp = codigoHttp;
            this.respuestaJson = respuestaJson;
        }

        public int getCodigoHttp() {
            return codigoHttp;
        }

        public String getRespuestaJson() {
            return respuestaJson;
        }
    }

    public static int generarPedido(
            String cliente,
            String producto,
            String modalidadEntrega) throws Exception {

        ResultadoPedido resultado =
                generarPedidoConRespuesta(
                        cliente,
                        producto,
                        modalidadEntrega
                );

        return resultado.getCodigoHttp();
    }

    public static ResultadoPedido generarPedidoConRespuesta(
            String cliente,
            String producto,
            String modalidadEntrega) throws Exception {

        URL url = new URL(API_PEDIDOS);

        HttpURLConnection conexion =
                (HttpURLConnection) url.openConnection();

        conexion.setRequestMethod("POST");
        conexion.setRequestProperty("Content-Type", "application/json");
        conexion.setRequestProperty("Accept", "application/json");
        conexion.setDoOutput(true);

        String json = "{"
                + "\"cliente\":\"" + escaparJson(cliente) + "\","
                + "\"producto\":\"" + escaparJson(producto) + "\","
                + "\"modalidadEntrega\":\""
                + escaparJson(modalidadEntrega) + "\""
                + "}";

        try (OutputStream os = conexion.getOutputStream()) {
            byte[] datos = json.getBytes(StandardCharsets.UTF_8);
            os.write(datos);
        }

        int codigoRespuesta = conexion.getResponseCode();

        InputStream stream;

        if (codigoRespuesta >= 200 && codigoRespuesta < 300) {
            stream = conexion.getInputStream();
        } else {
            stream = conexion.getErrorStream();
        }

        String respuestaJson = leerRespuesta(stream);

        System.out.println("Pedido enviado a Mapuescuela API");
        System.out.println("Codigo HTTP: " + codigoRespuesta);
        System.out.println("Respuesta: " + respuestaJson);

        conexion.disconnect();

        return new ResultadoPedido(
                codigoRespuesta,
                respuestaJson
        );
    }

    private static String leerRespuesta(InputStream stream)
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
        StringBuilder respuesta = new StringBuilder();

        while ((linea = lector.readLine()) != null) {
            respuesta.append(linea);
        }

        lector.close();

        return respuesta.toString();
    }

    private static String escaparJson(String texto) {

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