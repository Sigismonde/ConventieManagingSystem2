package ro.upt.ac.conventii.companie;

import jakarta.persistence.Column;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Companie 
{
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	private String nume;
	private String reprezentant;
	private String calitate;
	private String adresa;
	private String telefon;
	 @Column(unique = true)  // Facem CUI-ul unic în baza de date
	    private String cui;     // CUI-ul va fi stocat ca string pentru a păstra și formatul RO dacă există
	
	// Adăugăm email-ul cu validare
	 @Column(unique = true)
	    private String email;
	    
	    @Column(unique = true, name = "nr_reg_com")
	    private String nrRegCom;
	public Companie()
	{
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

	    // Adăugăm getter și setter pentru noul câmp
	    public String getCui() {
	        return cui;
	    }

	    public void setCui(String cui) {
	        this.cui = cui;
	    }

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getNume()
	{
		return nume;
	}

	public void setNume(String nume)
	{
		this.nume = nume;
	}

	public String getReprezentant()
	{
		return reprezentant;
	}

	public void setReprezentant(String reprezentant)
	{
		this.reprezentant = reprezentant;
	}

	public String getCalitate()
	{
		return calitate;
	}

	public void setCalitate(String calitate)
	{
		this.calitate = calitate;
	}

	public String getAdresa()
	{
		return adresa;
	}

	public void setAdresa(String adresa)
	{
		this.adresa = adresa;
	}

	public String getTelefon()
	{
		return telefon;
	}

	public void setTelefon(String telefon)
	{
		this.telefon = telefon;
	}
	
	public String toString()
	{
		return nume;
	}
}
