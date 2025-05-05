package ro.upt.ac.conventii.partner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import ro.upt.ac.conventii.companie.Companie;

@Entity
@Table(name = "partners")
public class Partner {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Lob
    @Column(name = "semnatura", columnDefinition="LONGBLOB")
    private byte[] semnatura;

    public byte[] getSemnatura() {
        return semnatura;
    }

    public void setSemnatura(byte[] semnatura) {
        this.semnatura = semnatura;
    }
    
    @ManyToOne
    @JoinColumn(name = "companie_id", nullable = false)
    private Companie companie;
    
    private String nume;
    private String prenume;
    private String functie;
    
    @Column(unique = true)
    private String email;
    
    private String telefon;
    
    // Constructors
    public Partner() {}
    
    public Partner(Companie companie, String nume, String prenume, String functie, String email, String telefon) {
        this.companie = companie;
        this.nume = nume;
        this.prenume = prenume;
        this.functie = functie;
        this.email = email;
        this.telefon = telefon;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Companie getCompanie() {
        return companie;
    }
    
    public void setCompanie(Companie companie) {
        this.companie = companie;
    }
    
    public String getNume() {
        return nume;
    }
    
    public void setNume(String nume) {
        this.nume = nume;
    }
    
    public String getPrenume() {
        return prenume;
    }
    
    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }
    
    public String getFunctie() {
        return functie;
    }
    
    public void setFunctie(String functie) {
        this.functie = functie;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefon() {
        return telefon;
    }
    
    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }
    
    public String getNumeComplet() {
        return prenume + " " + nume;
    }
    
    @PrePersist
    protected void onCreate() {
        System.out.println("DEBUG: Pre-persist Partner - " + this.toString());
    }
    
    @PostPersist
    protected void onPersist() {
        System.out.println("DEBUG: Post-persist Partner - ID=" + this.id);
    }
    
    @Override
    public String toString() {
        return "Partner{" +
                "id=" + id +
                ", nume='" + nume + '\'' +
                ", email='" + email + '\'' +
                ", companie=" + (companie != null ? companie.getNume() : "null") +
                '}';
    }
}