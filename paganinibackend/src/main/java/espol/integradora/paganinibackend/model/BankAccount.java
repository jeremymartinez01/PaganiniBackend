package espol.integradora.paganinibackend.model;

import jakarta.persistence.*;
import espol.integradora.paganinibackend.util.CryptoConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import espol.integradora.paganinibackend.model.constantes.*;

@Entity
@Table(name = "bank_account")
public class BankAccount {

    @Id
    @Column(name = "MetodoPagoId")
    private Integer metodoPagoId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "MetodoPagoId")
    @JsonIgnore
    private MetodoPago metodoPago;

    @Column(name = "NombreBanco", length = 20, nullable = false)
    private String nombreBanco;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "NumeroCuenta", columnDefinition = "VARBINARY(256)", nullable = false)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "TipoCuenta", nullable = false)
    private TipoCuenta tipoCuenta;

    @Column(name = "Titular", length = 50, nullable = false)
    private String titular;

    @Column(name = "Identificacion", length = 13, nullable = false)
    private String identificacion;

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

    public String getNombreBanco() {
        return nombreBanco;
    }

    public void setNombreBanco(String nombreBanco) {
        this.nombreBanco = nombreBanco;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }
    
}