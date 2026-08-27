package cl.mapuescuela;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/pedidos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PedidoResource {

    private static final List<Pedido> pedidos = new ArrayList<>();
    private static int siguienteId = 1;

    // Obtener todos los pedidos
    @GET
    public List<Pedido> obtenerPedidos() {
        return pedidos;
    }

    // Obtener un pedido por ID
    @GET
    @Path("/{id}")
    public Response obtenerPedido(@PathParam("id") int id) {

        for (Pedido pedido : pedidos) {
            if (pedido.getId() == id) {
                return Response.ok(pedido).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity("Pedido no encontrado")
                .build();
    }

    // Crear un nuevo pedido
    @POST
    public Response crearPedido(Pedido pedido) {

        pedido.setId(siguienteId++);
        pedido.setEstado("PENDIENTE_PAGO");

        pedidos.add(pedido);

        return Response.status(Response.Status.CREATED)
                .entity(pedido)
                .build();
    }

    // Actualizar el estado de un pedido
    @PUT
    @Path("/{id}/estado")
    public Response actualizarEstado(
            @PathParam("id") int id,
            Pedido datos) {

        for (Pedido pedido : pedidos) {

            if (pedido.getId() == id) {

                pedido.setEstado(datos.getEstado());

                return Response.ok(pedido).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity("Pedido no encontrado")
                .build();
    }
}