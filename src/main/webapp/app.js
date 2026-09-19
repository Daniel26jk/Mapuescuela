const API_PEDIDOS =
    "webapi/pedidos";

const API_PRODUCTOS =
    "webapi/productos";


const tablaPedidosActivos =
    document.getElementById("tablaPedidosActivos");

const tablaPedidosCancelados =
    document.getElementById("tablaPedidosCancelados");

const tablaPedidosCompletados =
    document.getElementById("tablaPedidosCompletados");


const buscarPedido =
    document.getElementById("buscarPedido");

const btnActualizar =
    document.getElementById("btnActualizar");


const totalActivos =
    document.getElementById("totalActivos");

const totalCancelados =
    document.getElementById("totalCancelados");

const totalCompletados =
    document.getElementById("totalCompletados");

const totalProductosActivos =
    document.getElementById("totalProductosActivos");


const formProducto =
    document.getElementById("formProducto");

const tablaProductos =
    document.getElementById("tablaProductos");

const mensajeProducto =
    document.getElementById("mensajeProducto");

const btnGuardarProducto =
    document.getElementById("btnGuardarProducto");

const btnCancelarEdicion =
    document.getElementById("btnCancelarEdicion");


let pedidosGuardados = [];

let productosGuardados = [];

let productoEditandoId = null;

let productoEditandoActivo = true;


/* =========================================================
   INICIO
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        await cargarProductos();

        await cargarPedidos();

    }
);


/* =========================================================
   PRODUCTOS
   ========================================================= */

formProducto.addEventListener(
    "submit",
    async event => {

        event.preventDefault();


        const nombre =
            document
                .getElementById("nombreProducto")
                .value
                .trim();


        const descripcion =
            document
                .getElementById("descripcionProducto")
                .value
                .trim();


        const precio =
            Number(
                document
                    .getElementById("precioProducto")
                    .value
            );


        const stock =
            Number(
                document
                    .getElementById("stockProducto")
                    .value
            );


        const producto = {

            nombre,
            descripcion,
            precio,
            stock,

            activo:
                productoEditandoId === null
                    ? true
                    : productoEditandoActivo
        };


        try {

            let response;


            if (productoEditandoId === null) {

                response =
                    await fetch(
                        API_PRODUCTOS,
                        {

                            method:
                                "POST",

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

            } else {

                response =
                    await fetch(
                        `${API_PRODUCTOS}/${productoEditandoId}`,
                        {

                            method:
                                "PUT",

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
            }


            const respuesta =
                await response.json();


            if (!response.ok) {

                throw new Error(
                    respuesta.mensaje
                    ||
                    "No fue posible guardar el producto."
                );
            }


            limpiarFormularioProducto();


            await cargarProductos();


        } catch (error) {

            mostrarMensajeProducto(
                error.message,
                "error"
            );
        }
    }
);


btnCancelarEdicion.addEventListener(
    "click",
    limpiarFormularioProducto
);


async function cargarProductos() {

    try {

        const response =
            await fetch(API_PRODUCTOS);


        productosGuardados =
            await response.json();


        mostrarProductos(
            productosGuardados
        );


        totalProductosActivos.textContent =
            productosGuardados.filter(
                producto =>
                    producto.activo
            ).length;


    } catch (error) {

        console.error(error);
    }
}


function mostrarProductos(productos) {

    tablaProductos.innerHTML =
        "";


    productos.forEach(
        producto => {

            const fila =
                document.createElement("tr");


            fila.innerHTML = `

                <td>
                    #${producto.id}
                </td>

                <td>

                    <strong>
                        ${producto.nombre}
                    </strong>

                    <br>

                    <small>
                        ${producto.descripcion || ""}
                    </small>

                </td>

                <td>
                    $${formatearPrecio(producto.precio)}
                </td>

                <td>
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

                    <button
                        class="boton-guardar"
                        onclick="editarProducto(${producto.id})"
                    >
                        Editar
                    </button>

                    <button
                        class="boton-guardar"
                        onclick="cambiarEstadoProducto(
                            ${producto.id},
                            ${producto.activo}
                        )"
                    >

                        ${
                            producto.activo
                                ? "Desactivar"
                                : "Activar"
                        }

                    </button>

                </td>
            `;


            tablaProductos.appendChild(
                fila
            );
        }
    );
}


function editarProducto(id) {

    const producto =
        productosGuardados.find(
            p =>
                p.id === id
        );


    if (!producto) {
        return;
    }


    productoEditandoId =
        producto.id;


    productoEditandoActivo =
        producto.activo;


    nombreProducto.value =
        producto.nombre;


    descripcionProducto.value =
        producto.descripcion;


    precioProducto.value =
        producto.precio;


    stockProducto.value =
        producto.stock;


    btnGuardarProducto.textContent =
        "Guardar cambios";


    btnCancelarEdicion.style.display =
        "inline-block";
}


async function cambiarEstadoProducto(
    id,
    estadoActual
) {

    const response =
        await fetch(
            `${API_PRODUCTOS}/${id}/estado`,
            {

                method:
                    "PUT",

                headers: {

                    "Content-Type":
                        "application/json"
                },

                body:
                    JSON.stringify({

                        activo:
                            !estadoActual

                    })
            }
        );


    if (response.ok) {

        await cargarProductos();

    }
}


function limpiarFormularioProducto() {

    formProducto.reset();


    productoEditandoId =
        null;


    productoEditandoActivo =
        true;


    btnGuardarProducto.textContent =
        "+ Agregar producto";


    btnCancelarEdicion.style.display =
        "none";
}


/* =========================================================
   PEDIDOS
   ========================================================= */

async function cargarPedidos() {

    try {

        const response =
            await fetch(API_PEDIDOS);


        pedidosGuardados =
            await response.json();


        mostrarPedidosSeparados(
            pedidosGuardados
        );


        actualizarResumen();


    } catch (error) {

        console.error(error);
    }
}


/* =========================================================
   SEPARAR PEDIDOS
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


    mostrarPedidosActivos(
        activos
    );


    mostrarPedidosHistoricos(
        tablaPedidosCancelados,
        cancelados
    );


    mostrarPedidosHistoricos(
        tablaPedidosCompletados,
        completados
    );
}


/* =========================================================
   PEDIDOS ACTIVOS
   ========================================================= */

function mostrarPedidosActivos(
    pedidos
) {

    tablaPedidosActivos.innerHTML =
        "";


    if (pedidos.length === 0) {

        tablaPedidosActivos.innerHTML = `

            <tr>

                <td colspan="7">
                    No existen pedidos activos.
                </td>

            </tr>
        `;

        return;
    }


    pedidos.forEach(
        pedido => {

            const fila =
                document.createElement("tr");


            fila.innerHTML =
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
   HISTÓRICOS
   ========================================================= */

function mostrarPedidosHistoricos(
    tabla,
    pedidos
) {

    tabla.innerHTML =
        "";


    if (pedidos.length === 0) {

        tabla.innerHTML = `

            <tr>

                <td colspan="6">
                    No existen pedidos en esta categoría.
                </td>

            </tr>
        `;

        return;
    }


    pedidos.forEach(
        pedido => {

            const fila =
                document.createElement("tr");


            fila.innerHTML =
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
   CREAR FILA
   ========================================================= */

function crearFilaPedido(
    pedido,
    editable
) {

    return `

        <td>
            ${
                pedido.codigoPedido
                ||
                `#${pedido.id}`
            }
        </td>

        <td>
            ${pedido.cliente || "-"}
        </td>

        <td>
            ${pedido.producto || "-"}
        </td>

        <td>
            ${pedido.cantidad || "-"}
        </td>

        <td>
            ${formatearModalidad(
                pedido.modalidadEntrega
            )}
        </td>

        <td>
            ${formatearEstado(
                pedido.estado
            )}
        </td>

        ${
            editable
                ? `

                    <td>

                        <select
                            id="estado-${pedido.id}"
                        >
                            ${crearOpcionesEstado(pedido)}
                        </select>

                        <button
                            class="boton-guardar"
                            onclick="actualizarEstado(${pedido.id})"
                        >
                            Guardar
                        </button>

                    </td>

                  `
                : ""
        }
    `;
}


/* =========================================================
   ESTADOS
   ========================================================= */

function crearOpcionesEstado(
    pedido
) {

    let estados = [];


    if (
        pedido.modalidadEntrega
        === "RETIRO"
    ) {

        estados = [

            "PENDIENTE_PAGO",
            "PAGADO",
            "RECHAZADO",
            "PREPARANDO",
            "LISTO_RETIRO",
            "ENTREGADO",
            "CANCELADO"

        ];

    } else {

        estados = [

            "PENDIENTE_PAGO",
            "PAGADO",
            "RECHAZADO",
            "PREPARANDO",
            "DESPACHADO",
            "ENTREGADO",
            "CANCELADO"

        ];
    }


    return estados
        .map(
            estado => `

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
        .join("");
}


async function actualizarEstado(
    id
) {

    const selector =
        document.getElementById(
            `estado-${id}`
        );


    const estado =
        selector.value;


    const response =
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
                    JSON.stringify({
                        estado
                    })
            }
        );


    if (response.ok) {

        await cargarPedidos();

        await cargarProductos();

    }
}


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

                    return (

                        (pedido.codigoPedido || "")
                            .toLowerCase()
                            .includes(texto)

                        ||

                        (pedido.cliente || "")
                            .toLowerCase()
                            .includes(texto)

                        ||

                        (pedido.producto || "")
                            .toLowerCase()
                            .includes(texto)

                        ||

                        (pedido.estado || "")
                            .toLowerCase()
                            .includes(texto)

                    );
                }
            );


        mostrarPedidosSeparados(
            filtrados
        );
    }
);


btnActualizar.addEventListener(
    "click",
    async () => {

        await cargarPedidos();

        await cargarProductos();

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


    totalCompletados.textContent =
        pedidosGuardados.filter(
            pedido =>
                pedido.estado
                === "ENTREGADO"
        ).length;
}


/* =========================================================
   UTILIDADES
   ========================================================= */

function formatearPrecio(
    precio
) {

    return Number(precio)
        .toLocaleString(
            "es-CL"
        );
}


function formatearModalidad(
    modalidad
) {

    if (modalidad === "RETIRO") {
        return "Retiro";
    }


    if (modalidad === "DESPACHO") {
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

        PAGADO:
            "Pagado",

        RECHAZADO:
            "Rechazado",

        PREPARANDO:
            "Preparando",

        LISTO_RETIRO:
            "Listo para retiro",

        DESPACHADO:
            "Despachado",

        ENTREGADO:
            "Entregado",

        CANCELADO:
            "Cancelado"
    };


    return estados[estado]
        ||
        estado
        ||
        "-";
}


function mostrarMensajeProducto(
    texto,
    tipo
) {

    mensajeProducto.textContent =
        texto;


    mensajeProducto.className =
        `mensaje ${tipo}`;
}