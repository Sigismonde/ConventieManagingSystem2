package ro.upt.ac.conventii.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    // Pattern pentru validarea emailului
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    // Pattern pentru validarea numărului de înregistrare
    private static final Pattern NR_REG_COM_PATTERN = 
        Pattern.compile("J[A-Z]{2}/\\d{1,4}/\\d{4}");
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidNrRegCom(String nrRegCom) {
        if (nrRegCom == null || nrRegCom.trim().isEmpty()) {
            return false;
        }
        return NR_REG_COM_PATTERN.matcher(nrRegCom).matches();
    }
    
    public static boolean isValidCui(String cui) {
        if (cui == null || cui.trim().isEmpty()) {
            return false;
        }
        // Acceptă CUI-uri cu sau fără prefixul RO
        return cui.matches("(RO)?\\d{1,10}");
    }
}