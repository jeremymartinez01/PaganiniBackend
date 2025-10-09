package espol.integradora.paganinibackend.model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import espol.integradora.paganinibackend.util.CryptoConverter;
import jakarta.persistence.*;
@Entity
@Table(name = "ewallet")
public class Ewallet {

    @Id
    @Column(name = "MetodoPagoId")
    private Integer metodoPagoId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "MetodoPagoId")
    @JsonIgnore
    private MetodoPago metodoPago;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Direccion", columnDefinition = "VARCHAR(255)", nullable = false)
    private String direccion;

    @ManyToOne
    @JoinColumn(name = "criptocoinId", nullable = false)
    private Criptocoin criptocoin;

    public Integer getMetodoPagoId() {
        return metodoPagoId;
    }

    public void setMetodoPagoId(Integer metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Criptocoin getCriptocoin() {
        return criptocoin;
    }

    public void setCriptocoin(Criptocoin criptocoin) {
        this.criptocoin = criptocoin;
    }

}
