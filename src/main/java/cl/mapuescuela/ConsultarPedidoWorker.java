package cl.mapuescuela;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//Clase consultar pedido consulta un pedido existente en el Web Service, obtiene el ID y realiza una petición GET.

public class ConsultarPedidoWorker {

    private static final String API_PEDIDOS =
            "http://localhost:8080/mapuescuela-api/webapi/pedidos";

    public static int consultarPedido(int idPedido) throws Exception {

        URL url = new URL(API_PEDIDOS + "/" + idPedido);
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();

        conexion.setRequestMethod("GET");
        conexion.setRequestProperty("Accept", "application/json");

        int codigoRespuesta = conexion.getResponseCode();

        System.out.println("Consulta de pedido");
        System.out.println("Pedido ID: " + idPedido);
        System.out.println("Codigo HTTP: " + codigoRespuesta);

        if (codigoRespuesta == HttpURLConnection.HTTP_OK) {

            BufferedReader lector = new BufferedReader(
                    new InputStreamReader(conexion.getInputStream())
            );

            String linea;
            StringBuilder respuesta = new StringBuilder();

            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }

            lector.close();

            System.out.println("Respuesta:");
            System.out.println(respuesta.toString());
        }

        conexion.disconnect();

        return codigoRespuesta;
    }
}