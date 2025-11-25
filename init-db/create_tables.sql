USE drawings;

DROP TABLE IF EXISTS Users;
CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL
);

-- Tabla: draw (Relación 1:N con Users y parte de N:N con Users)
-- Un usuario (Users) puede tener muchos dibujos (Draw).
DROP TABLE IF EXISTS draw;
CREATE TABLE draw (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Clave foránea 1:N con Users.
    -- El usuario que creó el dibujo.
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla: permisos (Tabla de relación N:N entre Users y Draw)
-- Un usuario tiene permisos específicos (lectura/escritura) en un dibujo.
DROP TABLE IF EXISTS permissios;
CREATE TABLE permissios (
    user_id INT NOT NULL,
    draw_id INT NOT NULL,

    -- Permiso de visualización (lectura)
    can_read BOOLEAN NOT NULL DEFAULT FALSE,

    -- Permiso de modificación (escritura/edición)
    can_write BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (user_id, draw_id),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (draw_id) REFERENCES draw(id) ON DELETE CASCADE
);

-- Tabla: version (Relación 1:N con Draw)
-- Un dibujo (Draw) puede tener muchas versiones (Version).
DROP TABLE IF EXISTS version;
CREATE TABLE version (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version_number INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Clave foránea 1:N con Draw.
    -- El dibujo al que pertenece esta versión.
    draw_id INT NOT NULL,
    FOREIGN KEY (draw_id) REFERENCES draw(id) ON DELETE CASCADE,
    -- Se añade un UNIQUE index para asegurar que no hay duplicados de versiones para un mismo dibujo
    UNIQUE KEY (draw_id, version_number)
);

-- Tabla: draw_data (Relación 1:1 con Version)
-- Una versión (Version) solo tiene un conjunto de datos de dibujo (Draw_Data).
DROP TABLE IF EXISTS draw_data;
CREATE TABLE draw_data (
    version_id INT NOTTERY PRIMARY KEY,
    draw_content TEXT NOT NULL, --string de json en el cliente mandar el string de un json
    -- Clave foránea 1:1 con Version.
    -- Se usa la misma columna como PK y FK para garantizar la relación 1:1
    FOREIGN KEY (version_id) REFERENCES version(id) ON DELETE CASCADE
);






INSERT INTO `users` (`id`, `username`, `password`, `name`) VALUES
(1, 'riu', '$2a$12$GBW5Ohy655g7kur16K3ngetyft.2uM5p4ueFS0z1W1LKIKds.Ewtm', 'jaume Font'),
(3, 'prueba', '$2a$12$/EC2p1g5bTQeTNEtFcNRXuPMHqR5cB0ZwHPvLsoSAhGy9Qm.JgCq2', 'prueba'),
(4, 'prueba1', '$2a$12$cBU/u7AoyvzcRvUCUSYUjOoxtV0dNC0lEHA4Ta.0KwDTyE3cczsf.', 'prueba1');

GRANT ALL PRIVILEGES ON drawings.* TO 'admin'@'%';
FLUSH PRIVILEGES;