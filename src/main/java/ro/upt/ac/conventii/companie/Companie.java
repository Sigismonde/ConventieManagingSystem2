package ro.upt.ac.conventii.companie;

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
	
	public Companie()
	{
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
