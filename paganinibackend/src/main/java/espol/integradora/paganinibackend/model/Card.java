package espol.integradora.paganinibackend.model;
import espol.integradora.paganinibackend.util.CryptoConverter;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import espol.integradora.paganinibackend.model.constantes.*;

@Entity
@Table(name = "card")
public class Card {

    @Id
    @Column(name = "MetodoPagoId")
    private Integer metodoPagoId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "MetodoPagoId")
    @JsonIgnore
    private MetodoPago metodoPago;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "NumeroTarjeta", columnDefinition = "VARBINARY(256)", nullable = false)
    private String numeroTarjeta;

    @Column(name = "Titular", length = 50, nullable = false)
    private String titular;

    @Column(name = "Mes", nullable = false)
    private Integer mes;

    @Column(name = "Year", nullable = false)
    private Integer year;

    @Column(name = "Cvv", length = 5, nullable = false)
    private String cvv;

    @Column(name = "Tipo", nullable = false)
    private char tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "Red", nullable = false)
    private RedTarjeta red;


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

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public char getTipo() {
        return tipo;
    }

    public void setTipo(char tipo) {
        this.tipo = tipo;
    }

    public RedTarjeta getRed() {
        return red;
    }

    public void setRed(RedTarjeta red) {
        this.red = red;
    }
    
}