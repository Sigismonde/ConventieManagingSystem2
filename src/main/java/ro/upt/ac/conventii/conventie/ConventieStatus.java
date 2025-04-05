package ro.upt.ac.conventii.conventie;

public enum ConventieStatus {
    NETRIMIS,          // Convention not sent yet (draft)
    IN_ASTEPTARE,      // Waiting for approval
    APROBATA_PARTENER, // Approved by the partner but not by prodecan
    APROBATA,          // Fully approved
    RESPINSA           // Rejected
}