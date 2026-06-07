-- 1. Tabela Użytkowników
CREATE TABLE users (
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    password_hash VARCHAR2(255) NOT NULL
);

-- 2. Tabela Albumów
CREATE TABLE albums (
    album_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    user_id NUMBER NOT NULL,
    created_at DATE DEFAULT SYSDATE,
    CONSTRAINT fk_album_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Tabela Zdjęć
CREATE TABLE photos (
    photo_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    album_id NUMBER NOT NULL,
    filename VARCHAR2(255) NOT NULL,
    full_data BLOB NOT NULL,
    thumbnail_data BLOB,
    upload_date DATE DEFAULT SYSDATE,
    resolution VARCHAR2(50),
    file_size_bytes NUMBER,
    CONSTRAINT fk_photo_album FOREIGN KEY (album_id) REFERENCES albums(album_id) ON DELETE CASCADE
);

-- 4. Tabela Słownikowa Tagów
CREATE TABLE tags (
    tag_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tag_name VARCHAR2(50) NOT NULL UNIQUE
);

-- 5. Tabela Łącząca (Zdjęcia <-> Tagi)
CREATE TABLE photo_tags (
    photo_id NUMBER NOT NULL,
    tag_id NUMBER NOT NULL,
    PRIMARY KEY (photo_id, tag_id),
    CONSTRAINT fk_pt_photo FOREIGN KEY (photo_id) REFERENCES photos(photo_id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_tag FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);

-- Utworzenie domyślnego konta admina
INSERT INTO users (username, password_hash) VALUES ('admin', 'admin');
COMMIT;
