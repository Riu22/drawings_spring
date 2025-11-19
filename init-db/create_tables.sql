USE drawings;

DROP TABLE IF EXISTS Users;
CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL
);






INSERT INTO `users` (`id`, `username`, `password`, `name`) VALUES
(1, 'riu', '$2a$12$GBW5Ohy655g7kur16K3ngetyft.2uM5p4ueFS0z1W1LKIKds.Ewtm', 'jaume Font'),
(3, 'prueba', '$2a$12$/EC2p1g5bTQeTNEtFcNRXuPMHqR5cB0ZwHPvLsoSAhGy9Qm.JgCq2', 'prueba'),
(4, 'prueba1', '$2a$12$cBU/u7AoyvzcRvUCUSYUjOoxtV0dNC0lEHA4Ta.0KwDTyE3cczsf.', 'prueba1');

GRANT ALL PRIVILEGES ON drawings.* TO 'admin'@'%';
FLUSH PRIVILEGES;