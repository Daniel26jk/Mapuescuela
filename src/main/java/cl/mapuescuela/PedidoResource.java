package cl.mapuescuela;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/pedidos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PedidoResource {

    @PersistenceContext(unitName = "MapuescuelaPU")
    private EntityManager entityManager;

    // Crear pedido
    @POST
    @Transactional
    public Response crearPedido(Pedido pedido) {

        pedido.setEstado("PENDIENTE_PAGO");

        entityManager.persist(pedido);
        entityManager.flush();

        return Response
                .status(Response.Status.CREATED)
                .entity(pedido)
                .build();
    }

    // Listar todos los pedidos
    @GET
    public Response listarPedidos() {

        List<Pedido> pedidos = entityManager
                .createQuery("SELECT p FROM Pedido p ORDER BY p.id", Pedido.class)
                .getResultList();

        return Response.ok(pedidos).build();
    }

    // Consultar pedido por ID
    @GET
    @Path("/{id}")
    public Response obtenerPedido(@PathParam("id") int id) {

        Pedido pedido = entityManager.find(Pedido.class, id);

        if (pedido == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("{\"mensaje\":\"Pedido no encontrado\"}")
                    .build();
        }

        return Response.ok(pedido).build();
    }

    // Actualizar estado de un pedido
    @PUT
    @Path("/{id}/estado")
    @Transactional
    public Response actualizarEstado(
            @PathParam("id") int id,
            Pedido datos) {

        Pedido pedido = entityManager.find(Pedido.class, id);

        if (pedido == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("{\"mensaje\":\"Pedido no encontrado\"}")
                    .build();
        }

        if (datos.getEstado() == null || datos.getEstado().trim().isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"mensaje\":\"Debe indicar el estado\"}")
                    .build();
        }

        pedido.setEstado(datos.getEstado());

        entityManager.merge(pedido);

        return Response.ok(pedido).build();
    }
}