package ro.upt.ac.conventii;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ro.upt.ac.conventii.prodecan.Prodecan;
import ro.upt.ac.conventii.prodecan.ProdecanRepository;
import ro.upt.ac.conventii.security.User;
import ro.upt.ac.conventii.security.UserRepository;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner loadTestData(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, ProdecanRepository prodecanRepository) {
        return args -> {
System.out.println("Încercare creare utilizator prodecan de test...");
            
            if (userRepository.findByEmail("prodecan@test.com") == null) {
            	
              
                User userProdecan = new User();
                userProdecan.setEmail("prodecan@test.com");
                userProdecan.setNume("Popescu");
                userProdecan.setPrenume("Ion");
                userProdecan.setPassword(passwordEncoder.encode("password")); // parola va fi "password"
                userProdecan.setRole("ROLE_PRODECAN");
                userProdecan.setEnabled(true);
                userProdecan.setFacultate("Automatica si Calculatoare");
                userProdecan.setDepartament("Calculatoare");
                
                userRepository.save(userProdecan);
                
               
                Prodecan prodecan = new Prodecan();
                prodecan.setEmail("prodecan@test.com");
                prodecan.setNume("Popescu");
                prodecan.setPrenume("Ion");
                prodecan.setFacultate("Automatica si Calculatoare");
                prodecan.setDepartament("Calculatoare");
                prodecan.setTelefon("0712345678");
                
                prodecanRepository.save(prodecan);
                
                System.out.println("Prodecan creat cu succes!");
                System.out.println("Email: prodecan@test.com");
                System.out.println("Parola: password");
            } else {
                System.out.println("Prodecanul există deja în baza de date!");
            }

            
            if (userRepository.findByEmail("student@test.com") == null) {
                User userStudent = new User();
                userStudent.setEmail("student@test.com");
                userStudent.setNume("Student");
                userStudent.setPrenume("Test");
                userStudent.setPassword(passwordEncoder.encode("password"));
                userStudent.setRole("ROLE_STUDENT");
                userStudent.setEnabled(true);
                userStudent.setFacultate("Automatica si Calculatoare");
                userStudent.setSpecializare("Calculatoare si Tehnologia Informatiei");
                
                userRepository.save(userStudent);
                
                System.out.println("Student creat cu succes!");
                System.out.println("Email: student@test.com");
                System.out.println("Parola: password");
            }
        };
    }
}