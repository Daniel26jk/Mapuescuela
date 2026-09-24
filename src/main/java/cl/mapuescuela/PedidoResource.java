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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (pedido == null) {
            return error(
                    Response.Status.BAD_REQUEST,
                    "Debe enviar los datos del pedido"
            );
        }

        if (vacio(pedido.getCliente())) {
            return error(
                    Response.Status.BAD_REQUEST,
                    "Debe indicar el nombre del cliente"
            );
        }

        String modalidad =
                normalizarModalidad(
                        pedido.getModalidadEntrega()
                );

        if (modalidad == null) {
            return error(
                    Response.Status.BAD_REQUEST,
                    "Debe indicar una modalidad de entrega válida"
            );
        }


        /*
         * Permite trabajar tanto con el carrito nuevo
         * como con el formato antiguo productoId + cantidad.
         */
        List<PedidoItem> itemsEntrada =
                pedido.getItems();

        if (itemsEntrada == null ||
                itemsEntrada.isEmpty()) {

            if (pedido.getProductoId() <= 0 ||
                    pedido.getCantidad() <= 0) {

                return error(
                        Response.Status.BAD_REQUEST,
                        "Debe agregar al menos un producto al pedido"
                );
            }

            PedidoItem itemLegacy =
                    new PedidoItem();

            itemLegacy.setProductoId(
                    pedido.getProductoId()
            );

            itemLegacy.setCantidad(
                    pedido.getCantidad()
            );

            itemsEntrada =
                    new ArrayList<>();

            itemsEntrada.add(
                    itemLegacy
            );
        }


        List<PedidoItem> itemsValidados =
                new ArrayList<>();

        int total = 0;
        int totalUnidades = 0;


        for (PedidoItem itemEntrada : itemsEntrada) {

            if (itemEntrada == null ||
                    itemEntrada.getProductoId() <= 0 ||
                    itemEntrada.getCantidad() <= 0) {

                return error(
                        Response.Status.BAD_REQUEST,
                        "Uno de los productos del carrito no es válido"
                );
            }


            Producto producto =
                    entityManager.find(
                            Producto.class,
                            itemEntrada.getProductoId()
                    );


            if (producto == null) {

                return error(
                        Response.Status.NOT_FOUND,
                        "Producto no encontrado: "
                                + itemEntrada.getProductoId()
                );
            }


            if (!producto.isActivo()) {

                return error(
                        Response.Status.BAD_REQUEST,
                        "El producto "
                                + producto.getNombre()
                                + " no está disponible"
                );
            }


            if (producto.getStock()
                    < itemEntrada.getCantidad()) {

                return error(
                        Response.Status.BAD_REQUEST,
                        "Stock insuficiente para "
                                + producto.getNombre()
                );
            }


            PedidoItem item =
                    new PedidoItem();

            item.setProductoId(
                    producto.getId()
            );

            item.setProducto(
                    producto.getNombre()
            );

            item.setCantidad(
                    itemEntrada.getCantidad()
            );

            item.setPrecioUnitario(
                    producto.getPrecio()
            );

            item.setSubtotal(
                    producto.getPrecio()
                            * itemEntrada.getCantidad()
            );


            itemsValidados.add(
                    item
            );

            total += item.getSubtotal();
            totalUnidades += item.getCantidad();
        }


        PedidoItem primero =
                itemsValidados.get(0);


        pedido.setCliente(
                pedido.getCliente().trim()
        );

        pedido.setEmail(
                limpiar(
                        pedido.getEmail()
                )
        );

        pedido.setTelefono(
                limpiar(
                        pedido.getTelefono()
                )
        );

        pedido.setModalidadEntrega(
                modalidad
        );

        pedido.setDireccionEntrega(
                limpiar(
                        pedido.getDireccionEntrega()
                )
        );

        pedido.setComunaEntrega(
                limpiar(
                        pedido.getComunaEntrega()
                )
        );


        /*
         * Se mantienen estos campos por compatibilidad
         * con Flowable y con pedidos antiguos.
         */
        pedido.setProductoId(
                primero.getProductoId()
        );

        pedido.setCantidad(
                totalUnidades
        );

        pedido.setProducto(
                itemsValidados.size() == 1
                        ? primero.getProducto()
                        : itemsValidados.size()
                        + " productos"
        );


        pedido.setTotal(
                total
        );

        pedido.setEstado(
                "PENDIENTE_PAGO"
        );

        pedido.setInventarioDescontado(
                false
        );

        pedido.setInventarioActualizado(
                false
        );

        pedido.setComprobanteAdjunto(
                false
        );

        pedido.setEmpresaTransporte(
                ""
        );

        pedido.setNumeroSeguimiento(
                ""
        );

        pedido.setFechaEnvio(
                ""
        );


        /*
         * Primero guardamos el pedido para obtener
         * el ID generado por MySQL.
         */
        entityManager.persist(
                pedido
        );

        entityManager.flush();


        pedido.setCodigoPedido(
                generarCodigoPedido(
                        pedido.getCliente(),
                        pedido.getId()
                )
        );

        entityManager.merge(
                pedido
        );


        /*
         * Guardamos los productos asociados al pedido.
         */
        for (PedidoItem item : itemsValidados) {

            item.setPedidoId(
                    pedido.getId()
            );

            entityManager.persist(
                    item
            );
        }


        entityManager.flush();

        pedido.setItems(
                itemsValidados
        );


        /*
         * Una vez creado el pedido y obtenido su ID real,
         * iniciamos automáticamente el proceso G4.
         */
        try {

            FlowableProcessStarter
                    .iniciarProceso(
                            pedido
                    );

        } catch (Exception e) {

            e.printStackTrace();

            throw new jakarta.ws.rs.WebApplicationException(
                    Response
                            .status(
                                    Response.Status.BAD_GATEWAY
                            )
                            .entity(
                                    "{\"mensaje\":\"El pedido no pudo iniciar el proceso en Flowable\"}"
                            )
                            .build()
            );
        }


        return Response
                .status(
                        Response.Status.CREATED
                )
                .entity(
                        pedido
                )
                .build();
    }


    /* =========================================================
       LISTAR PEDIDOS
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


        completarItems(
                pedidos
        );


        return Response
                .ok(
                        pedidos
                )
                .build();
    }


    /* =========================================================
       CONSULTAR PEDIDO POR ID
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

            return error(
                    Response.Status.NOT_FOUND,
                    "Pedido no encontrado"
            );
        }


        pedido.setItems(
                obtenerItems(
                        id
                )
        );


        return Response
                .ok(
                        pedido
                )
                .build();
    }


    /* =========================================================
       CONSULTAR PEDIDO
       ========================================================= */

    @GET
    @Path("/consultar")
    public Response consultarPedido(
            @QueryParam("busqueda") String busqueda) {

        if (vacio(busqueda)) {

            return error(
                    Response.Status.BAD_REQUEST,
                    "Debe indicar un nombre, correo, teléfono o código de pedido"
            );
        }


        String texto =
                busqueda.trim();

        String mayusculas =
                texto.toUpperCase();


        /*
         * Primero buscamos coincidencia exacta
         * por código de pedido.
         */
        List<Pedido> porCodigo =
                entityManager
                        .createQuery(
                                "SELECT p FROM Pedido p "
                                        + "WHERE UPPER(p.codigoPedido) = :codigo "
                                        + "ORDER BY p.id DESC",
                                Pedido.class
                        )
                        .setParameter(
                                "codigo",
                                mayusculas
                        )
                        .getResultList();


        if (!porCodigo.isEmpty()) {

            completarItems(
                    porCodigo
            );

            return Response
                    .ok(
                            porCodigo
                    )
                    .build();
        }


        /*
         * Si no coincide con código buscamos por:
         * cliente, email o teléfono.
         */
        List<Pedido> pedidos =
                entityManager
                        .createQuery(
                                "SELECT p FROM Pedido p "
                                        + "WHERE LOWER(p.cliente) LIKE :texto "
                                        + "OR LOWER(p.email) LIKE :texto "
                                        + "OR LOWER(p.telefono) LIKE :texto "
                                        + "ORDER BY p.id DESC",
                                Pedido.class
                        )
                        .setParameter(
                                "texto",
                                "%"
                                        + texto.toLowerCase()
                                        + "%"
                        )
                        .getResultList();


        completarItems(
                pedidos
        );


        return Response
                .ok(
                        pedidos
                )
                .build();
    }


    /* =========================================================
       ACTUALIZAR ESTADO
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

            return error(
                    Response.Status.NOT_FOUND,
                    "Pedido no encontrado"
            );
        }


        if (datos == null ||
                vacio(datos.getEstado())) {

            return error(
                    Response.Status.BAD_REQUEST,
                    "Debe indicar el estado"
            );
        }


        String estado =
                datos
                        .getEstado()
                        .trim()
                        .toUpperCase();


        if (!estadoValido(estado)) {

            return error(
                    Response.Status.BAD_REQUEST,
                    "Estado de pedido no válido"
            );
        }


        pedido.setEstado(
                estado
        );

        entityManager.merge(
                pedido
        );

        entityManager.flush();


        pedido.setItems(
                obtenerItems(
                        id
                )
        );


        return Response
                .ok(
                        pedido
                )
                .build();
    }


    /* =========================================================
       ACTUALIZAR INVENTARIO
       ========================================================= */

    @PUT
    @Path("/{id}/inventario")
    @Transactional
    public Response actualizarInventario(
            @PathParam("id") int id) {

        Pedido pedido =
                entityManager.find(
                        Pedido.class,
                        id
                );


        if (pedido == null) {

            return error(
                    Response.Status.NOT_FOUND,
                    "Pedido no encontrado"
            );
        }


        /*
         * Evita descontar inventario dos veces.
         *
         * inventarioActualizado corresponde a la
         * implementación anterior y
         * inventarioDescontado a la nueva.
         */
        if (pedido.isInventarioDescontado() ||
                pedido.isInventarioActualizado()) {

            pedido.setItems(
                    obtenerItems(
                            id
                    )
            );

            return Response
                    .ok(
                            pedido
                    )
                    .build();
        }


        if (!"PAGADO".equalsIgnoreCase(
                pedido.getEstado())) {

            return error(
                    Response.Status.BAD_REQUEST,
                    "El inventario solo puede descontarse cuando el pedido está PAGADO"
            );
        }


        List<PedidoItem> items =
                obtenerItems(
                        id
                );


        /*
         * Compatibilidad con pedidos anteriores
         * que no tengan registros PedidoItem.
         */
        if (items.isEmpty()) {

            PedidoItem legacy =
                    new PedidoItem();

            legacy.setProductoId(
                    pedido.getProductoId()
            );

            legacy.setCantidad(
                    pedido.getCantidad()
            );

            legacy.setProducto(
                    pedido.getProducto()
            );

            items.add(
                    legacy
            );
        }


        /*
         * Agrupamos cantidades por producto para
         * evitar descontar incorrectamente si un
         * mismo producto aparece varias veces.
         */
        Map<Integer, Integer> cantidades =
                new HashMap<>();


        for (PedidoItem item : items) {

            Integer actual =
                    cantidades.get(
                            item.getProductoId()
                    );

            cantidades.put(
                    item.getProductoId(),
                    (actual == null ? 0 : actual)
                            + item.getCantidad()
            );
        }


        Map<Integer, Producto> productos =
                new HashMap<>();


        /*
         * Primero validamos todo el stock antes
         * de realizar cualquier descuento.
         */
        for (Map.Entry<Integer, Integer> entry
                : cantidades.entrySet()) {

            Producto producto =
                    entityManager.find(
                            Producto.class,
                            entry.getKey()
                    );


            if (producto == null) {

                return error(
                        Response.Status.NOT_FOUND,
                        "Uno de los productos del pedido ya no existe"
                );
            }


            if (producto.getStock()
                    < entry.getValue()) {

                return error(
                        Response.Status.BAD_REQUEST,
                        "Stock insuficiente para "
                                + producto.getNombre()
                );
            }


            productos.put(
                    entry.getKey(),
                    producto
            );
        }


        /*
         * Descontamos el inventario.
         */
        for (Map.Entry<Integer, Integer> entry
                : cantidades.entrySet()) {

            Producto producto =
                    productos.get(
                            entry.getKey()
                    );

            producto.setStock(
                    producto.getStock()
                            - entry.getValue()
            );

            entityManager.merge(
                    producto
            );
        }


        pedido.setInventarioDescontado(
                true
        );

        pedido.setInventarioActualizado(
                true
        );


        entityManager.merge(
                pedido
        );

        entityManager.flush();


        pedido.setItems(
                items
        );


        return Response
                .ok(
                        pedido
                )
                .build();
    }


    /* =========================================================
       GUARDAR COMPROBANTE
       ========================================================= */

    @PUT
    @Path("/{id}/comprobante")
    @Transactional
    public Response guardarComprobante(
            @PathParam("id") int id,
            ComprobantePago datos) {

        Pedido pedido =
                entityManager.find(
                        Pedido.class,
                        id
                );


        if (pedido == null) {

            return error(
                    Response.Status.NOT_FOUND,
                    "Pedido no encontrado"
            );
        }


        if (datos == null ||
                vacio(datos.getNombreArchivo()) ||
                vacio(datos.getDatosBase64())) {

            return error(
                    Response.Status.BAD_REQUEST,
                    "Debe adjuntar un comprobante válido"
            );
        }


        List<ComprobantePago> existentes =
                entityManager
                        .createQuery(
                                "SELECT c FROM ComprobantePago c "
                                        + "WHERE c.pedidoId = :pedidoId",
                                ComprobantePago.class
                        )
                        .setParameter(
                                "pedidoId",
                                id
                        )
                        .getResultList();


        ComprobantePago comprobante;


        if (existentes.isEmpty()) {

            comprobante =
                    new ComprobantePago();

            comprobante.setPedidoId(
                    id
            );

        } else {

            comprobante =
                    existentes.get(0);
        }


        comprobante.setNombreArchivo(
                datos
                        .getNombreArchivo()
                        .trim()
        );


        comprobante.setTipoContenido(
                vacio(
                        datos.getTipoContenido()
                )
                        ? "application/octet-stream"
                        : datos
                                .getTipoContenido()
                                .trim()
        );


        comprobante.setDatosBase64(
                datos
                        .getDatosBase64()
                        .trim()
        );


        comprobante.setFechaCarga(
                LocalDateTime
                        .now()
                        .toString()
        );


        if (comprobante.getId() == 0) {

            entityManager.persist(
                    comprobante
            );

        } else {

            entityManager.merge(
                    comprobante
            );
        }


        pedido.setComprobanteAdjunto(
                true
        );


        if ("PENDIENTE_PAGO".equalsIgnoreCase(
                pedido.getEstado())) {

            pedido.setEstado(
                    "PAGO_REVISION"
            );
        }


        entityManager.merge(
                pedido
        );

        entityManager.flush();


        return Response
                .ok(
                        comprobante
                )
                .build();
    }


    /* =========================================================
       OBTENER COMPROBANTE
       ========================================================= */

    @GET
    @Path("/{id}/comprobante")
    public Response obtenerComprobante(
            @PathParam("id") int id) {

        Pedido pedido =
                entityManager.find(
                        Pedido.class,
                        id
                );


        if (pedido == null) {

            return error(
                    Response.Status.NOT_FOUND,
                    "Pedido no encontrado"
            );
        }


        List<ComprobantePago> comprobantes =
                entityManager
                        .createQuery(
                                "SELECT c FROM ComprobantePago c "
                                        + "WHERE c.pedidoId = :pedidoId",
                                ComprobantePago.class
                        )
                        .setParameter(
                                "pedidoId",
                                id
                        )
                        .getResultList();


        if (comprobantes.isEmpty()) {

            return error(
                    Response.Status.NOT_FOUND,
                    "El pedido no tiene comprobante adjunto"
            );
        }


        return Response
                .ok(
                        comprobantes.get(0)
                )
                .build();
    }


    /* =========================================================
       DATOS DE DESPACHO
       ========================================================= */

    @PUT
    @Path("/{id}/despacho")
    @Transactional
    public Response actualizarDespacho(
            @PathParam("id") int id,
            Pedido datos) {

        Pedido pedido =
                entityManager.find(
                        Pedido.class,
                        id
                );


        if (pedido == null) {

            return error(
                    Response.Status.NOT_FOUND,
                    "Pedido no encontrado"
            );
        }


        if (!"DESPACHO".equalsIgnoreCase(
                pedido.getModalidadEntrega())) {

            return error(
                    Response.Status.BAD_REQUEST,
                    "El pedido no corresponde a modalidad DESPACHO"
            );
        }


        pedido.setEmpresaTransporte(
                limpiar(
                        datos == null
                                ? null
                                : datos.getEmpresaTransporte()
                )
        );


        pedido.setNumeroSeguimiento(
                limpiar(
                        datos == null
                                ? null
                                : datos.getNumeroSeguimiento()
                )
        );


        pedido.setFechaEnvio(
                limpiar(
                        datos == null
                                ? null
                                : datos.getFechaEnvio()
                )
        );


        entityManager.merge(
                pedido
        );

        entityManager.flush();


        pedido.setItems(
                obtenerItems(
                        id
                )
        );


        return Response
                .ok(
                        pedido
                )
                .build();
    }


    /* =========================================================
       UTILIDADES
       ========================================================= */

    private List<PedidoItem> obtenerItems(
            int pedidoId) {

        return entityManager
                .createQuery(
                        "SELECT i FROM PedidoItem i "
                                + "WHERE i.pedidoId = :pedidoId "
                                + "ORDER BY i.id",
                        PedidoItem.class
                )
                .setParameter(
                        "pedidoId",
                        pedidoId
                )
                .getResultList();
    }


    private void completarItems(
            List<Pedido> pedidos) {

        for (Pedido pedido : pedidos) {

            pedido.setItems(
                    obtenerItems(
                            pedido.getId()
                    )
            );
        }
    }


    private boolean estadoValido(
            String estado) {

        return "PENDIENTE_PAGO".equals(estado)
                || "PAGO_REVISION".equals(estado)
                || "PAGADO".equals(estado)
                || "RECHAZADO".equals(estado)
                || "PREPARANDO".equals(estado)
                || "LISTO_RETIRO".equals(estado)
                || "DESPACHADO".equals(estado)
                || "ENTREGADO".equals(estado)
                || "CANCELADO".equals(estado);
    }


    private String normalizarModalidad(
            String modalidad) {

        if (modalidad == null) {
            return null;
        }


        String valor =
                modalidad
                        .trim()
                        .toUpperCase();


        if ("RETIRO".equals(valor) ||
                "DESPACHO".equals(valor)) {

            return valor;
        }


        return null;
    }


    private boolean vacio(
            String valor) {

        return valor == null ||
                valor.trim().isEmpty();
    }


    private String limpiar(
            String valor) {

        return valor == null
                ? ""
                : valor.trim();
    }


    private String generarCodigoPedido(
            String cliente,
            int id) {

        String nombre =
                cliente == null
                        ? "PEDIDO"
                        : cliente.trim();


        String primero =
                nombre.contains(" ")
                        ? nombre.substring(
                                0,
                                nombre.indexOf(' ')
                        )
                        : nombre;


        primero =
                Normalizer
                        .normalize(
                                primero,
                                Normalizer.Form.NFD
                        )
                        .replaceAll(
                                "\\p{M}",
                                ""
                        )
                        .replaceAll(
                                "[^A-Za-z0-9]",
                                ""
                        )
                        .toUpperCase();


        if (primero.isEmpty()) {
            primero = "PEDIDO";
        }


        return primero
                + "-"
                + String.format(
                        "%06d",
                        id
                );
    }


    private Response error(
            Response.Status estado,
            String mensaje) {

        return Response
                .status(
                        estado
                )
                .entity(
                        "{\"mensaje\":\""
                                + mensaje.replace(
                                        "\"",
                                        "'"
                                )
                                + "\"}"
                )
                .build();
    }
}
