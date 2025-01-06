package ro.upt.ac.conventii.student;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Student 
{
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String nume;
	private String prenume;
	private String cnp;
	private Date dataNasterii;
	private String loculNasterii;
	private String cetatenie;
	private String serieCi;
	private String numarCi;
	private String adresa;
	private String anUniversitar;
	private String facultate;
	private String specializare;
	@Column(name = "an_de_studiu")
	private Integer anDeStudiu = 1; // Setăm default la 1
	   
	@Column(unique = true)
	private String email;
	private String telefon;
	

    
    // Constructor
    public Student() {
        this.anDeStudiu = 1; // Setăm și în constructor pentru siguranță
    }
    
    // Getter pentru anDeStudiu
    public int getAnDeStudiu() {
        return anDeStudiu != null ? anDeStudiu : 1; // Returnăm 1 dacă e null
    }
    
    public String getNumeComplet() {
        return nume + " " + prenume;
    }
    
    public void setAnDeStudiu(Integer anDeStudiu) {
        this.anDeStudiu = anDeStudiu != null ? anDeStudiu : 1; // Setăm 1 dacă e null
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

	public String getPrenume()
	{
		return prenume;
	}

	public void setPrenume(String prenume)
	{
		this.prenume = prenume;
	}

	public String getCnp()
	{
		return cnp;
	}

	public void setCnp(String cnp)
	{
		this.cnp = cnp;
	}

	public Date getDataNasterii()
	{
		return dataNasterii;
	}

	public void setDataNasterii(Date dataNasterii)
	{
		this.dataNasterii = dataNasterii;
	}

	public String getLoculNasterii()
	{
		return loculNasterii;
	}

	public void setLoculNasterii(String loculNasterii)
	{
		this.loculNasterii = loculNasterii;
	}

	public String getCetatenie()
	{
		return cetatenie;
	}

	public void setCetatenie(String cetatenie)
	{
		this.cetatenie = cetatenie;
	}


	public String getSerieCi()
	{
		return serieCi;
	}

	public void setSerieCi(String serieCi)
	{
		this.serieCi = serieCi;
	}

	public String getNumarCi()
	{
		return numarCi;
	}

	public void setNumarCi(String numarCi)
	{
		this.numarCi = numarCi;
	}

	public String getAdresa()
	{
		return adresa;
	}

	public void setAdresa(String adresa)
	{
		this.adresa = adresa;
	}

	public String getAnUniversitar()
	{
		return anUniversitar;
	}

	public void setAnUniversitar(String anUniversitar)
	{
		this.anUniversitar = anUniversitar;
	}

	public String getFacultate()
	{
		return facultate;
	}

	public void setFacultate(String facultate)
	{
		this.facultate = facultate;
	}

	public String getSpecializare()
	{
		return specializare;
	}

	public void setSpecializare(String specializare)
	{
		this.specializare = specializare;
	}



	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getTelefon()
	{
		return telefon;
	}

	public void setTelefon(String telefon)
	{
		this.telefon = telefon;
	}	
}
