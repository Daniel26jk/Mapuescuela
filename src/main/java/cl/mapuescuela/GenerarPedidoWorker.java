package cl.mapuescuela;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

//La clase generar pedido generará un nuevo pedido mediante la web service, esta recibirá los datos del cliente, producto, y modalidad de entrega, la peticion de la clase es de tipo POST.

public class GenerarPedidoWorker {

    private static final String API_PEDIDOS =
            "http://localhost:8080/mapuescuela-api/webapi/pedidos";

    public static int generarPedido(String cliente, String producto, String modalidadEntrega)
            throws Exception {

        URL url = new URL(API_PEDIDOS);
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();

        conexion.setRequestMethod("POST");
        conexion.setRequestProperty("Content-Type", "application/json");
        conexion.setRequestProperty("Accept", "application/json");
        conexion.setDoOutput(true);

        String json = "{"
                + "\"cliente\":\"" + cliente + "\","
                + "\"producto\":\"" + producto + "\","
                + "\"modalidadEntrega\":\"" + modalidadEntrega + "\""
                + "}";

        try (OutputStream os = conexion.getOutputStream()) {
            byte[] datos = json.getBytes(StandardCharsets.UTF_8);
            os.write(datos);
        }

        int codigoRespuesta = conexion.getResponseCode();

        System.out.println("Pedido enviado a Mapuescuela API");
        System.out.println("Codigo HTTP: " + codigoRespuesta);

        conexion.disconnect();

        return codigoRespuesta;
    }
}