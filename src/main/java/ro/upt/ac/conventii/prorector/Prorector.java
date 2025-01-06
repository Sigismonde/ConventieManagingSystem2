package ro.upt.ac.conventii.prorector;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prorector")
public class Prorector {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String nume;
    private String prenume;
    private String email;
    private String facultate;
    private String departament;
    private String titluAcademic;
    private String telefon;

    // Constructor implicit
    public Prorector() {
    }

    // Constructor cu parametri
    public Prorector(String nume, String prenume, String email, String facultate, 
                    String departament, String titluAcademic, String telefon) {
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.facultate = facultate;
        this.departament = departament;
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

    // Metodă pentru obținerea numelui complet
    public String getNumeComplet() {
        return titluAcademic + " " + prenume + " " + nume;
    }

    @Override
    public String toString() {
        return "Prorector{" +
                "id=" + id +
                ", nume='" + nume + '\'' +
                ", prenume='" + prenume + '\'' +
                ", email='" + email + '\'' +
                ", facultate='" + facultate + '\'' +
                ", departament='" + departament + '\'' +
                ", titluAcademic='" + titluAcademic + '\'' +
                ", telefon='" + telefon + '\'' +
                '}';
    }
}