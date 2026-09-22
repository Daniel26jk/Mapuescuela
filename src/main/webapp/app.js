const API_PRODUCTOS =
    "webapi/productos";

const API_PEDIDOS =
    "webapi/pedidos";

const API_AUTH =
    "webapi/auth";

const STOCK_BAJO =
    5;


/* =========================================================
   ELEMENTOS
   ========================================================= */

const tablaProductos =
    document.getElementById(
        "tablaProductos"
    );

const tablaPedidosActivos =
    document.getElementById(
        "tablaPedidosActivos"
    );

const tablaPedidosCancelados =
    document.getElementById(
        "tablaPedidosCancelados"
    );

const tablaPedidosCompletados =
    document.getElementById(
        "tablaPedidosCompletados"
    );


const formProducto =
    document.getElementById(
        "formProducto"
    );

const productoEditarId =
    document.getElementById(
        "productoEditarId"
    );

const nombreProducto =
    document.getElementById(
        "nombreProducto"
    );

const categoriaProducto =
    document.getElementById(
        "categoriaProducto"
    );

const precioProducto =
    document.getElementById(
        "precioProducto"
    );

const stockProducto =
    document.getElementById(
        "stockProducto"
    );

const imagenProducto =
    document.getElementById(
        "imagenProducto"
    );

const descripcionProducto =
    document.getElementById(
        "descripcionProducto"
    );

const btnGuardarProducto =
    document.getElementById(
        "btnGuardarProducto"
    );

const btnCancelarEdicion =
    document.getElementById(
        "btnCancelarEdicion"
    );

const mensajeProducto =
    document.getElementById(
        "mensajeProducto"
    );


const buscarPedido =
    document.getElementById(
        "buscarPedido"
    );

const btnActualizar =
    document.getElementById(
        "btnActualizar"
    );

const btnInicio =
    document.getElementById(
        "btnInicio"
    );

const btnCerrarSesion =
    document.getElementById(
        "btnCerrarSesion"
    );


const totalActivos =
    document.getElementById(
        "totalActivos"
    );

const totalCompletados =
    document.getElementById(
        "totalCompletados"
    );

const totalCancelados =
    document.getElementById(
        "totalCancelados"
    );

const totalProductos =
    document.getElementById(
        "totalProductos"
    );

const totalStockBajo =
    document.getElementById(
        "totalStockBajo"
    );


const panelDetallePedido =
    document.getElementById(
        "panelDetallePedido"
    );

const tituloDetallePedido =
    document.getElementById(
        "tituloDetallePedido"
    );

const detallePedidoAdmin =
    document.getElementById(
        "detallePedidoAdmin"
    );

const btnCerrarDetalle =
    document.getElementById(
        "btnCerrarDetalle"
    );


const formDespacho =
    document.getElementById(
        "formDespacho"
    );

const despachoPedidoId =
    document.getElementById(
        "despachoPedidoId"
    );

const empresaTransporte =
    document.getElementById(
        "empresaTransporte"
    );

const numeroSeguimiento =
    document.getElementById(
        "numeroSeguimiento"
    );

const fechaEnvio =
    document.getElementById(
        "fechaEnvio"
    );

const mensajeDespacho =
    document.getElementById(
        "mensajeDespacho"
    );


let productosGuardados =
    [];

let pedidosGuardados =
    [];


/* =========================================================
   INICIO
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    iniciar
);


async function iniciar() {

    const sesion =
        await verificarSesion();


    if (!sesion) {

        window.location.href =
            "login.html";

        return;
    }


    await Promise.all([
        cargarProductos(),
        cargarPedidos()
    ]);
}


/* =========================================================
   SESION
   ========================================================= */

async function verificarSesion() {

    try {

        const response =
            await fetch(
                `${API_AUTH}/session`,
                {
                    credentials:
                        "same-origin"
                }
            );


        if (!response.ok) {

            return false;
        }


        const data =
            await response.json();


        return (
            data.autenticado
            === true
        );


    } catch (error) {

        console.error(
            "No fue posible verificar la sesión.",
            error
        );

        return false;
    }
}


btnInicio.addEventListener(
    "click",
    () => {

        window.location.href =
            "index.html";
    }
);


btnCerrarSesion.addEventListener(
    "click",
    async () => {

        try {

            await fetch(
                `${API_AUTH}/logout`,
                {
                    method:
                        "POST",

                    credentials:
                        "same-origin"
                }
            );

        } catch (error) {

            console.error(
                error
            );
        }


        window.location.href =
            "login.html";
    }
);


/* =========================================================
   PRODUCTOS
   ========================================================= */

async function cargarProductos() {

    try {

        const response =
            await fetch(
                API_PRODUCTOS
            );


        if (!response.ok) {

            throw new Error(
                "No fue posible cargar productos."
            );
        }


        productosGuardados =
            await response.json();


        renderProductos();

        actualizarResumen();


    } catch (error) {

        tablaProductos.innerHTML =
            `
            <tr>
                <td colspan="7">
                    ${escaparHtml(error.message)}
                </td>
            </tr>
            `;
    }
}


function renderProductos() {

    tablaProductos.innerHTML =
        "";


    if (
        productosGuardados.length
        === 0
    ) {

        tablaProductos.innerHTML =
            `
            <tr>
                <td colspan="7">
                    No hay productos registrados.
                </td>
            </tr>
            `;

        return;
    }


    productosGuardados.forEach(
        producto => {

            const fila =
                document.createElement(
                    "tr"
                );


            fila.innerHTML =
                `
                <td>
                    ${producto.id}
                </td>

                <td>
                    <strong>
                        ${escaparHtml(producto.nombre)}
                    </strong>

                    <br>

                    <small>
                        ${escaparHtml(producto.descripcion || "")}
                    </small>
                </td>

                <td>
                    ${escaparHtml(producto.categoria || "-")}
                </td>

                <td>
                    $${formatearPrecio(producto.precio)}
                </td>

                <td>
                    ${
                        producto.stock <= STOCK_BAJO
                            ? "⚠ "
                            : ""
                    }
                    ${producto.stock}
                </td>

                <td>
                    ${
                        producto.activo
                            ? "Activo"
                            : "Inactivo"
                    }
                </td>

                <td>

                    <div class="acciones-tabla">

                        <button
                            class="boton-tabla"
                            data-accion="editar"
                        >
                            Editar
                        </button>

                        <button
                            class="boton-tabla"
                            data-accion="estado"
                        >
                            ${
                                producto.activo
                                    ? "Desactivar"
                                    : "Activar"
                            }
                        </button>

                    </div>

                </td>
                `;


            fila
                .querySelector(
                    '[data-accion="editar"]'
                )
                .addEventListener(
                    "click",
                    () =>
                        editarProducto(
                            producto
                        )
                );


            fila
                .querySelector(
                    '[data-accion="estado"]'
                )
                .addEventListener(
                    "click",
                    () =>
                        cambiarEstadoProducto(
                            producto
                        )
                );


            tablaProductos.appendChild(
                fila
            );
        }
    );
}


/* =========================================================
   GUARDAR PRODUCTO
   ========================================================= */

formProducto.addEventListener(
    "submit",
    async event => {

        event.preventDefault();


        const id =
            Number(
                productoEditarId.value
                || 0
            );


        const editando =
            id > 0;


        const existente =
            productosGuardados.find(
                producto =>
                    producto.id
                    === id
            );


        const producto = {

            nombre:
                nombreProducto
                    .value
                    .trim(),

            categoria:
                categoriaProducto
                    .value
                    .trim(),

            precio:
                Number(
                    precioProducto.value
                ),

            stock:
                Number(
                    stockProducto.value
                ),

            imagenUrl:
                imagenProducto
                    .value
                    .trim(),

            descripcion:
                descripcionProducto
                    .value
                    .trim(),

            activo:
                editando
                    ? existente.activo
                    : true
        };


        try {

            const response =
                await fetch(

                    editando
                        ? `${API_PRODUCTOS}/${id}`
                        : API_PRODUCTOS,

                    {
                        method:
                            editando
                                ? "PUT"
                                : "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                producto
                            )
                    }
                );


            const data =
                await response.json();


            if (!response.ok) {

                throw new Error(
                    data.mensaje
                    || "No fue posible guardar el producto."
                );
            }


            limpiarFormularioProducto();


            mostrarMensaje(

                mensajeProducto,

                editando
                    ? "Producto actualizado correctamente."
                    : "Producto creado correctamente.",

                "exito"
            );


            await cargarProductos();


        } catch (error) {

            mostrarMensaje(
                mensajeProducto,
                error.message,
                "error"
            );
        }
    }
);


/* =========================================================
   EDITAR PRODUCTO
   ========================================================= */

function editarProducto(
    producto
) {

    productoEditarId.value =
        producto.id;

    nombreProducto.value =
        producto.nombre || "";

    categoriaProducto.value =
        producto.categoria || "";

    precioProducto.value =
        producto.precio || 0;

    stockProducto.value =
        producto.stock || 0;

    imagenProducto.value =
        producto.imagenUrl || "";

    descripcionProducto.value =
        producto.descripcion || "";


    btnGuardarProducto.textContent =
        "Guardar cambios";


    btnCancelarEdicion.style.display =
        "inline-block";


    formProducto.scrollIntoView(
        {
            behavior:
                "smooth",

            block:
                "start"
        }
    );
}


btnCancelarEdicion.addEventListener(
    "click",
    limpiarFormularioProducto
);


function limpiarFormularioProducto() {

    formProducto.reset();

    productoEditarId.value =
        "";

    btnGuardarProducto.textContent =
        "Crear producto";

    btnCancelarEdicion.style.display =
        "none";
}


/* =========================================================
   ACTIVAR / DESACTIVAR PRODUCTO
   ========================================================= */

async function cambiarEstadoProducto(
    producto
) {

    try {

        const response =
            await fetch(

                `${API_PRODUCTOS}/${producto.id}/estado`,

                {
                    method:
                        "PUT",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            {
                                activo:
                                    !producto.activo
                            }
                        )
                }
            );


        const data =
            await response.json();


        if (!response.ok) {

            throw new Error(
                data.mensaje
                || "No fue posible cambiar el estado."
            );
        }


        await cargarProductos();


    } catch (error) {

        alert(
            error.message
        );
    }
}


/* =========================================================
   PEDIDOS
   ========================================================= */

async function cargarPedidos() {

    try {

        const response =
            await fetch(
                API_PEDIDOS
            );


        if (!response.ok) {

            throw new Error(
                "No fue posible cargar los pedidos."
            );
        }


        pedidosGuardados =
            await response.json();


        mostrarPedidosSeparados(
            pedidosGuardados
        );


        actualizarResumen();


    } catch (error) {

        console.error(
            error
        );


        tablaPedidosActivos.innerHTML =
            `
            <tr>
                <td colspan="8">
                    ${escaparHtml(error.message)}
                </td>
            </tr>
            `;
    }
}


/* =========================================================
   CLASIFICACION PEDIDOS
   ========================================================= */

function mostrarPedidosSeparados(
    pedidos
) {

    const activos =
        pedidos.filter(
            pedido =>
                ![
                    "CANCELADO",
                    "RECHAZADO",
                    "ENTREGADO"
                ].includes(
                    pedido.estado
                )
        );


    const cancelados =
        pedidos.filter(
            pedido =>
                [
                    "CANCELADO",
                    "RECHAZADO"
                ].includes(
                    pedido.estado
                )
        );


    const completados =
        pedidos.filter(
            pedido =>
                pedido.estado
                === "ENTREGADO"
        );


    renderPedidosActivos(
        activos
    );


    renderPedidosHistoricos(
        tablaPedidosCancelados,
        cancelados,
        "No existen pedidos cancelados o rechazados."
    );


    renderPedidosHistoricos(
        tablaPedidosCompletados,
        completados,
        "No existen pedidos completados."
    );
}


/* =========================================================
   PEDIDOS ACTIVOS
   ========================================================= */

function renderPedidosActivos(
    pedidos
) {

    tablaPedidosActivos.innerHTML =
        "";


    if (
        pedidos.length === 0
    ) {

        tablaPedidosActivos.innerHTML =
            `
            <tr>
                <td colspan="8">
                    No existen pedidos activos.
                </td>
            </tr>
            `;

        return;
    }


    pedidos.forEach(
        pedido => {

            const fila =
                crearFilaPedido(
                    pedido,
                    true
                );


            tablaPedidosActivos.appendChild(
                fila
            );
        }
    );
}


/* =========================================================
   HISTORIAL PEDIDOS
   ========================================================= */

function renderPedidosHistoricos(
    tabla,
    pedidos,
    mensajeVacio
) {

    tabla.innerHTML =
        "";


    if (
        pedidos.length === 0
    ) {

        tabla.innerHTML =
            `
            <tr>
                <td colspan="8">
                    ${mensajeVacio}
                </td>
            </tr>
            `;

        return;
    }


    pedidos.forEach(
        pedido => {

            const fila =
                crearFilaPedido(
                    pedido,
                    false
                );


            tabla.appendChild(
                fila
            );
        }
    );
}


/* =========================================================
   CREAR FILA PEDIDO
   ========================================================= */

function crearFilaPedido(
    pedido,
    editable
) {

    const fila =
        document.createElement(
            "tr"
        );


    const productos =
        (
            pedido.items
            || []
        )
            .map(
                item =>
                    `
                    ${escaparHtml(item.producto)}
                    x${item.cantidad}
                    `
            )
            .join(
                "<br>"
            )
        ||
        escaparHtml(
            pedido.producto
            || "-"
        );


    const comprobante =

        pedido.comprobanteAdjunto

            ? `
                <button
                    class="boton-tabla"
                    data-accion="comprobante"
                >
                    Ver
                </button>
              `

            : `
                <span class="texto-pendiente">
                    Pendiente
                </span>
              `;


    let gestion;


    if (editable) {

        gestion =
            `
            <div class="acciones-tabla">

                <button
                    class="boton-tabla"
                    data-accion="guardar"
                >
                    Guardar estado
                </button>

                <button
                    class="boton-tabla"
                    data-accion="detalle"
                >
                    Detalle
                </button>

            </div>
            `;

    } else {

        gestion =
            `
            <button
                class="boton-tabla"
                data-accion="detalle"
            >
                Ver detalle
            </button>
            `;
    }


    fila.innerHTML =
        `
        <td>

            <strong>
                ${
                    escaparHtml(
                        pedido.codigoPedido
                        || `#${pedido.id}`
                    )
                }
            </strong>

        </td>


        <td>

            ${escaparHtml(
                pedido.cliente
                || "-"
            )}

            <br>

            <small>

                ${escaparHtml(
                    pedido.email
                    || "-"
                )}

                <br>

                ${escaparHtml(
                    pedido.telefono
                    || "-"
                )}

            </small>

        </td>


        <td>
            ${productos}
        </td>


        <td>

            $${formatearPrecio(
                pedido.total
                || 0
            )}

        </td>


        <td>

            ${formatearModalidad(
                pedido.modalidadEntrega
            )}

        </td>


        <td>

            ${
                editable

                    ? `
                        <select class="selector-estado">

                            ${crearOpcionesEstado(
                                pedido
                            )}

                        </select>
                      `

                    : `
                        <strong>
                            ${formatearEstado(
                                pedido.estado
                            )}
                        </strong>
                      `
            }

        </td>


        <td>
            ${comprobante}
        </td>


        <td>
            ${gestion}
        </td>
        `;


    const btnDetalle =
        fila.querySelector(
            '[data-accion="detalle"]'
        );


    btnDetalle.addEventListener(
        "click",
        () =>
            mostrarDetallePedido(
                pedido.id
            )
    );


    const btnComprobante =
        fila.querySelector(
            '[data-accion="comprobante"]'
        );


    if (btnComprobante) {

        btnComprobante.addEventListener(
            "click",
            () =>
                verComprobante(
                    pedido.id
                )
        );
    }


    if (editable) {

        const btnGuardar =
            fila.querySelector(
                '[data-accion="guardar"]'
            );


        const selectorEstado =
            fila.querySelector(
                ".selector-estado"
            );


        btnGuardar.addEventListener(
            "click",
            () =>
                actualizarEstado(

                    pedido.id,

                    selectorEstado.value
                )
        );
    }


    return fila;
}


/* =========================================================
   OPCIONES ESTADO
   ========================================================= */

function crearOpcionesEstado(
    pedido
) {

    const estadosBase = [

        "PENDIENTE_PAGO",

        "PAGO_REVISION",

        "PAGADO",

        "RECHAZADO",

        "PREPARANDO"
    ];


    const estadoEntrega =

        pedido.modalidadEntrega
        === "RETIRO"

            ? [
                "LISTO_RETIRO"
              ]

            : [
                "DESPACHADO"
              ];


    return estadosBase
        .concat(
            estadoEntrega,
            [
                "ENTREGADO",
                "CANCELADO"
            ]
        )
        .map(
            estado =>

                `
                <option
                    value="${estado}"
                    ${
                        pedido.estado === estado
                            ? "selected"
                            : ""
                    }
                >
                    ${formatearEstado(estado)}
                </option>
                `
        )
        .join(
            ""
        );
}


/* =========================================================
   ACTUALIZAR ESTADO
   ========================================================= */

async function actualizarEstado(
    id,
    estado
) {

    try {

        let response =
            await fetch(

                `${API_PEDIDOS}/${id}/estado`,

                {
                    method:
                        "PUT",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            {
                                estado
                            }
                        )
                }
            );


        let data =
            await response.json();


        if (!response.ok) {

            throw new Error(
                data.mensaje
                || "No fue posible actualizar el estado."
            );
        }


        /*
         * Cuando el pedido pasa a PAGADO,
         * se solicita la actualización
         * de inventario.
         */

        if (
            estado === "PAGADO"
        ) {

            response =
                await fetch(

                    `${API_PEDIDOS}/${id}/inventario`,

                    {
                        method:
                            "PUT",

                        headers: {
                            "Content-Type":
                                "application/json"
                        }
                    }
                );


            data =
                await response.json();


            if (!response.ok) {

                throw new Error(

                    data.mensaje

                    ||
                    "El estado cambió, pero no fue posible descontar inventario."
                );
            }
        }


        await Promise.all([
            cargarPedidos(),
            cargarProductos()
        ]);


    } catch (error) {

        alert(
            error.message
        );
    }
}


/* =========================================================
   COMPROBANTE
   ========================================================= */

async function verComprobante(
    id
) {

    try {

        const response =
            await fetch(
                `${API_PEDIDOS}/${id}/comprobante`
            );


        const data =
            await response.json();


        if (!response.ok) {

            throw new Error(
                data.mensaje
                || "No fue posible obtener el comprobante."
            );
        }


        const binario =
            atob(
                data.datosBase64
            );


        const bytes =
            new Uint8Array(
                binario.length
            );


        for (
            let i = 0;
            i < binario.length;
            i++
        ) {

            bytes[i] =
                binario.charCodeAt(
                    i
                );
        }


        const blob =
            new Blob(
                [bytes],
                {
                    type:
                        data.tipoContenido
                        || "application/octet-stream"
                }
            );


        const url =
            URL.createObjectURL(
                blob
            );


        const ventana =
            window.open(
                url,
                "_blank"
            );


        if (!ventana) {

            URL.revokeObjectURL(
                url
            );


            throw new Error(
                "El navegador bloqueó la ventana del comprobante."
            );
        }


        setTimeout(
            () =>
                URL.revokeObjectURL(
                    url
                ),
            60000
        );


    } catch (error) {

        alert(
            error.message
        );
    }
}


/* =========================================================
   DETALLE PEDIDO
   ========================================================= */

async function mostrarDetallePedido(
    id
) {

    try {

        const response =
            await fetch(
                `${API_PEDIDOS}/${id}`
            );


        const pedido =
            await response.json();


        if (!response.ok) {

            throw new Error(
                pedido.mensaje
                || "No fue posible cargar el pedido."
            );
        }


        tituloDetallePedido.textContent =
            `
            ${
                pedido.codigoPedido
                || `#${pedido.id}`
            }
            ·
            ${
                pedido.cliente
                || ""
            }
            `;


        const items =
            (
                pedido.items
                || []
            )
                .map(
                    item =>

                        `
                        <li>
                            ${escaparHtml(item.producto)}
                            x${item.cantidad}
                            —
                            $${formatearPrecio(item.subtotal)}
                        </li>
                        `
                )
                .join(
                    ""
                );


        detallePedidoAdmin.innerHTML =
            `
            <div>

                <span>
                    Cliente
                </span>

                <strong>
                    ${escaparHtml(pedido.cliente || "-")}
                </strong>

            </div>


            <div>

                <span>
                    Correo
                </span>

                <strong>
                    ${escaparHtml(pedido.email || "-")}
                </strong>

            </div>


            <div>

                <span>
                    Teléfono
                </span>

                <strong>
                    ${escaparHtml(pedido.telefono || "-")}
                </strong>

            </div>


            <div>

                <span>
                    Estado
                </span>

                <strong>
                    ${formatearEstado(pedido.estado)}
                </strong>

            </div>


            <div>

                <span>
                    Modalidad
                </span>

                <strong>
                    ${formatearModalidad(pedido.modalidadEntrega)}
                </strong>

            </div>


            <div>

                <span>
                    Total
                </span>

                <strong>
                    $${formatearPrecio(pedido.total || 0)}
                </strong>

            </div>


            <div class="detalle-admin-ancho">

                <span>
                    Productos
                </span>

                <ul>
                    ${
                        items
                        ||
                        `
                        <li>
                            ${escaparHtml(pedido.producto || "-")}
                        </li>
                        `
                    }
                </ul>

            </div>


            <div>

                <span>
                    Comprobante
                </span>

                <strong>

                    ${
                        pedido.comprobanteAdjunto
                            ? "Adjuntado"
                            : "Pendiente"
                    }

                </strong>

            </div>


            ${
                pedido.modalidadEntrega
                === "DESPACHO"

                    ? `
                        <div class="detalle-admin-ancho">

                            <span>
                                Dirección de entrega
                            </span>

                            <strong>

                                ${escaparHtml(
                                    pedido.direccionEntrega
                                    || "-"
                                )}

                                ,

                                ${escaparHtml(
                                    pedido.comunaEntrega
                                    || "-"
                                )}

                            </strong>

                        </div>
                      `

                    : ""
            }


            ${
                pedido.empresaTransporte

                    ? `
                        <div>

                            <span>
                                Transporte
                            </span>

                            <strong>
                                ${escaparHtml(pedido.empresaTransporte)}
                            </strong>

                        </div>
                      `

                    : ""
            }


            ${
                pedido.numeroSeguimiento

                    ? `
                        <div>

                            <span>
                                N° seguimiento
                            </span>

                            <strong>
                                ${escaparHtml(pedido.numeroSeguimiento)}
                            </strong>

                        </div>
                      `

                    : ""
            }


            ${
                pedido.fechaEnvio

                    ? `
                        <div>

                            <span>
                                Fecha de envío
                            </span>

                            <strong>
                                ${escaparHtml(pedido.fechaEnvio)}
                            </strong>

                        </div>
                      `

                    : ""
            }
            `;


        despachoPedidoId.value =
            pedido.id;


        empresaTransporte.value =
            pedido.empresaTransporte
            || "";


        numeroSeguimiento.value =
            pedido.numeroSeguimiento
            || "";


        fechaEnvio.value =
            pedido.fechaEnvio
            || "";


        formDespacho.style.display =

            pedido.modalidadEntrega
            === "DESPACHO"

                ? "block"
                : "none";


        mensajeDespacho.textContent =
            "";


        panelDetallePedido.style.display =
            "block";


        panelDetallePedido.scrollIntoView(
            {
                behavior:
                    "smooth",

                block:
                    "start"
            }
        );


    } catch (error) {

        alert(
            error.message
        );
    }
}


/* =========================================================
   CERRAR DETALLE
   ========================================================= */

btnCerrarDetalle.addEventListener(
    "click",
    () => {

        panelDetallePedido.style.display =
            "none";
    }
);


/* =========================================================
   DESPACHO
   ========================================================= */

formDespacho.addEventListener(
    "submit",
    async event => {

        event.preventDefault();


        const id =
            Number(
                despachoPedidoId.value
            );


        try {

            const response =
                await fetch(

                    `${API_PEDIDOS}/${id}/despacho`,

                    {
                        method:
                            "PUT",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                {
                                    empresaTransporte:
                                        empresaTransporte
                                            .value
                                            .trim(),

                                    numeroSeguimiento:
                                        numeroSeguimiento
                                            .value
                                            .trim(),

                                    fechaEnvio:
                                        fechaEnvio.value
                                }
                            )
                    }
                );


            const data =
                await response.json();


            if (!response.ok) {

                throw new Error(
                    data.mensaje
                    || "No fue posible guardar el despacho."
                );
            }


            mostrarMensaje(
                mensajeDespacho,
                "Datos de despacho guardados.",
                "exito"
            );


            await cargarPedidos();


        } catch (error) {

            mostrarMensaje(
                mensajeDespacho,
                error.message,
                "error"
            );
        }
    }
);


/* =========================================================
   BUSCADOR
   ========================================================= */

buscarPedido.addEventListener(
    "input",
    () => {

        const texto =
            buscarPedido
                .value
                .toLowerCase()
                .trim();


        const filtrados =
            pedidosGuardados.filter(
                pedido => {

                    const items =
                        (
                            pedido.items
                            || []
                        )
                            .map(
                                item =>
                                    item.producto
                            )
                            .join(
                                " "
                            );


                    return [

                        pedido.codigoPedido,

                        pedido.cliente,

                        pedido.email,

                        pedido.telefono,

                        pedido.producto,

                        pedido.estado,

                        items

                    ].some(
                        valor =>

                            (
                                valor
                                || ""
                            )
                                .toLowerCase()
                                .includes(
                                    texto
                                )
                    );
                }
            );


        mostrarPedidosSeparados(
            filtrados
        );
    }
);


/* =========================================================
   ACTUALIZAR
   ========================================================= */

btnActualizar.addEventListener(
    "click",
    async () => {

        buscarPedido.value =
            "";


        await Promise.all([
            cargarPedidos(),
            cargarProductos()
        ]);
    }
);


/* =========================================================
   RESUMEN
   ========================================================= */

function actualizarResumen() {

    totalActivos.textContent =
        pedidosGuardados.filter(
            pedido =>
                ![
                    "CANCELADO",
                    "RECHAZADO",
                    "ENTREGADO"
                ].includes(
                    pedido.estado
                )
        ).length;


    totalCompletados.textContent =
        pedidosGuardados.filter(
            pedido =>
                pedido.estado
                === "ENTREGADO"
        ).length;


    totalCancelados.textContent =
        pedidosGuardados.filter(
            pedido =>
                [
                    "CANCELADO",
                    "RECHAZADO"
                ].includes(
                    pedido.estado
                )
        ).length;


    totalProductos.textContent =
        productosGuardados.filter(
            producto =>
                producto.activo
        ).length;


    totalStockBajo.textContent =
        productosGuardados.filter(
            producto =>
                producto.activo
                &&
                producto.stock
                <= STOCK_BAJO
        ).length;
}


/* =========================================================
   UTILIDADES
   ========================================================= */

function formatearPrecio(
    valor
) {

    return Number(
        valor || 0
    ).toLocaleString(
        "es-CL"
    );
}


function formatearModalidad(
    modalidad
) {

    if (
        modalidad === "RETIRO"
    ) {

        return "Retiro";
    }


    if (
        modalidad === "DESPACHO"
    ) {

        return "Despacho";
    }


    return modalidad || "-";
}


function formatearEstado(
    estado
) {

    const estados = {

        PENDIENTE_PAGO:
            "Pendiente de pago",

        PAGO_REVISION:
            "Pago en revisión",

        PAGADO:
            "Pago aprobado",

        RECHAZADO:
            "Pago rechazado",

        PREPARANDO:
            "En preparación",

        LISTO_RETIRO:
            "Listo para retiro",

        DESPACHADO:
            "Enviado",

        ENTREGADO:
            "Finalizado",

        CANCELADO:
            "Cancelado"
    };


    return (
        estados[estado]
        || estado
        || "-"
    );
}


function mostrarMensaje(
    elemento,
    texto,
    tipo
) {

    elemento.textContent =
        texto;


    elemento.className =
        "mensaje"
        +
        (
            tipo
                ? ` ${tipo}`
                : ""
        );
}


function escaparHtml(
    valor
) {

    return String(
        valor == null
            ? ""
            : valor
    ).replace(
        /[&<>'"]/g,
        caracter => (

            {
                "&":
                    "&amp;",

                "<":
                    "&lt;",

                ">":
                    "&gt;",

                "'":
                    "&#39;",

                '"':
                    "&quot;"

            }[caracter]
        )
    );
}
