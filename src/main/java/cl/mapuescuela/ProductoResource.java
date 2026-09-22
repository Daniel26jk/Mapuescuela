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
                .createQuery("SELECT p FROM Producto p ORDER BY p.id", Producto.class)
                .getResultList();

        return Response.ok(productos).build();
    }

    @GET
    @Path("/{id}")
    public Response obtenerProducto(@PathParam("id") int id) {
        Producto producto = entityManager.find(Producto.class, id);

        if (producto == null) {
            return error(Response.Status.NOT_FOUND, "Producto no encontrado");
        }

        return Response.ok(producto).build();
    }

    @POST
    @Transactional
    public Response crearProducto(Producto producto) {
        String validacion = validarProducto(producto);
        if (validacion != null) {
            return error(Response.Status.BAD_REQUEST, validacion);
        }

        producto.setNombre(producto.getNombre().trim());
        producto.setDescripcion(limpiar(producto.getDescripcion()));
        producto.setCategoria(limpiar(producto.getCategoria()));
        producto.setImagenUrl(limpiar(producto.getImagenUrl()));
        producto.setActivo(true);

        entityManager.persist(producto);
        entityManager.flush();

        return Response.status(Response.Status.CREATED).entity(producto).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response actualizarProducto(@PathParam("id") int id, Producto datos) {
        Producto producto = entityManager.find(Producto.class, id);

        if (producto == null) {
            return error(Response.Status.NOT_FOUND, "Producto no encontrado");
        }

        String validacion = validarProducto(datos);
        if (validacion != null) {
            return error(Response.Status.BAD_REQUEST, validacion);
        }

        producto.setNombre(datos.getNombre().trim());
        producto.setDescripcion(limpiar(datos.getDescripcion()));
        producto.setCategoria(limpiar(datos.getCategoria()));
        producto.setImagenUrl(limpiar(datos.getImagenUrl()));
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());
        producto.setActivo(datos.isActivo());

        entityManager.merge(producto);
        entityManager.flush();

        return Response.ok(producto).build();
    }

    @PUT
    @Path("/{id}/estado")
    @Transactional
    public Response cambiarEstado(@PathParam("id") int id, Producto datos) {
        Producto producto = entityManager.find(Producto.class, id);

        if (producto == null) {
            return error(Response.Status.NOT_FOUND, "Producto no encontrado");
        }

        producto.setActivo(datos.isActivo());
        entityManager.merge(producto);
        entityManager.flush();

        return Response.ok(producto).build();
    }

    private String validarProducto(Producto producto) {
        if (producto == null || producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            return "Debe indicar el nombre del producto";
        }
        if (producto.getPrecio() < 0) {
            return "El precio no puede ser negativo";
        }
        if (producto.getStock() < 0) {
            return "El stock no puede ser negativo";
        }
        return null;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado)
                .entity("{\"mensaje\":\"" + mensaje.replace("\"", "'") + "\"}")
                .build();
    }
}
