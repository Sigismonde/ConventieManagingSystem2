package ro.upt.ac.conventii.conventie;

public enum ConventieStatus {
    NETRIMIS,             // Convention not sent yet
    IN_ASTEPTARE,         // Convention awaiting initial approval
    APROBATA_PARTENER,    // Approved by partner
    TRIMISA_TUTORE,       // Sent to tutor
    APROBATA_TUTORE,      // Approved by tutor
    IN_ASTEPTARE_PRODECAN, // Awaiting prodecan approval
    IN_ASTEPTARE_PRORECTOR, // Awaiting prorector approval (new status)
    APROBATA,             // Fully approved
    RESPINSA              // Rejected
}