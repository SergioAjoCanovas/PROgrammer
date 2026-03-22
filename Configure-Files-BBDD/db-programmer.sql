-- Tabla de roles
CREATE TABLE roles (
	id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO roles (nombre) VALUES ('ADMIN'), ('DEVELOPER'), ('COMPANY'), ('VISITOR');



-- Tabla de usuarios
CREATE TABLE usuarios(
	id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_rol INT,
    FOREIGN KEY (id_rol) REFERENCES roles(id)
);



-- Tabla de categorías
CREATE TABLE categorias_tecnologias (
	id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO categorias_tecnologias (nombre) VALUES 
('Frontend & Web'), ('Backend & Core'), ('Bases de Datos'), 
('DevOps & Cloud'), ('Mobile'), ('Data & IA');



-- Tabla de tecnologías
CREATE TABLE tecnologias(
	id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    categoria_id INT,
    FOREIGN KEY (categoria_id) REFERENCES categorias_tecnologias(id)
);

INSERT INTO tecnologias (nombre, categoria_id) VALUES
-- Categoría 1: Frontend & Web
('HTML5', 1), ('CSS3', 1), ('JavaScript', 1), ('TypeScript', 1), ('React', 1), ('Angular', 1), ('Vue.js', 1), ('Tailwind CSS', 1), ('Bootstrap', 1);

-- Categoría 2: Backend & Core
INSERT INTO tecnologias (nombre, categoria_id) VALUES 
('Java', 2), ('Spring Boot', 2), ('Python', 2), ('Node.js', 2), ('C#', 2), ('.NET', 2), ('PHP', 2), ('Laravel', 2), ('Go', 2), ('C++', 2);

-- Categoría 3: Bases de Datos
INSERT INTO tecnologias (nombre, categoria_id) VALUES 
('MySQL', 3), ('PostgreSQL', 3), ('MongoDB', 3), ('Redis', 3), ('Oracle DB', 3), ('Firebase', 3);

-- Categoría 4: DevOps & Cloud
INSERT INTO tecnologias (nombre, categoria_id) VALUES 
('Docker', 4), ('Kubernetes', 4), ('Git', 4), ('GitHub', 4), ('AWS', 4), ('Google Cloud', 4), ('Linux', 4), ('CI/CD', 4);

-- Categoría 5: Mobile
INSERT INTO tecnologias (nombre, categoria_id) VALUES 
('React Native', 5), ('Flutter', 5), ('Android SDK', 5), ('iOS SDK', 5);

-- Categoría 6: Data & IA
INSERT INTO tecnologias (nombre, categoria_id) VALUES 
('TensorFlow', 6), ('PyTorch', 6), ('Pandas', 6), ('Jupyter', 6);



-- Tabla de desarrolladores
CREATE TABLE perfiles_desarrollador (
	id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT UNIQUE,
    biografia TEXT,
    github_url VARCHAR(255),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);



-- Tabla que une al desarrollador con las tecnologías marcadas
CREATE TABLE perfil_tecnologia(
	perfil_id INT,
    tecnologia_id INT,
    PRIMARY KEY (perfil_id, tecnologia_id),
    FOREIGN KEY (perfil_id) REFERENCES perfiles_desarrollador(id),
    FOREIGN KEY (tecnologia_id) REFERENCES tecnologias(id)
);



-- Tabla de empresas
CREATE TABLE perfiles_empresa(
	id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT UNIQUE,
    nombre_empresa VARCHAR(100) UNIQUE,
    sector VARCHAR(100),
    sitio_web VARCHAR(255),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) 
);



-- Tabla de categorias de los proyectos
CREATE TABLE categorias_proyectos(
	id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

INSERT INTO categorias_proyectos (nombre) VALUES ('Web'), ('Videojuegos'), ('IA'), ('Mobile'), ('Desktop');



-- Tabla de proyectos
CREATE TABLE proyectos(
	id INT AUTO_INCREMENT PRIMARY KEY,
    autor_id INT,
    categoria_id INT,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT,
    repo_url VARCHAR(255),
    esta_validado BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (autor_id) REFERENCES usuarios(id),
    FOREIGN KEY (categoria_id) REFERENCES categorias_proyectos(id)
);



-- Tabla que une el proyecto con las tecnologías que se han usado
CREATE TABLE proyecto_tecnologias(
	proyecto_id INT,
    tecnologia_id INT,
    PRIMARY KEY (proyecto_id, tecnologia_id),
    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    FOREIGN KEY (tecnologia_id) REFERENCES tecnologias(id)
);



-- Tabla de evaluaciones
CREATE TABLE evaluaciones(
	id INT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id INT,
    admin_id INT,
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
	id INT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id INT,
    usuario_id INT, -- El usuario que deja el comentario
	comentario TEXT NOT NULL,
    fecha_resena TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);



-- Tabla de ofertas
CREATE TABLE ofertas_empleo(
	id INT AUTO_INCREMENT PRIMARY KEY,
    empresa_id INT,
    titulo VARCHAR(100),
    descripcion TEXT,
    activa BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (empresa_id) REFERENCES usuarios(id)
);



-- Tabla oferta tecnología
CREATE TABLE oferta_tecnologia (
    oferta_id INT,
    tecnologia_id INT,
    PRIMARY KEY (oferta_id, tecnologia_id),
    FOREIGN KEY (oferta_id) REFERENCES ofertas_empleo(id),
    FOREIGN KEY (tecnologia_id) REFERENCES tecnologias(id)
);



-- Tabla postulaciones
CREATE TABLE postulaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    oferta_id INT,
    desarrollador_id INT,
    proyecto_vinculado_id INT, 
    mensaje_adjunto VARCHAR(500),
    fecha_postulacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (oferta_id) REFERENCES ofertas_empleo(id),
    FOREIGN KEY (desarrollador_id) REFERENCES usuarios(id),
    FOREIGN KEY (proyecto_vinculado_id) REFERENCES proyectos(id)
);


 -- Tabla de mensajes
CREATE TABLE mensajes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    emisor_id INT,
    receptor_id INT,
    contenido TEXT,
    leido BOOLEAN DEFAULT FALSE,
    fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emisor_id) REFERENCES usuarios(id),
    FOREIGN KEY (receptor_id) REFERENCES usuarios(id)
);



-- Tabla de favoritos
CREATE TABLE favoritos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    perfil_guardado_id INT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (perfil_guardado_id) REFERENCES usuarios(id)
);