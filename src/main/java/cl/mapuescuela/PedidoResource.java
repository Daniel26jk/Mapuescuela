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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.text.Normalizer;
import java.util.List;

@Path("/pedidos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PedidoResource {

    @PersistenceContext(unitName = "MapuescuelaPU")
    private EntityManager entityManager;


    /* =========================================================
       CREAR PEDIDO
       ========================================================= */

    @POST
    @Transactional
    public Response crearPedido(Pedido pedido) {

        if (pedido.getCliente() == null ||
                pedido.getCliente().trim().isEmpty()) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            "{\"mensaje\":\"Debe indicar el cliente\"}"
                    )
                    .build();
        }


        if (pedido.getProductoId() <= 0) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            "{\"mensaje\":\"Debe indicar un producto válido\"}"
                    )
                    .build();
        }


        if (pedido.getCantidad() <= 0) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            "{\"mensaje\":\"La cantidad debe ser mayor que cero\"}"
                    )
                    .build();
        }


        Producto producto =
                entityManager.find(
                        Producto.class,
                        pedido.getProductoId()
                );


        if (producto == null) {

            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(
                            "{\"mensaje\":\"Producto no encontrado\"}"
                    )
                    .build();
        }


        if (!producto.isActivo()) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            "{\"mensaje\":\"El producto no está disponible\"}"
                    )
                    .build();
        }


        if (producto.getStock() < pedido.getCantidad()) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            "{\"mensaje\":\"Stock insuficiente\"}"
                    )
                    .build();
        }


        pedido.setCliente(
                pedido.getCliente().trim()
        );

        pedido.setProducto(
                producto.getNombre()
        );

        pedido.setEstado(
                "PENDIENTE_PAGO"
        );


        /*
         * Primero guardamos el pedido para que MySQL
         * genere el ID interno.
         */
        entityManager.persist(
                pedido
        );

        entityManager.flush();


        /*
         * Después utilizamos ese ID para crear
         * el código amigable para el cliente.
         *
         * Ejemplo:
         * María González + ID 8
         * = MARIA-000008
         */
        String codigoPedido =
                generarCodigoPedido(
                        pedido.getCliente(),
                        pedido.getId()
                );


        pedido.setCodigoPedido(
                codigoPedido
        );


        entityManager.merge(
                pedido
        );

        entityManager.flush();


        return Response
                .status(Response.Status.CREATED)
                .entity(pedido)
                .build();
    }


    /* =========================================================
       LISTAR TODOS LOS PEDIDOS
       ========================================================= */

    @GET
    public Response listarPedidos() {

        List<Pedido> pedidos =
                entityManager
                        .createQuery(
                                "SELECT p FROM Pedido p "
                                        + "ORDER BY p.id DESC",
                                Pedido.class
                        )
                        .getResultList();


        return Response
                .ok(pedidos)
                .build();
    }


    /* =========================================================
       CONSULTAR POR ID INTERNO
       ========================================================= */

    @GET
    @Path("/{id}")
    public Response obtenerPedido(
            @PathParam("id") int id) {

        Pedido pedido =
                entityManager.find(
                        Pedido.class,
                        id
                );


        if (pedido == null) {

            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(
                            "{\"mensaje\":\"Pedido no encontrado\"}"
                    )
                    .build();
        }


        return Response
                .ok(pedido)
                .build();
    }


    /* =========================================================
       CONSULTAR POR NOMBRE O CÓDIGO
       ========================================================= */

    @GET
    @Path("/consultar")
    public Response consultarPedido(
            @QueryParam("busqueda") String busqueda) {

        if (busqueda == null ||
                busqueda.trim().isEmpty()) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            "{\"mensaje\":\"Debe indicar un nombre o código de pedido\"}"
                    )
                    .build();
        }


        String texto =
                busqueda
                        .trim()
                        .toUpperCase();


        /*
         * Primero intentamos encontrar una coincidencia
         * exacta por código.
         */
        List<Pedido> porCodigo =
                entityManager
                        .createQuery(
                                "SELECT p FROM Pedido p "
                                        + "WHERE UPPER(p.codigoPedido) = :codigo",
                                Pedido.class
                        )
                        .setParameter(
                                "codigo",
                                texto
                        )
                        .getResultList();


        if (!porCodigo.isEmpty()) {

            return Response
                    .ok(porCodigo)
                    .build();
        }


        /*
         * Si no corresponde a un código, buscamos
         * por el nombre del comprador.
         *
         * LIKE permite buscar:
         *
         * María
         * María González
         * González
         */
        List<Pedido> porCliente =
                entityManager
                        .createQuery(
                                "SELECT p FROM Pedido p "
                                        + "WHERE UPPER(p.cliente) LIKE :cliente "
                                        + "ORDER BY p.id DESC",
                                Pedido.class
                        )
                        .setParameter(
                                "cliente",
                                "%" + texto + "%"
                        )
                        .getResultList();


        if (porCliente.isEmpty()) {

            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(
                            "{\"mensaje\":\"No se encontraron pedidos con esos datos\"}"
                    )
                    .build();
        }


        return Response
                .ok(porCliente)
                .build();
    }


    /* =========================================================
       ACTUALIZAR ESTADO DEL PEDIDO
       ========================================================= */

    @PUT
    @Path("/{id}/estado")
    @Transactional
    public Response actualizarEstado(
            @PathParam("id") int id,
            Pedido datos) {

        Pedido pedido =
                entityManager.find(
                        Pedido.class,
                        id
                );


        if (pedido == null) {

            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(
                            "{\"mensaje\":\"Pedido no encontrado\"}"
                    )
                    .build();
        }


        if (datos.getEstado() == null ||
                datos.getEstado().trim().isEmpty()) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            "{\"mensaje\":\"Debe indicar el estado\"}"
                    )
                    .build();
        }


        String estadoAnterior =
                pedido.getEstado();


        String nuevoEstado =
                datos
                        .getEstado()
                        .trim()
                        .toUpperCase();


        /*
         * El inventario se descuenta solamente
         * cuando el pedido pasa por primera vez
         * al estado PAGADO.
         */
        if ("PAGADO".equals(nuevoEstado)
                &&
                !"PAGADO".equalsIgnoreCase(
                        estadoAnterior
                )) {


            if (pedido.getProductoId() <= 0 ||
                    pedido.getCantidad() <= 0) {

                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(
                                "{\"mensaje\":\"El pedido no contiene información válida de inventario\"}"
                        )
                        .build();
            }


            Producto producto =
                    entityManager.find(
                            Producto.class,
                            pedido.getProductoId()
                    );


            if (producto == null) {

                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(
                                "{\"mensaje\":\"Producto asociado al pedido no encontrado\"}"
                        )
                        .build();
            }


            if (producto.getStock()
                    < pedido.getCantidad()) {

                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(
                                "{\"mensaje\":\"Stock insuficiente para aprobar el pago\"}"
                        )
                        .build();
            }


            int nuevoStock =
                    producto.getStock()
                            - pedido.getCantidad();


            producto.setStock(
                    nuevoStock
            );


            entityManager.merge(
                    producto
            );
        }


        pedido.setEstado(
                nuevoEstado
        );


        entityManager.merge(
                pedido
        );


        return Response
                .ok(pedido)
                .build();
    }


    /* =========================================================
       GENERAR CÓDIGO AMIGABLE
       ========================================================= */

    private String generarCodigoPedido(
            String cliente,
            int id) {

        /*
         * Obtenemos solamente el primer nombre.
         *
         * "María González"
         * se transforma en
         * "María"
         */
        String primerNombre =
                cliente
                        .trim()
                        .split("\\s+")[0];


        /*
         * Eliminamos tildes.
         *
         * María -> Maria
         * José  -> Jose
         */
        primerNombre =
                Normalizer
                        .normalize(
                                primerNombre,
                                Normalizer.Form.NFD
                        )
                        .replaceAll(
                                "\\p{M}",
                                ""
                        );


        /*
         * Convertimos a mayúsculas y eliminamos
         * caracteres especiales.
         */
        primerNombre =
                primerNombre
                        .toUpperCase()
                        .replaceAll(
                                "[^A-Z0-9]",
                                ""
                        );


        /*
         * Protección por si el nombre llegara
         * a quedar vacío después de limpiarlo.
         */
        if (primerNombre.isEmpty()) {

            primerNombre =
                    "PEDIDO";
        }


        /*
         * El ID interno hace que el código
         * siga siendo único.
         *
         * Ejemplo:
         * MARIA-000008
         */
        return String.format(
                "%s-%06d",
                primerNombre,
                id
        );
    }
}