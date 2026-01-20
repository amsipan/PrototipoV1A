package secsys.models;

import java.util.UUID;

public class Cliente {
    private UUID clienteId;
    private String ruc;
    private String razonSocial;
    private String direccion;
    private String representanteLegal;
    private String telefono;
    private String correo;
    private String sector;
    private String tamano;
    private String estado;

    // getters/setters...
    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID id) { this.clienteId = id; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String v) { this.razonSocial = v; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { this.direccion = v; }
    public String getRepresentanteLegal() { return representanteLegal; }
    public void setRepresentanteLegal(String v) { this.representanteLegal = v; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }
    public String getCorreo() { return correo; }
    public void setCorreo(String v) { this.correo = v; }
    public String getSector() { return sector; }
    public void setSector(String v) { this.sector = v; }
    public String getTamano() { return tamano; }
    public void setTamano(String v) { this.tamano = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
}
