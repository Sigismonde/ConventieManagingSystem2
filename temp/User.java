package ro.upt.ac.conventii.users;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User 
{
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	private String firstName;
	
    private String lastName;
	
    private String password;
    
    private String password2;

    public User()
    {
    }
    
    public User(String firstName, String lastName, String password)
    {
    	this.firstName=firstName;
    	this.lastName=lastName;
    	this.password=password;
    }
        
    public int getId() 
    {
		return id;
	}

	public void setId(int id) 
	{
		this.id = id;
	}
	
	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getPassword() 
	{
		return password;
	}

	public void setPassword(String password) 
	{
		this.password = password;
	}

	public String getPassword2() 
	{
		return password2;
	}

	public void setPassword2(String password2) 
	{
		this.password2 = password2;
	}

	@Override
    public String toString() 
    {
    	return firstName+" "+lastName;
    }
}
