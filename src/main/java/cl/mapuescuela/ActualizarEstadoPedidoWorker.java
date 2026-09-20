package cl.mapuescuela;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

//Clase Actualizar estado se encarga de actualizar el estado de un pedido mediante el servicio REST, esto permitirá cambiar el estado de un pedido a Pagado, Cancelado, Etc.

public class ActualizarEstadoPedidoWorker {

private static final String API_PEDIDOS =
        System.getenv().getOrDefault(
                "MAPUESCUELA_API_URL",
                "http://localhost:8081/mapuescuela-api/webapi/pedidos"
        );

    public static int actualizarEstado(int idPedido, String nuevoEstado)
            throws Exception {

        URL url = new URL(API_PEDIDOS + "/" + idPedido + "/estado");
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();

        conexion.setRequestMethod("PUT");
        conexion.setRequestProperty("Content-Type", "application/json");
        conexion.setRequestProperty("Accept", "application/json");
        conexion.setDoOutput(true);

        String json = "{"
                + "\"estado\":\"" + nuevoEstado + "\""
                + "}";

        try (OutputStream os = conexion.getOutputStream()) {
            byte[] datos = json.getBytes(StandardCharsets.UTF_8);
            os.write(datos);
        }

        int codigoRespuesta = conexion.getResponseCode();

        System.out.println("Estado del pedido actualizado");
        System.out.println("Pedido ID: " + idPedido);
        System.out.println("Nuevo estado: " + nuevoEstado);
        System.out.println("Codigo HTTP: " + codigoRespuesta);

        conexion.disconnect();

        return codigoRespuesta;
    }
}