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

@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoResource {

    @PersistenceContext(unitName = "MapuescuelaPU")
    private EntityManager entityManager;

    @GET
    public Response listarProductos() {
        List<Producto> productos = entityManager
                .createQuery(
                        "SELECT p FROM Producto p ORDER BY p.id",
                        Producto.class
                )
                .getResultList();

        return Response.ok(productos).build();
    }

    @GET
    @Path("/{id}")
    public Response obtenerProducto(@PathParam("id") int id) {

        Producto producto = entityManager.find(Producto.class, id);

        if (producto == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"mensaje\":\"Producto no encontrado\"}")
                    .build();
        }

        return Response.ok(producto).build();
    }

    @POST
    @Transactional
    public Response crearProducto(Producto producto) {

        if (producto.getNombre() == null ||
                producto.getNombre().trim().isEmpty()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"mensaje\":\"Debe indicar el nombre del producto\"}")
                    .build();
        }

        if (producto.getPrecio() < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"mensaje\":\"El precio no puede ser negativo\"}")
                    .build();
        }

        if (producto.getStock() < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"mensaje\":\"El stock no puede ser negativo\"}")
                    .build();
        }

        producto.setActivo(true);

        entityManager.persist(producto);
        entityManager.flush();

        return Response.status(Response.Status.CREATED)
                .entity(producto)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response actualizarProducto(
            @PathParam("id") int id,
            Producto datos) {

        Producto producto = entityManager.find(Producto.class, id);

        if (producto == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"mensaje\":\"Producto no encontrado\"}")
                    .build();
        }

        if (datos.getNombre() != null &&
                !datos.getNombre().trim().isEmpty()) {

            producto.setNombre(datos.getNombre());
        }

        if (datos.getDescripcion() != null) {
            producto.setDescripcion(datos.getDescripcion());
        }

        if (datos.getPrecio() >= 0) {
            producto.setPrecio(datos.getPrecio());
        }

        if (datos.getStock() >= 0) {
            producto.setStock(datos.getStock());
        }

        producto.setActivo(datos.isActivo());

        entityManager.merge(producto);

        return Response.ok(producto).build();
    }

    @PUT
    @Path("/{id}/estado")
    @Transactional
    public Response cambiarEstado(
            @PathParam("id") int id,
            Producto datos) {

        Producto producto = entityManager.find(Producto.class, id);

        if (producto == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"mensaje\":\"Producto no encontrado\"}")
                    .build();
        }

        producto.setActivo(datos.isActivo());

        entityManager.merge(producto);

        return Response.ok(producto).build();
    }
}