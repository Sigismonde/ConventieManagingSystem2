package ro.upt.ac.conventii.companie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Companie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private String nume;
    
    // Înlocuim reprezentant cu nume și prenume separate
    private String numeReprezentant;
    private String prenumeReprezentant;
    
    // Înlocuim calitate cu functie
    private String functie;
    
    private String adresa;
    private String telefon;
    
    @Column(unique = true)
    private String cui;
    
    @Column(unique = true)
    private String email;
    
    @Column(unique = true, name = "nr_reg_com")
    private String nrRegCom;
    
    // Constructori
    public Companie() {
    }
    
    // Getteri și setteri pentru noile câmpuri
    public String getNumeReprezentant() {
        return numeReprezentant;
    }
    
    public void setNumeReprezentant(String numeReprezentant) {
        this.numeReprezentant = numeReprezentant;
    }
    
    public String getPrenumeReprezentant() {
        return prenumeReprezentant;
    }
    
    public void setPrenumeReprezentant(String prenumeReprezentant) {
        this.prenumeReprezentant = prenumeReprezentant;
    }
    
    public String getFunctie() {
        return functie;
    }
    
    public void setFunctie(String functie) {
        this.functie = functie;
    }
    
    // Metodă helper pentru a obține numele complet al reprezentantului
    public String getReprezentant() {
        if (numeReprezentant != null && prenumeReprezentant != null) {
            return prenumeReprezentant + " " + numeReprezentant;
        }
        return numeReprezentant != null ? numeReprezentant : "";
    }
    
    // Metodă pentru compatibilitate cu codul existent
    public String getCalitate() {
        return functie;
    }
    
    // Getteri și setteri existenți
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

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getCui() {
        return cui;
    }

    public void setCui(String cui) {
        this.cui = cui;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNrRegCom() {
        return nrRegCom;
    }

    public void setNrRegCom(String nrRegCom) {
        this.nrRegCom = nrRegCom;
    }
    
    @Override
    public String toString() {
        return nume;
    }
}