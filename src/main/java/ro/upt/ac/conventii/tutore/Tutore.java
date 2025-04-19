// Tutore.java
package ro.upt.ac.conventii.tutore;

import jakarta.persistence.*;
import ro.upt.ac.conventii.companie.Companie;

@Entity
@Table(name = "tutori")
public class Tutore {
    
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
    public Tutore() {}
    
    public Tutore(Companie companie, String nume, String prenume, String functie, String email, String telefon) {
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
    
    @Override
    public String toString() {
        return "Tutore{" +
                "id=" + id +
                ", companie=" + companie +
                ", nume='" + nume + '\'' +
                ", prenume='" + prenume + '\'' +
                ", functie='" + functie + '\'' +
                ", email='" + email + '\'' +
                ", telefon='" + telefon + '\'' +
                '}';
    }
}