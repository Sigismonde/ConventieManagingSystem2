// ConventieStatus.java
package ro.upt.ac.conventii.conventie;

public enum ConventieStatus {
    NETRIMIS,             // Convention not sent yet (draft)
    IN_ASTEPTARE,         // Waiting for approval
    APROBATA_PARTENER,    // Approved by the partner but not by prodecan
    TRIMISA_TUTORE,       // Sent to tutor after partner approval
    APROBATA_TUTORE,      // Approved by the tutor
    APROBATA,             // Fully approved (by partner, tutor and prodecan)
    RESPINSA              // Rejected
}