
ALTER DATABASE conventii CHARACTER SET utf8mb4 COLLATE utf8mb4_romanian_ci;


CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE COLLATE utf8mb4_romanian_ci,
    nume VARCHAR(100) NOT NULL COLLATE utf8mb4_romanian_ci,
    prenume VARCHAR(100) NOT NULL COLLATE utf8mb4_romanian_ci,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    departament VARCHAR(100) COLLATE utf8mb4_romanian_ci,
    facultate VARCHAR(100) COLLATE utf8mb4_romanian_ci,
    specializare VARCHAR(100) COLLATE utf8mb4_romanian_ci,
    titlu_academic VARCHAR(100) COLLATE utf8mb4_romanian_ci,
    CONSTRAINT UK_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_romanian_ci;

CREATE TABLE IF NOT EXISTS prodecan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nume VARCHAR(100) NOT NULL,
    prenume VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    facultate VARCHAR(100) NOT NULL,
    departament VARCHAR(100),
    telefon VARCHAR(20)
);


INSERT INTO prodecan (nume, prenume, email, facultate, departament, telefon)
VALUES ('Popescu', 'Ion', 'prodecan@test.com', 'Automatica si Calculatoare', 'Calculatoare', '0712345678');