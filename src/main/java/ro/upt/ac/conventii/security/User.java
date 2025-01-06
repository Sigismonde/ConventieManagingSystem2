package ro.upt.ac.conventii.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String nume;
    private String prenume;
    private String password;
    private String role;
    private boolean enabled;
    private String departament;
    private String facultate;
    private String specializare;
    private String titluAcademic;
    
    @Column(name = "first_login")
    private Boolean firstLogin;
    
    public User() {
        this.enabled = true;
    }

    public User(String email, String nume, String prenume, String password, String role) {
        this.email = email;
        this.nume = nume;
        this.prenume = prenume;
        this.password = password;
        this.role = role;
        this.enabled = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        return Collections.singletonList(authority);
    }


    
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    


    

    public Boolean isFirstLogin() {
        // Returnăm true doar pentru studenți și doar dacă firstLogin este null sau true
        if ("ROLE_STUDENT".equals(this.getRole())) {
            return firstLogin == null || firstLogin;
        }
        return false;  // Pentru non-studenți returnăm mereu false
    }

    public void setFirstLogin(Boolean firstLogin) {
        this.firstLogin = firstLogin;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
    


    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDepartament() {
        return departament;
    }

    public void setDepartament(String departament) {
        this.departament = departament;
    }

    public String getFacultate() {
        return facultate;
    }

    public void setFacultate(String facultate) {
        this.facultate = facultate;
    }

    public String getSpecializare() {
        return specializare;
    }

    public void setSpecializare(String specializare) {
        this.specializare = specializare;
    }

    public String getTitluAcademic() {
        return titluAcademic;
    }

    public void setTitluAcademic(String titluAcademic) {
        this.titluAcademic = titluAcademic;
    }

    public String getNumeComplet() {
        if (prenume == null || nume == null) {
            return "Utilizator";
        }
        return prenume + " " + nume;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nume='" + nume + '\'' +
                ", prenume='" + prenume + '\'' +
                ", role='" + role + '\'' +
                ", enabled=" + enabled +
                ", departament='" + departament + '\'' +
                ", facultate='" + facultate + '\'' +
                ", specializare='" + specializare + '\'' +
                ", titluAcademic='" + titluAcademic + '\'' +
                '}';
    }
}