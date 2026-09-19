const API_PRODUCTOS =
    "webapi/productos";

const API_PEDIDOS =
    "webapi/pedidos";


const listaProductos =
    document.getElementById("listaProductos");

const buscarProducto =
    document.getElementById("buscarProducto");


const formPedido =
    document.getElementById("formPedidoCliente");

const productoId =
    document.getElementById("productoId");

const productoSeleccionado =
    document.getElementById("productoSeleccionado");

const cantidad =
    document.getElementById("cantidad");

const detalleProducto =
    document.getElementById("detalleProducto");

const mensajeCliente =
    document.getElementById("mensajeCliente");


const resultadoPedido =
    document.getElementById("resultadoPedido");

const codigoPedidoCliente =
    document.getElementById("codigoPedidoCliente");

const estadoPedidoCliente =
    document.getElementById("estadoPedidoCliente");


const formConsultarPedido =
    document.getElementById("formConsultarPedido");

const consultaPedido =
    document.getElementById("consultaPedido");

const mensajeConsulta =
    document.getElementById("mensajeConsulta");

const listaPedidosCliente =
    document.getElementById("listaPedidosCliente");


let productosDisponibles = [];

let productoActual = null;


/* =========================================================
   INICIO
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    cargarProductos
);


/* =========================================================
   PRODUCTOS
   ========================================================= */

async function cargarProductos() {

    listaProductos.innerHTML = `
        <p class="cargando">
            Cargando productos...
        </p>
    `;


    try {

        const response =
            await fetch(API_PRODUCTOS);


        if (!response.ok) {

            throw new Error(
                "No fue posible cargar los productos."
            );
        }


        const productos =
            await response.json();


        productosDisponibles =
            productos.filter(
                producto =>
                    producto.activo === true
                    &&
                    producto.stock > 0
            );


        mostrarProductos(
            productosDisponibles
        );


    } catch (error) {

        listaProductos.innerHTML = `
            <p class="cargando">
                No fue posible cargar los productos.
            </p>
        `;

        console.error(error);
    }
}


buscarProducto.addEventListener(
    "input",
    () => {

        const texto =
            buscarProducto
                .value
                .toLowerCase()
                .trim();


        const filtrados =
            productosDisponibles.filter(
                producto =>
                    (producto.nombre || "")
                        .toLowerCase()
                        .includes(texto)
            );


        mostrarProductos(
            filtrados
        );
    }
);


function mostrarProductos(productos) {

    listaProductos.innerHTML = "";


    if (productos.length === 0) {

        listaProductos.innerHTML = `
            <p class="cargando">
                No se encontraron productos disponibles.
            </p>
        `;

        return;
    }


    productos.forEach(
        producto => {

            const tarjeta =
                document.createElement("article");


            tarjeta.className =
                "tarjeta-producto-cliente";


            tarjeta.innerHTML = `

                <h4>
                    ${producto.nombre}
                </h4>

                <p>
                    ${
                        producto.descripcion
                        ||
                        "Sin descripción"
                    }
                </p>

                <div class="producto-datos">

                    <span>
                        Precio:
                        <strong>
                            $${formatearPrecio(producto.precio)}
                        </strong>
                    </span>

                    <span>
                        Stock:
                        <strong>
                            ${producto.stock}
                        </strong>
                    </span>

                </div>

                <button
                    type="button"
                    class="boton-seleccionar-producto"
                >
                    Seleccionar
                </button>
            `;


            const boton =
                tarjeta.querySelector(
                    ".boton-seleccionar-producto"
                );


            boton.addEventListener(
                "click",
                () =>
                    seleccionarProducto(
                        producto
                    )
            );


            listaProductos.appendChild(
                tarjeta
            );
        }
    );
}


function seleccionarProducto(producto) {

    productoActual =
        producto;


    productoId.value =
        producto.id;


    productoSeleccionado.value =
        producto.nombre;


    cantidad.value =
        1;


    cantidad.max =
        producto.stock;


    detalleProducto.innerHTML = `

        <strong>
            Producto seleccionado:
        </strong>

        ${producto.nombre}

        <br>

        <strong>
            Precio unitario:
        </strong>

        $${formatearPrecio(producto.precio)}

        <br>

        <strong>
            Stock disponible:
        </strong>

        ${producto.stock}
    `;


    mostrarMensaje(
        mensajeCliente,
        `Producto "${producto.nombre}" seleccionado.`,
        "exito"
    );
}


/* =========================================================
   CREAR PEDIDO
   ========================================================= */

formPedido.addEventListener(
    "submit",
    async event => {

        event.preventDefault();


        const cliente =
            document
                .getElementById("cliente")
                .value
                .trim();


        const modalidadEntrega =
            document
                .getElementById("modalidadEntrega")
                .value;


        const idProducto =
            Number(
                productoId.value
            );


        const cantidadPedido =
            Number(
                cantidad.value
            );


        if (
            !productoActual
            ||
            idProducto <= 0
        ) {

            mostrarMensaje(
                mensajeCliente,
                "Debes seleccionar un producto.",
                "error"
            );

            return;
        }


        if (
            cantidadPedido <= 0
            ||
            cantidadPedido > productoActual.stock
        ) {

            mostrarMensaje(
                mensajeCliente,
                "La cantidad solicitada no es válida.",
                "error"
            );

            return;
        }


        const pedido = {

            cliente:
                cliente,

            productoId:
                idProducto,

            cantidad:
                cantidadPedido,

            modalidadEntrega:
                modalidadEntrega
        };


        try {

            const response =
                await fetch(
                    API_PEDIDOS,
                    {

                        method:
                            "POST",

                        headers: {

                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                pedido
                            )
                    }
                );


            const respuesta =
                await response.json();


            if (!response.ok) {

                throw new Error(
                    respuesta.mensaje
                    ||
                    "No fue posible crear el pedido."
                );
            }


            mostrarMensaje(
                mensajeCliente,
                "Pedido creado correctamente.",
                "exito"
            );


            codigoPedidoCliente.textContent =
                respuesta.codigoPedido
                ||
                "-";


            estadoPedidoCliente.textContent =
                formatearEstado(
                    respuesta.estado
                );


            resultadoPedido.style.display =
                "block";


            /*
             * Dejamos preparado el buscador
             * con el código recién generado.
             */
            consultaPedido.value =
                respuesta.codigoPedido
                ||
                respuesta.cliente
                ||
                "";


            formPedido.reset();


            productoId.value =
                "";


            productoSeleccionado.value =
                "";


            detalleProducto.innerHTML =
                "";


            productoActual =
                null;


            await cargarProductos();


            resultadoPedido.scrollIntoView({
                behavior: "smooth",
                block: "center"
            });


        } catch (error) {

            mostrarMensaje(
                mensajeCliente,
                error.message,
                "error"
            );
        }
    }
);


/* =========================================================
   CONSULTAR PEDIDOS
   ========================================================= */

formConsultarPedido.addEventListener(
    "submit",
    async event => {

        event.preventDefault();


        const busqueda =
            consultaPedido
                .value
                .trim();


        if (!busqueda) {

            mostrarMensaje(
                mensajeConsulta,
                "Debes indicar un nombre o código de pedido.",
                "error"
            );

            return;
        }


        const url =
            `${API_PEDIDOS}/consultar`
            +
            `?busqueda=${encodeURIComponent(busqueda)}`;


        try {

            const response =
                await fetch(url);


            const respuesta =
                await response.json();


            if (!response.ok) {

                listaPedidosCliente.innerHTML =
                    "";


                throw new Error(
                    respuesta.mensaje
                    ||
                    "No fue posible consultar los pedidos."
                );
            }


            /*
             * El endpoint devuelve una lista,
             * incluso cuando buscamos por código.
             */
            const pedidos =
                Array.isArray(respuesta)
                    ? respuesta
                    : [respuesta];


            mostrarMensaje(
                mensajeConsulta,
                pedidos.length === 1
                    ? "Se encontró 1 pedido."
                    : `Se encontraron ${pedidos.length} pedidos.`,
                "exito"
            );


            mostrarPedidosCliente(
                pedidos
            );


        } catch (error) {

            mostrarMensaje(
                mensajeConsulta,
                error.message,
                "error"
            );
        }
    }
);


function mostrarPedidosCliente(
    pedidos
) {

    listaPedidosCliente.innerHTML =
        "";


    pedidos.forEach(
        pedido => {

            const tarjeta =
                document.createElement("article");


            tarjeta.className =
                "tarjeta-producto-cliente";


            tarjeta.innerHTML = `

                <h4>
                    ${pedido.codigoPedido || "Pedido"}
                </h4>

                <p>
                    <strong>
                        Comprador:
                    </strong>

                    ${pedido.cliente || "-"}
                </p>

                <p>
                    <strong>
                        Producto:
                    </strong>

                    ${pedido.producto || "-"}
                </p>

                <div class="producto-datos">

                    <span>
                        Cantidad:
                        <strong>
                            ${pedido.cantidad || "-"}
                        </strong>
                    </span>

                    <span>
                        Entrega:
                        <strong>
                            ${formatearModalidad(
                                pedido.modalidadEntrega
                            )}
                        </strong>
                    </span>

                </div>

                <div class="detalle-producto-cliente">

                    <strong>
                        Estado actual:
                    </strong>

                    ${formatearEstado(
                        pedido.estado
                    )}

                </div>
            `;


            listaPedidosCliente.appendChild(
                tarjeta
            );
        }
    );
}


/* =========================================================
   UTILIDADES
   ========================================================= */

function mostrarMensaje(
    elemento,
    texto,
    tipo
) {

    elemento.textContent =
        texto;


    elemento.className =
        `mensaje ${tipo}`;
}


function formatearPrecio(precio) {

    return Number(precio)
        .toLocaleString(
            "es-CL"
        );
}


function formatearEstado(estado) {

    switch (estado) {

        case "PENDIENTE_PAGO":
            return "Pendiente de pago";

        case "PAGADO":
            return "Pagado";

        case "RECHAZADO":
            return "Pago rechazado";

        case "PREPARANDO":
            return "Preparando";

        case "LISTO_RETIRO":
            return "Listo para retiro";

        case "DESPACHADO":
            return "Despachado";

        case "ENTREGADO":
            return "Entregado";

        case "CANCELADO":
            return "Cancelado";

        default:
            return estado || "-";
    }
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