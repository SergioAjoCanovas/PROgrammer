-- Tabla de roles
CREATE TABLE roles (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO roles (nombre) VALUES ('ADMIN'), ('DEVELOPER'), ('COMPANY'), ('VISITOR');

-- Tabla de usuarios
CREATE TABLE usuarios(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    foto_perfil VARCHAR(255),
    github VARCHAR(255),
    linkedin VARCHAR(255),
    curriculum VARCHAR (255),
    biografia TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_rol BIGINT,
    FOREIGN KEY (id_rol) REFERENCES roles(id)
);

-- Tabla de categorías de tecnologías
CREATE TABLE categorias_tecnologias (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO categorias_tecnologias (nombre) VALUES 
('Frontend & Web'), ('Backend & Core'), ('Bases de Datos'), 
('DevOps & Cloud'), ('Mobile'), ('Data & IA');

-- Tabla de tecnologías
CREATE TABLE tecnologias(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    icono VARCHAR(50),
    categoria_id BIGINT,
    FOREIGN KEY (categoria_id) REFERENCES categorias_tecnologias(id)
);

INSERT INTO tecnologias (nombre, icono, categoria_id) VALUES
('HTML5', 'fab fa-html5', 1),
('CSS3', 'fab fa-css3-alt', 1),
('JavaScript', 'fab fa-js', 1),
('TypeScript', 'fas fa-code', 1),
('React', 'fab fa-react', 1),
('Angular', 'fab fa-angular', 1),
('Vue.js', 'fab fa-vuejs', 1),

('Java', 'fab fa-java', 2),
('Spring Boot', 'fas fa-leaf', 2),
('Python', 'fab fa-python', 2),
('Node.js', 'fab fa-node-js', 2),
('C#', 'fas fa-hashtag', 2),
('PHP', 'fab fa-php', 2),
('Go', 'fas fa-terminal', 2),

('MySQL', 'fas fa-database', 3),
('PostgreSQL', 'fas fa-database', 3),
('MongoDB', 'fas fa-leaf', 3),

('Docker', 'fab fa-docker', 4),

('React Native', 'fab fa-react', 5),
('Flutter', 'fas fa-rocket', 5);

-- Tabla de desarrolladores
CREATE TABLE perfiles_desarrollador (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT UNIQUE,
    biografia TEXT,
    github_url VARCHAR(255),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabla intermedia perfil-tecnología
CREATE TABLE perfil_tecnologia(
	perfil_id BIGINT,
    tecnologia_id BIGINT,
    PRIMARY KEY (perfil_id, tecnologia_id),
    FOREIGN KEY (perfil_id) REFERENCES perfiles_desarrollador(id),
    FOREIGN KEY (tecnologia_id) REFERENCES tecnologias(id)
);

-- Tabla de empresas
CREATE TABLE perfiles_empresa(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT UNIQUE,
    nombre_empresa VARCHAR(100) UNIQUE,
    sector VARCHAR(100),
    sitio_web VARCHAR(255),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) 
);

-- Tabla de categorías de proyectos
CREATE TABLE categorias_proyectos(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);
INSERT INTO categorias_proyectos (nombre) VALUES ('Web'), ('Videojuegos'), ('IA'), ('Mobile'), ('Desktop');

-- Tabla de proyectos
CREATE TABLE proyectos(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    autor_id BIGINT,
    categoria_id BIGINT,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT,
    repo_url VARCHAR(255),
    foto_1 VARCHAR(255),
    foto_2 VARCHAR(255),
    foto_3 VARCHAR(255),
    foto_4 VARCHAR(255),
    esta_validado BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (autor_id) REFERENCES usuarios(id),
    FOREIGN KEY (categoria_id) REFERENCES categorias_proyectos(id)
);

-- Tabla intermedia proyecto-tecnologías
CREATE TABLE proyecto_tecnologias(
	proyecto_id BIGINT,
    tecnologia_id BIGINT,
    PRIMARY KEY (proyecto_id, tecnologia_id),
    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    FOREIGN KEY (tecnologia_id) REFERENCES tecnologias(id)
);

-- Tabla de evaluaciones
CREATE TABLE evaluaciones(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id BIGINT,
    admin_id BIGINT,
    nota_arquitectura INT CHECK (nota_arquitectura BETWEEN 1 AND 10),
    nota_limpieza INT CHECK (nota_limpieza BETWEEN 1 AND 10),
    nota_documentacion INT CHECK (nota_documentacion BETWEEN 1 AND 10),
    comentario_tecnico TEXT,
    fecha_evaluacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    FOREIGN KEY (admin_id) REFERENCES usuarios(id)
);

-- Tabla de reseñas
CREATE TABLE resenas_proyectos(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id BIGINT,
    usuario_id BIGINT,
	comentario TEXT NOT NULL,
    fecha_resena TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabla de ofertas (ACTUALIZADA CON LAS NUEVAS COLUMNAS)
CREATE TABLE ofertas_empleo(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT,
    titulo VARCHAR(100),
    descripcion TEXT,
    requisitos TEXT,          -- <-- NUEVA COLUMNA
    ofrecemos TEXT,            -- <-- NUEVA COLUMNA
    rango_salarial VARCHAR(100), -- <-- NUEVA COLUMNA
    activa BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (empresa_id) REFERENCES usuarios(id)
);

-- Tabla intermedia oferta-tecnología
CREATE TABLE oferta_tecnologia (
    oferta_id BIGINT,
    tecnologia_id BIGINT,
    PRIMARY KEY (oferta_id, tecnologia_id),
    FOREIGN KEY (oferta_id) REFERENCES ofertas_empleo(id),
    FOREIGN KEY (tecnologia_id) REFERENCES tecnologias(id)
);

-- Tabla de postulaciones
CREATE TABLE postulaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    oferta_id BIGINT,
    desarrollador_id BIGINT,
    proyecto_vinculado_id BIGINT, 
    mensaje_adjunto VARCHAR(500),
    fecha_postulacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (oferta_id) REFERENCES ofertas_empleo(id),
    FOREIGN KEY (desarrollador_id) REFERENCES usuarios(id),
    FOREIGN KEY (proyecto_vinculado_id) REFERENCES proyectos(id)
);

-- Tabla de mensajes
CREATE TABLE mensajes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    emisor_id BIGINT,
    receptor_id BIGINT,
    contenido TEXT,
    leido BOOLEAN DEFAULT FALSE,
    fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emisor_id) REFERENCES usuarios(id),
    FOREIGN KEY (receptor_id) REFERENCES usuarios(id)
);

-- Tabla de favoritos
CREATE TABLE favoritos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT,
    perfil_guardado_id BIGINT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (perfil_guardado_id) REFERENCES usuarios(id)
);

-- Inserción de administradores por defecto
INSERT INTO usuarios (username, email, password, foto_perfil, id_rol) VALUES 
('Sergio Ajo', 'sergiowork47@gmail.com', '$2a$12$dIm9H6z6vRMyDa8MzqJZXuf8J617eWGdTcExMt3BkDJYLi2foAkN6', '/Img/perfiles/SergioAjo.jpg', 1),
('David Alcázar', 'davidalcazar2015@gmail.com', '$2a$12$qJWqybbDpYHU5k4j8rO5xu/hHb2HzOqk1hM9w.DihIglbgILg2P5y', '/Img/perfiles/DavidAlcazar.jpg', 1),
('Cristian Escobar', 'crisescobardominguez@gmail.com', '$2a$12$sanMgW3WUX4uXuoEpO3yGeqXPHwlg2JEIN9Mju1wedAdSIzrhc3ou', '/Img/perfiles/CristianEscobar.jpg', 1);