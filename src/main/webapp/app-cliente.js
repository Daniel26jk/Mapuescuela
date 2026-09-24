const API_PRODUCTOS = "webapi/productos";
const API_PEDIDOS = "webapi/pedidos";

const listaProductos = document.getElementById("listaProductos");
const buscarProducto = document.getElementById("buscarProducto");
const tablaCarrito = document.getElementById("tablaCarrito");
const totalCarrito = document.getElementById("totalCarrito");
const formPedido = document.getElementById("formPedidoCliente");
const modalidadEntrega = document.getElementById("modalidadEntrega");
const campoDireccion = document.getElementById("campoDireccion");
const campoComuna = document.getElementById("campoComuna");
const direccionEntrega = document.getElementById("direccionEntrega");
const comunaEntrega = document.getElementById("comunaEntrega");
const mensajeCliente = document.getElementById("mensajeCliente");
const resultadoPedido = document.getElementById("resultadoPedido");
const codigoPedidoCliente = document.getElementById("codigoPedidoCliente");
const estadoPedidoCliente = document.getElementById("estadoPedidoCliente");
const totalPedidoCliente = document.getElementById("totalPedidoCliente");
const formComprobante = document.getElementById("formComprobante");
const archivoComprobante = document.getElementById("archivoComprobante");
const mensajeComprobante = document.getElementById("mensajeComprobante");
const formConsultarPedido = document.getElementById("formConsultarPedido");
const consultaPedido = document.getElementById("consultaPedido");
const mensajeConsulta = document.getElementById("mensajeConsulta");
const listaPedidosCliente = document.getElementById("listaPedidosCliente");

let productosDisponibles = [];
let carrito = [];
let pedidoCreado = null;

document.addEventListener("DOMContentLoaded", cargarProductos);

modalidadEntrega.addEventListener("change", () => {
    const despacho = modalidadEntrega.value === "DESPACHO";
    campoDireccion.style.display = despacho ? "block" : "none";
    campoComuna.style.display = despacho ? "block" : "none";
    direccionEntrega.required = despacho;
    comunaEntrega.required = despacho;
    if (!despacho) {
        direccionEntrega.value = "";
        comunaEntrega.value = "";
    }
});

buscarProducto.addEventListener("input", () => {
    const texto = buscarProducto.value.toLowerCase().trim();
    mostrarProductos(productosDisponibles.filter(p =>
        (p.nombre || "").toLowerCase().includes(texto) ||
        (p.categoria || "").toLowerCase().includes(texto)
    ));
});

async function cargarProductos() {
    listaProductos.innerHTML = '<p class="cargando">Cargando productos...</p>';
    try {
        const response = await fetch(API_PRODUCTOS);
        if (!response.ok) throw new Error("No fue posible cargar los productos.");
        const productos = await response.json();
        productosDisponibles = productos.filter(p => p.activo === true && p.stock > 0);
        mostrarProductos(productosDisponibles);
    } catch (error) {
        listaProductos.innerHTML = '<p class="cargando">No fue posible cargar los productos.</p>';
        console.error(error);
    }
}

function mostrarProductos(productos) {
    listaProductos.innerHTML = "";
    if (productos.length === 0) {
        listaProductos.innerHTML = '<p class="cargando">No se encontraron productos disponibles.</p>';
        return;
    }

    productos.forEach(producto => {
        const tarjeta = document.createElement("article");
        tarjeta.className = "tarjeta-producto-cliente tarjeta-producto-completa";
        const imagen = producto.imagenUrl
            ? `<img src="${escaparAtributo(producto.imagenUrl)}" alt="${escaparAtributo(producto.nombre)}" class="producto-imagen" onerror="this.style.display='none'">`
            : `<div class="producto-imagen producto-sin-imagen">Sin imagen</div>`;

        tarjeta.innerHTML = `
            ${imagen}
            <div class="producto-contenido">
                <span class="producto-categoria">${escaparHtml(producto.categoria || "Sin categoría")}</span>
                <h4>${escaparHtml(producto.nombre)}</h4>
                <p>${escaparHtml(producto.descripcion || "Sin descripción")}</p>
                <div class="producto-datos"><span>Precio: <strong>$${formatearPrecio(producto.precio)}</strong></span><span>Stock: <strong>${producto.stock}</strong></span></div>
                <div class="agregar-carrito-linea">
                    <input type="number" min="1" max="${producto.stock}" value="1" class="cantidad-tarjeta">
                    <button type="button" class="boton-seleccionar-producto">Agregar al carrito</button>
                </div>
            </div>`;

        tarjeta.querySelector("button").addEventListener("click", () => {
            const cantidad = Number(tarjeta.querySelector(".cantidad-tarjeta").value);
            agregarAlCarrito(producto, cantidad);
        });
        listaProductos.appendChild(tarjeta);
    });
}

function agregarAlCarrito(producto, cantidad) {
    if (!Number.isInteger(cantidad) || cantidad <= 0 || cantidad > producto.stock) {
        alert("La cantidad seleccionada no es válida.");
        return;
    }

    const existente = carrito.find(i => i.producto.id === producto.id);
    const totalDeseado = (existente ? existente.cantidad : 0) + cantidad;
    if (totalDeseado > producto.stock) {
        alert(`Solo hay ${producto.stock} unidades disponibles de ${producto.nombre}.`);
        return;
    }

    if (existente) existente.cantidad = totalDeseado;
    else carrito.push({ producto, cantidad });

    renderCarrito();
}

function renderCarrito() {
    if (carrito.length === 0) {
        tablaCarrito.innerHTML = '<tr><td colspan="5">Tu carrito está vacío.</td></tr>';
        totalCarrito.textContent = "$0";
        return;
    }

    tablaCarrito.innerHTML = "";
    let total = 0;
    carrito.forEach(item => {
        const subtotal = item.producto.precio * item.cantidad;
        total += subtotal;
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td>${escaparHtml(item.producto.nombre)}</td>
            <td>$${formatearPrecio(item.producto.precio)}</td>
            <td><input class="cantidad-carrito" type="number" min="1" max="${item.producto.stock}" value="${item.cantidad}"></td>
            <td>$${formatearPrecio(subtotal)}</td>
            <td><button type="button" class="boton-peligro-pequeno">Quitar</button></td>`;

        fila.querySelector(".cantidad-carrito").addEventListener("change", e => {
            const nueva = Number(e.target.value);
            if (nueva > 0 && nueva <= item.producto.stock) item.cantidad = nueva;
            else e.target.value = item.cantidad;
            renderCarrito();
        });
        fila.querySelector("button").addEventListener("click", () => {
            carrito = carrito.filter(i => i.producto.id !== item.producto.id);
            renderCarrito();
        });
        tablaCarrito.appendChild(fila);
    });
    totalCarrito.textContent = `$${formatearPrecio(total)}`;
}

formPedido.addEventListener("submit", async event => {
    event.preventDefault();
    if (carrito.length === 0) {
        mostrarMensaje(mensajeCliente, "Agrega al menos un producto al carrito.", "error");
        return;
    }

    const pedido = {
        cliente: document.getElementById("cliente").value.trim(),
        email: document.getElementById("email").value.trim(),
        telefono: document.getElementById("telefono").value.trim(),
        modalidadEntrega: modalidadEntrega.value,
        direccionEntrega: direccionEntrega.value.trim(),
        comunaEntrega: comunaEntrega.value.trim(),
        items: carrito.map(item => ({ productoId: item.producto.id, cantidad: item.cantidad }))
    };

    try {
        const response = await fetch(API_PEDIDOS, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(pedido) });
        const respuesta = await response.json();
        if (!response.ok) throw new Error(respuesta.mensaje || "No fue posible crear el pedido.");

        pedidoCreado = respuesta;
        codigoPedidoCliente.textContent = respuesta.codigoPedido || "-";
        estadoPedidoCliente.textContent = formatearEstado(respuesta.estado);
        totalPedidoCliente.textContent = `$${formatearPrecio(respuesta.total || 0)}`;
        consultaPedido.value = respuesta.codigoPedido || "";
        resultadoPedido.style.display = "block";
        mostrarMensaje(mensajeCliente, "Pedido creado correctamente.", "exito");
        carrito = [];
        renderCarrito();
        resultadoPedido.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
        mostrarMensaje(mensajeCliente, error.message, "error");
    }
});

formComprobante.addEventListener("submit", async event => {
    event.preventDefault();
    if (!pedidoCreado) {
        mostrarMensaje(mensajeComprobante, "Primero debes crear un pedido.", "error");
        return;
    }

    const archivo = archivoComprobante.files[0];
    if (!archivo) return;
    if (archivo.size > 5 * 1024 * 1024) {
        mostrarMensaje(mensajeComprobante, "El archivo supera el máximo de 5 MB.", "error");
        return;
    }

    try {
        const dataUrl = await leerArchivo(archivo);
        const base64 = dataUrl.substring(dataUrl.indexOf(",") + 1);
        const response = await fetch(`${API_PEDIDOS}/${pedidoCreado.id}/comprobante`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nombreArchivo: archivo.name, tipoContenido: archivo.type || "application/octet-stream", datosBase64: base64 })
        });
        const respuesta = await response.json();
        if (!response.ok) throw new Error(respuesta.mensaje || "No fue posible adjuntar el comprobante.");

        pedidoCreado.estado = "PAGO_REVISION";
        pedidoCreado.comprobanteAdjunto = true;
        estadoPedidoCliente.textContent = formatearEstado("PAGO_REVISION");
        mostrarMensaje(mensajeComprobante, "Comprobante adjuntado. El pago quedó en revisión.", "exito");
        archivoComprobante.value = "";
    } catch (error) {
        mostrarMensaje(mensajeComprobante, error.message, "error");
    }
});

formConsultarPedido.addEventListener("submit", async event => {
    event.preventDefault();
    listaPedidosCliente.innerHTML = "";
    try {
        const response = await fetch(`${API_PEDIDOS}/consultar?busqueda=${encodeURIComponent(consultaPedido.value.trim())}`);
        const respuesta = await response.json();
        if (!response.ok) throw new Error(respuesta.mensaje || "No fue posible consultar el pedido.");
        if (respuesta.length === 0) {
            mostrarMensaje(mensajeConsulta, "No se encontraron pedidos.", "error");
            return;
        }
        mostrarMensaje(mensajeConsulta, `${respuesta.length} pedido(s) encontrado(s).`, "exito");
        respuesta.forEach(mostrarPedidoConsultado);
    } catch (error) {
        mostrarMensaje(mensajeConsulta, error.message, "error");
    }
});

function mostrarPedidoConsultado(pedido) {
    const tarjeta = document.createElement("article");
    tarjeta.className = "pedido-cliente-card";
    const items = (pedido.items || []).map(i => `${escaparHtml(i.producto)} x${i.cantidad}`).join(", ") || escaparHtml(pedido.producto || "-");
    tarjeta.innerHTML = `
        <div class="pedido-card-encabezado"><strong>${escaparHtml(pedido.codigoPedido || `#${pedido.id}`)}</strong><span class="estado-etiqueta">${formatearEstado(pedido.estado)}</span></div>
        <p><strong>Productos:</strong> ${items}</p>
        <p><strong>Total:</strong> $${formatearPrecio(pedido.total || 0)}</p>
        <p><strong>Entrega:</strong> ${formatearModalidad(pedido.modalidadEntrega)}</p>
        ${pedido.modalidadEntrega === "DESPACHO" ? `<p><strong>Dirección:</strong> ${escaparHtml(pedido.direccionEntrega || "-")}, ${escaparHtml(pedido.comunaEntrega || "-")}</p>` : ""}
        <p><strong>Comprobante:</strong> ${pedido.comprobanteAdjunto ? "Adjuntado" : "Pendiente"}</p>
        ${pedido.numeroSeguimiento ? `<p><strong>Seguimiento:</strong> ${escaparHtml(pedido.empresaTransporte || "-")} · ${escaparHtml(pedido.numeroSeguimiento)}</p>` : ""}`;
    listaPedidosCliente.appendChild(tarjeta);
}

function leerArchivo(archivo) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = () => reject(new Error("No fue posible leer el archivo."));
        reader.readAsDataURL(archivo);
    });
}

function formatearPrecio(precio) { return Number(precio || 0).toLocaleString("es-CL"); }
function formatearModalidad(valor) { return valor === "RETIRO" ? "Retiro" : valor === "DESPACHO" ? "Despacho" : (valor || "-"); }
function formatearEstado(estado) {
    const estados = { PENDIENTE_PAGO: "Pendiente de pago", PAGO_REVISION: "Pago en revisión", PAGADO: "Pago aprobado", RECHAZADO: "Pago rechazado", PREPARANDO: "En preparación", LISTO_RETIRO: "Listo para retiro", DESPACHADO: "Enviado", ENTREGADO: "Finalizado", CANCELADO: "Cancelado" };
    return estados[estado] || estado || "-";
}
function mostrarMensaje(elemento, texto, tipo) { elemento.textContent = texto; elemento.className = "mensaje" + (tipo ? ` ${tipo}` : ""); }
function escaparHtml(valor) { return String(valor == null ? "" : valor).replace(/[&<>'"]/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;","'":"&#39;",'"':"&quot;"}[c])); }
function escaparAtributo(valor) { return escaparHtml(valor); }
