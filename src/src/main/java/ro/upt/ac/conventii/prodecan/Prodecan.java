package ro.upt.ac.conventii.prodecan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "prodecan")
public class Prodecan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String nume;
    private String prenume;
    private String email;
    private String facultate;
    private String departament;
    private String telefon;
   
    // Constructor implicit
    public Prodecan() {
    }

    // Constructor cu parametri
    public Prodecan(String nume, String prenume, String email, String facultate, 
                    String departament, String telefon) {
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.facultate = facultate;
        this.departament = departament;
        this.telefon = telefon;
    }
    
    @Lob
    @Column(name = "semnatura", columnDefinition="LONGBLOB")
    private byte[] semnatura;
    
    public byte[] getSemnatura() {
        return semnatura;
    }
    
    public void setSemnatura(byte[] semnatura) {
        this.semnatura = semnatura;
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

    public String getFacultate() {
        return facultate;
    }

    public void setFacultate(String facultate) {
        this.facultate = facultate;
    }

    public String getDepartament() {
        return departament;
    }

    public void setDepartament(String departament) {
        this.departament = departament;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    // Metodă pentru obținerea numelui complet
    public String getNumeComplet() {
        return prenume + " " + nume;
    }

    @Override
    public String toString() {
        return "Prodecan{" +
                "id=" + id +
                ", nume='" + nume + '\'' +
                ", prenume='" + prenume + '\'' +
                ", email='" + email + '\'' +
                ", facultate='" + facultate + '\'' +
                ", departament='" + departament + '\'' +
                ", telefon='" + telefon + '\'' +
                '}';
    }
}