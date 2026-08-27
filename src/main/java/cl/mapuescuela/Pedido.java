package cl.mapuescuela;

public class Pedido {

    private int id;
    private String cliente;
    private String producto;
    private String modalidadEntrega;
    private String estado;

    public Pedido() {
    }

    public Pedido(int id, String cliente, String producto,
                  String modalidadEntrega, String estado) {
        this.id = id;
        this.cliente = cliente;
        this.producto = producto;
        this.modalidadEntrega = modalidadEntrega;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
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