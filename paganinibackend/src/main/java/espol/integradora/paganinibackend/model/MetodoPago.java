package espol.integradora.paganinibackend.model;

import jakarta.persistence.*;
import java.util.*;
import espol.integradora.paganinibackend.model.constantes.*;
import espol.integradora.paganinibackend.model.*;
@Entity
@Table(name = "metodo_pago")
public class MetodoPago {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "UserId", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipo", nullable = false)
    private TipoPago tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false)
    private EstadoPago estado = EstadoPago.inactivo;

    @OneToOne(mappedBy = "metodoPago", cascade = CascadeType.ALL)
    private BankAccount bankAccount;

    @OneToOne(mappedBy = "metodoPago", cascade = CascadeType.ALL)
    private Card card;

    @OneToOne(mappedBy = "metodoPago", cascade = CascadeType.ALL)
    private Ewallet ewallet;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public TipoPago getTipo() {
        return tipo;
    }

    public void setTipo(TipoPago tipo) {
        this.tipo = tipo;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Ewallet getEwallet() {
        return ewallet;
    }

    public void setEwallet(Ewallet ewallet) {
        this.ewallet = ewallet;
    }

}
