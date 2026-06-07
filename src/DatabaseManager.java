import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "album_app";
    private static final String PASS = "123";

    public DatabaseManager() {}

    public User login(String login, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password_hash"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ALBUMY
    public List<Album> getAlbumsForUser(User u) {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, u.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Album album = new Album(rs.getInt("album_id"), rs.getString("name"), u.getId());
                loadPhotosForAlbum(conn, album);
                albums.add(album);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return albums;
    }

    public void createAlbum(String name, User owner) {
        String sql = "INSERT INTO albums (name, user_id) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, owner.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ZDJĘCIA
    private void loadPhotosForAlbum(Connection conn, Album album) throws SQLException {
        String sql = "SELECT * FROM photos WHERE album_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, album.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Photo p = new Photo(
                        rs.getInt("photo_id"), rs.getString("filename"),
                        rs.getBytes("full_data"), rs.getBytes("thumbnail_data"),
                        rs.getDate("upload_date").toString(), rs.getString("resolution"),
                        rs.getLong("file_size_bytes")
                );
                loadTagsForPhoto(conn, p);
                album.getPhotos().add(p);
            }
        }
    }

    public void addPhotoToAlbum(Photo p, Album a) {
        String sql = "INSERT INTO photos (album_id, filename, full_data, thumbnail_data, resolution, file_size_bytes) VALUES (?, ?, ?, ?, ?, ?)";

        // Tablica z nazwą klucza głównego
        String[] generatedColumns = {"PHOTO_ID"};

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql, generatedColumns)) {

            pstmt.setInt(1, a.getId());
            pstmt.setString(2, p.getFilename());
            pstmt.setBytes(3, p.getFullData());
            pstmt.setBytes(4, p.getThumbnailData());
            pstmt.setString(5, p.getResolution());
            pstmt.setLong(6, p.getFileSize());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                p.setId(rs.getInt(1)); // Przypisujemy zdjęciu jego nowe ID z bazy
                a.getPhotos().add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // TAGI
    private void loadTagsForPhoto(Connection conn, Photo p) throws SQLException {
        String sql = "SELECT t.tag_name FROM tags t JOIN photo_tags pt ON t.tag_id = pt.tag_id WHERE pt.photo_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, p.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                p.getTags().add(rs.getString("tag_name"));
            }
        }
    }

    public void addTagToPhoto(Photo p, String tag) {
        String tagName = tag.trim().toLowerCase();
        if (p.getTags().contains(tagName)) return;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            int tagId = -1;
            // Sprawdzamy czy tag już istnieje w bazie
            PreparedStatement checkTag = conn.prepareStatement("SELECT tag_id FROM tags WHERE tag_name = ?");
            checkTag.setString(1, tagName);
            ResultSet rsTag = checkTag.executeQuery();
            if (rsTag.next()) {
                tagId = rsTag.getInt(1);
            } else {
                // Jeśli nie, tworzymy go
                PreparedStatement insertTag = conn.prepareStatement("INSERT INTO tags (tag_name) VALUES (?)", new String[]{"TAG_ID"});
                insertTag.setString(1, tagName);
                insertTag.executeUpdate();
                ResultSet rsNewTag = insertTag.getGeneratedKeys();
                if (rsNewTag.next()) tagId = rsNewTag.getInt(1);
            }
            // Łączymy zdjęcie z tagiem
            if (tagId != -1) {
                PreparedStatement link = conn.prepareStatement("INSERT INTO photo_tags (photo_id, tag_id) VALUES (?, ?)");
                link.setInt(1, p.getId());
                link.setInt(2, tagId);
                link.executeUpdate();
                p.getTags().add(tagName);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void removeTagFromPhoto(Photo p, String tag) {
        String tagName = tag.toLowerCase();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            PreparedStatement getTag = conn.prepareStatement("SELECT tag_id FROM tags WHERE tag_name = ?");
            getTag.setString(1, tagName);
            ResultSet rsTag = getTag.executeQuery();
            if (rsTag.next()) {
                int tagId = rsTag.getInt(1);
                PreparedStatement unlink = conn.prepareStatement("DELETE FROM photo_tags WHERE photo_id = ? AND tag_id = ?");
                unlink.setInt(1, p.getId());
                unlink.setInt(2, tagId);
                unlink.executeUpdate();
                p.getTags().remove(tagName);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}

// MODELE DANYCH

class User {
    private int id;
    private String login, password;
    public User(int id, String login, String password) { this.id = id; this.login = login; this.password = password; }
    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
}

class Photo {
    private int id;
    private String filename, date, resolution;
    private byte[] fullData, thumbnailData;
    private long fileSize;
    private List<String> tags = new ArrayList<>();

    public Photo(int id, String filename, byte[] fullData, byte[] thumbnailData, String date, String resolution, long fileSize) {
        this.id = id; this.filename = filename; this.fullData = fullData; this.thumbnailData = thumbnailData;
        this.date = date; this.resolution = resolution; this.fileSize = fileSize;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFilename() { return filename; }
    public byte[] getFullData() { return fullData; }
    public byte[] getThumbnailData() { return thumbnailData; }
    public String getResolution() { return resolution; }
    public long getFileSize() { return fileSize; }
    public List<String> getTags() { return tags; }
}

class Album {
    private int id;
    private String name;
    private int ownerId;
    private List<Photo> photos = new ArrayList<>();

    public Album(int id, String name, int ownerId) { this.id = id; this.name = name; this.ownerId = ownerId; }
    public int getId() { return id; }
    public String getName() { return name; }
    public int getOwnerId() { return ownerId; }
    public List<Photo> getPhotos() { return photos; }
    @Override public String toString() { return name; }
}