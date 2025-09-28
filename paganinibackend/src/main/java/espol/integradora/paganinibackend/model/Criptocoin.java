package espol.integradora.paganinibackend.model;
import jakarta.persistence.*;


@Entity
@Table(name = "Criptocoin")
public class Criptocoin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String abreviacion;
    private String nombre;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getAbreviacion() {
        return abreviacion;
    }
    public void setAbreviacion(String abreviacion) {
        this.abreviacion = abreviacion;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
}