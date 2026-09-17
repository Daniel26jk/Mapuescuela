const API_URL = "webapi/pedidos";

const formPedido =
    document.getElementById("formPedido");

const tablaPedidos =
    document.getElementById("tablaPedidos");

const mensaje =
    document.getElementById("mensaje");

const btnActualizar =
    document.getElementById("btnActualizar");

const buscarPedido =
    document.getElementById("buscarPedido");

const totalPedidos =
    document.getElementById("totalPedidos");

const totalPendientes =
    document.getElementById("totalPendientes");

const totalPagados =
    document.getElementById("totalPagados");


let pedidosGuardados = [];


/* CREAR PEDIDO */

formPedido.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const cliente =
            document
                .getElementById("cliente")
                .value
                .trim();

        const producto =
            document
                .getElementById("producto")
                .value
                .trim();

        const modalidadEntrega =
            document
                .getElementById("modalidadEntrega")
                .value;


        const pedido = {
            cliente,
            producto,
            modalidadEntrega
        };


        try {

            const response =
                await fetch(
                    API_URL,
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(pedido)
                    }
                );


            if (!response.ok) {
                throw new Error(
                    "No se pudo crear el pedido."
                );
            }


            const pedidoCreado =
                await response.json();


            mostrarMensaje(
                `Pedido #${pedidoCreado.id} creado correctamente.`,
                "exito"
            );


            formPedido.reset();


            await cargarPedidos();


        } catch (error) {

            mostrarMensaje(
                error.message,
                "error"
            );

        }

    }
);


/* ACTUALIZAR LISTA */

btnActualizar.addEventListener(
    "click",
    cargarPedidos
);


/* BUSCADOR */

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
                (pedido) => {

                    return (

                        String(pedido.id)
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

                        ||

                        (pedido.modalidadEntrega || "")
                            .toLowerCase()
                            .includes(texto)

                    );

                }
            );


        mostrarPedidos(filtrados);

    }
);


/* CARGAR PEDIDOS */

async function cargarPedidos() {

    tablaPedidos.innerHTML = `
        <tr>
            <td
                colspan="6"
                class="cargando"
            >
                Cargando pedidos...
            </td>
        </tr>
    `;


    try {

        const response =
            await fetch(API_URL);


        if (!response.ok) {

            throw new Error(
                "No se pudieron cargar los pedidos."
            );

        }


        pedidosGuardados =
            await response.json();


        mostrarPedidos(
            pedidosGuardados
        );


        actualizarResumen();


    } catch (error) {

        tablaPedidos.innerHTML = `
            <tr>
                <td
                    colspan="6"
                    class="cargando"
                >
                    Error al cargar los pedidos.
                </td>
            </tr>
        `;

        console.error(error);

    }

}


/* MOSTRAR PEDIDOS */

function mostrarPedidos(pedidos) {

    tablaPedidos.innerHTML = "";


    if (pedidos.length === 0) {

        tablaPedidos.innerHTML = `
            <tr>
                <td
                    colspan="6"
                    class="cargando"
                >
                    No se encontraron pedidos.
                </td>
            </tr>
        `;

        return;
    }


    pedidos.forEach(
        (pedido) => {

            const fila =
                document.createElement("tr");


            fila.innerHTML = `

                <td>
                    #${pedido.id}
                </td>


                <td>
                    <strong>
                        ${pedido.cliente ?? ""}
                    </strong>
                </td>


                <td>
                    ${pedido.producto ?? ""}
                </td>


                <td>
                    ${
                        pedido.modalidadEntrega
                        === "RETIRO"
                        ? "Retiro"
                        : "Despacho"
                    }
                </td>


                <td>
                    ${crearEstadoVisual(
                        pedido.estado
                    )}
                </td>


                <td>

                    <select
                        id="estado-${pedido.id}"
                    >

                        <option
                            value="PENDIENTE_PAGO"
                            ${
                                pedido.estado
                                === "PENDIENTE_PAGO"
                                ? "selected"
                                : ""
                            }
                        >
                            Pendiente de pago
                        </option>


                        <option
                            value="PAGADO"
                            ${
                                pedido.estado
                                === "PAGADO"
                                ? "selected"
                                : ""
                            }
                        >
                            Pagado
                        </option>


                        <option
                            value="RECHAZADO"
                            ${
                                pedido.estado
                                === "RECHAZADO"
                                ? "selected"
                                : ""
                            }
                        >
                            Rechazado
                        </option>


                        <option
                            value="PREPARANDO"
                            ${
                                pedido.estado
                                === "PREPARANDO"
                                ? "selected"
                                : ""
                            }
                        >
                            Preparando
                        </option>


                        <option
                            value="LISTO_RETIRO"
                            ${
                                pedido.estado
                                === "LISTO_RETIRO"
                                ? "selected"
                                : ""
                            }
                        >
                            Listo para retiro
                        </option>


                        <option
                            value="DESPACHADO"
                            ${
                                pedido.estado
                                === "DESPACHADO"
                                ? "selected"
                                : ""
                            }
                        >
                            Despachado
                        </option>


                        <option
                            value="CANCELADO"
                            ${
                                pedido.estado
                                === "CANCELADO"
                                ? "selected"
                                : ""
                            }
                        >
                            Cancelado
                        </option>

                    </select>


                    <button
                        type="button"
                        class="boton-guardar"
                        onclick="actualizarEstado(
                            ${pedido.id}
                        )"
                    >
                        Guardar
                    </button>

                </td>
            `;


            tablaPedidos
                .appendChild(fila);

        }
    );

}


/* ESTADO VISUAL */

function crearEstadoVisual(estado) {

    const estados = {

        PENDIENTE_PAGO: {
            texto:
                "Pendiente de pago",
            clase:
                "estado-pendiente"
        },


        PAGADO: {
            texto:
                "Pagado",
            clase:
                "estado-pagado"
        },


        RECHAZADO: {
            texto:
                "Rechazado",
            clase:
                "estado-rechazado"
        },


        PREPARANDO: {
            texto:
                "Preparando",
            clase:
                "estado-preparando"
        },


        LISTO_RETIRO: {
            texto:
                "Listo para retiro",
            clase:
                "estado-retiro"
        },


        DESPACHADO: {
            texto:
                "Despachado",
            clase:
                "estado-despachado"
        },


        CANCELADO: {
            texto:
                "Cancelado",
            clase:
                "estado-cancelado"
        }

    };


    const datos =
        estados[estado]
        ||
        {
            texto:
                estado || "Sin estado",

            clase:
                "estado-cancelado"
        };


    return `
        <span
            class="estado ${datos.clase}"
        >
            ${datos.texto}
        </span>
    `;

}


/* ACTUALIZAR ESTADO */

async function actualizarEstado(id) {

    const selector =
        document.getElementById(
            `estado-${id}`
        );


    const nuevoEstado =
        selector.value;


    try {

        const response =
            await fetch(
                `${API_URL}/${id}/estado`,
                {

                    method: "PUT",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            {
                                estado:
                                    nuevoEstado
                            }
                        )

                }
            );


        if (!response.ok) {

            throw new Error(
                "No se pudo actualizar el estado."
            );

        }


        mostrarMensaje(
            `Pedido #${id} actualizado correctamente.`,
            "exito"
        );


        await cargarPedidos();


    } catch (error) {

        mostrarMensaje(
            error.message,
            "error"
        );

    }

}


/* RESUMEN SUPERIOR */

function actualizarResumen() {

    totalPedidos.textContent =
        pedidosGuardados.length;


    totalPendientes.textContent =
        pedidosGuardados.filter(
            pedido =>
                pedido.estado
                === "PENDIENTE_PAGO"
        ).length;


    totalPagados.textContent =
        pedidosGuardados.filter(
            pedido =>
                pedido.estado
                === "PAGADO"
        ).length;

}


/* MENSAJES */

function mostrarMensaje(
    texto,
    tipo
) {

    mensaje.textContent =
        texto;


    mensaje.className =
        `mensaje ${tipo}`;


    setTimeout(
        () => {

            mensaje.className =
                "mensaje";

        },
        5000
    );

}


/* CARGA INICIAL */

cargarPedidos();