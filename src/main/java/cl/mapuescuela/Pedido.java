package cl.mapuescuela;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String codigoPedido;

    private String cliente;

    private int productoId;

    private String producto;

    private int cantidad;

    private String modalidadEntrega;

    private String estado;

    public Pedido() {
    }

    /*
     * Se mantiene este constructor para no romper
     * código anterior que pudiera utilizarlo.
     */
    public Pedido(
            String cliente,
            String producto,
            String modalidadEntrega,
            String estado) {

        this.cliente = cliente;
        this.producto = producto;
        this.modalidadEntrega = modalidadEntrega;
        this.estado = estado;
    }

    public Pedido(
            String cliente,
            int productoId,
            String producto,
            int cantidad,
            String modalidadEntrega,
            String estado) {

        this.cliente = cliente;
        this.productoId = productoId;
        this.producto = producto;
        this.cantidad = cantidad;
        this.modalidadEntrega = modalidadEntrega;
        this.estado = estado;
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

    public String getModalidadEntrega() {
        return modalidadEntrega;
    }

    public void setModalidadEntrega(String modalidadEntrega) {
        this.modalidadEntrega = modalidadEntrega;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}