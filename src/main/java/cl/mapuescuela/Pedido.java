package cl.mapuescuela;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String codigoPedido;

    private String cliente;
    private String email;
    private String telefono;

    private int productoId;
    private String producto;
    private int cantidad;
    private int total;

    private String modalidadEntrega;

    @Column(length = 1000)
    private String direccionEntrega;

    private String comunaEntrega;
    private String estado;
    private boolean inventarioDescontado;
    private boolean comprobanteAdjunto;

    private String empresaTransporte;
    private String numeroSeguimiento;
    private String fechaEnvio;

    @Transient
    private List<PedidoItem> items = new ArrayList<PedidoItem>();

    private boolean inventarioActualizado;

    public Pedido() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoPedido() {
        return codigoPedido;
    }

    public void setCodigoPedido(String codigoPedido) {
        this.codigoPedido = codigoPedido;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getModalidadEntrega() {
        return modalidadEntrega;
    }

    public void setModalidadEntrega(String modalidadEntrega) {
        this.modalidadEntrega = modalidadEntrega;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getComunaEntrega() {
        return comunaEntrega;
    }

    public void setComunaEntrega(String comunaEntrega) {
        this.comunaEntrega = comunaEntrega;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
public boolean isInventarioActualizado() {
    return inventarioActualizado;
}

public void setInventarioActualizado(boolean inventarioActualizado) {
    this.inventarioActualizado = inventarioActualizado;
}

public boolean isInventarioDescontado() {
    return inventarioDescontado;
}

public void setInventarioDescontado(boolean inventarioDescontado) {
    this.inventarioDescontado = inventarioDescontado;
}

public boolean isComprobanteAdjunto() {
    return comprobanteAdjunto;
}

public void setComprobanteAdjunto(boolean comprobanteAdjunto) {
    this.comprobanteAdjunto = comprobanteAdjunto;
}

public String getEmpresaTransporte() {
    return empresaTransporte;
}

public void setEmpresaTransporte(String empresaTransporte) {
    this.empresaTransporte = empresaTransporte;
}

public String getNumeroSeguimiento() {
    return numeroSeguimiento;
}

public void setNumeroSeguimiento(String numeroSeguimiento) {
    this.numeroSeguimiento = numeroSeguimiento;
}

public String getFechaEnvio() {
    return fechaEnvio;
}

public void setFechaEnvio(String fechaEnvio) {
    this.fechaEnvio = fechaEnvio;
}

public List<PedidoItem> getItems() {
    return items;
}

public void setItems(List<PedidoItem> items) {
    this.items = items;
}
}