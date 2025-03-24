package ro.upt.ac.conventii.rector;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "rector")
public class Rector {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String nume;
    private String prenume;
    private String email;
    private String titluAcademic;
    private String telefon;
    
    @Lob
    @Column(name = "semnatura", columnDefinition="LONGBLOB")
    private byte[] semnatura;
    
    // Constructor implicit
    public Rector() {
    }

    // Constructor cu parametri
    public Rector(String nume, String prenume, String email, String titluAcademic, String telefon) {
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.titluAcademic = titluAcademic;
        this.telefon = telefon;
    }

    // Getteri și setteri
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitluAcademic() {
        return titluAcademic;
    }

    public void setTitluAcademic(String titluAcademic) {
        this.titluAcademic = titluAcademic;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }
    
    public byte[] getSemnatura() {
        return semnatura;
    }

    public void setSemnatura(byte[] semnatura) {
        this.semnatura = semnatura;
    }

    // Metodă pentru obținerea numelui complet
    public String getNumeComplet() {
        return titluAcademic + " " + prenume + " " + nume;
    }

    @Override
    public String toString() {
        return "Rector{" +
                "id=" + id +
                ", nume='" + nume + '\'' +
                ", prenume='" + prenume + '\'' +
                ", email='" + email + '\'' +
                ", titluAcademic='" + titluAcademic + '\'' +
                ", telefon='" + telefon + '\'' +
                '}';
    }
}