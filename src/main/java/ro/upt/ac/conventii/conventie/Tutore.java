package ro.upt.ac.conventii.conventie;

import jakarta.persistence.Embeddable;

@Embeddable
public class Tutore 
{
	private String nume;
	private String prenume;
	private String functie;
	private String telefon;
	private String email;
	
	public Tutore()
	{
	}

	public String getNume()
	{
		return nume;
	}

	public void setNume(String nume)
	{
		this.nume = nume;
	}

	public String getPrenume()
	{
		return prenume;
	}

	public void setPrenume(String prenume)
	{
		this.prenume = prenume;
	}

	public String getFunctie()
	{
		return functie;
	}

	public void setFunctie(String functie)
	{
		this.functie = functie;
	}

	public String getTelefon()
	{
		return telefon;
	}

	public void setTelefon(String telefon)
	{
		this.telefon = telefon;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String toString()
	{
		return nume+" "+prenume;
	}
}
