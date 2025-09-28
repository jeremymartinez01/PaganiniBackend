package espol.integradora.paganinibackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_contact",
       uniqueConstraints = @UniqueConstraint(name="uk_owner_contact", columnNames = {"owner_id","contact_id"}))
public class UserContact {

    public enum Status { PENDING, ACCEPTED, BLOCKED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dueño de la agenda
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false,
                foreignKey = @ForeignKey(name="fk_uc_owner"))
    private User owner;

    // El usuario que es contacto
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false,
                foreignKey = @ForeignKey(name="fk_uc_contact"))
    private User contact;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACCEPTED;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // getters/setters
    public Long getId() { return id; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public User getContact() { return contact; }
    public void setContact(User contact) { this.contact = contact; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
