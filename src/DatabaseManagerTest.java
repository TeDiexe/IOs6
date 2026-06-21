import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DatabaseManagerTest {

    private DatabaseManager databaseManager;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        databaseManager = new DatabaseManager();
    }

    // ==========================================
    // 1. TESTY LOGIKI BAZODANOWEJ (DatabaseManager)
    // ==========================================

    @Test
    public void testLoginSuccess() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("user_id")).thenReturn(123);
            when(mockResultSet.getString("username")).thenReturn("admin");
            when(mockResultSet.getString("password_hash")).thenReturn("admin");

            User user = databaseManager.login("admin", "admin");

            assertNotNull(user);
            assertEquals(123, user.getId());
            assertEquals("admin", user.getLogin());
        }
    }

    @Test
    public void testLoginFailure() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(false);

            User user = databaseManager.login("zly_user", "zle_haslo");

            assertNull(user);
        }
    }

    @Test
    public void testCreateAlbum() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            User owner = new User(1, "admin", "admin");
            databaseManager.createAlbum("Nowy Album Testowy", owner);

            verify(mockPreparedStatement).setString(1, "Nowy Album Testowy");
            verify(mockPreparedStatement).setInt(2, owner.getId());
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    public void testGetAlbumsForUserWithPhotos() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            User user = new User(1, "admin", "admin");

            PreparedStatement stmtAlbums = mock(PreparedStatement.class);
            PreparedStatement stmtPhotos = mock(PreparedStatement.class);
            PreparedStatement stmtTags = mock(PreparedStatement.class);

            ResultSet rsAlbums = mock(ResultSet.class);
            ResultSet rsPhotos = mock(ResultSet.class);
            ResultSet rsTags = mock(ResultSet.class);

            when(mockConnection.prepareStatement(contains("FROM albums"))).thenReturn(stmtAlbums);
            when(stmtAlbums.executeQuery()).thenReturn(rsAlbums);
            when(rsAlbums.next()).thenReturn(true, false);
            when(rsAlbums.getInt("album_id")).thenReturn(55);
            when(rsAlbums.getString("name")).thenReturn("Wakacje");

            when(mockConnection.prepareStatement(contains("FROM photos"))).thenReturn(stmtPhotos);
            when(stmtPhotos.executeQuery()).thenReturn(rsPhotos);
            when(rsPhotos.next()).thenReturn(true, false);
            when(rsPhotos.getInt("photo_id")).thenReturn(99);
            when(rsPhotos.getString("filename")).thenReturn("img.jpg");
            when(rsPhotos.getBytes("full_data")).thenReturn(new byte[]{1, 2, 3, 4});
            when(rsPhotos.getBytes("thumbnail_data")).thenReturn(new byte[]{1, 2});

            Date mockSqlDate = new Date(System.currentTimeMillis());
            when(rsPhotos.getDate("upload_date")).thenReturn(mockSqlDate);
            when(rsPhotos.getString("resolution")).thenReturn("1920x1080");
            when(rsPhotos.getLong("file_size_bytes")).thenReturn(500L);

            when(mockConnection.prepareStatement(contains("FROM tags"))).thenReturn(stmtTags);
            when(stmtTags.executeQuery()).thenReturn(rsTags);
            when(rsTags.next()).thenReturn(true, false);
            when(rsTags.getString("tag_name")).thenReturn("krajobraz");

            List<Album> albums = databaseManager.getAlbumsForUser(user);

            assertNotNull(albums);
            assertEquals(1, albums.size());
            assertEquals("Wakacje", albums.get(0).getName());
            assertEquals(55, albums.get(0).getId());

            assertEquals(1, albums.get(0).getPhotos().size());
            Photo loadedPhoto = albums.get(0).getPhotos().get(0);
            assertEquals("img.jpg", loadedPhoto.getFilename());
            assertTrue(loadedPhoto.getTags().contains("krajobraz"));
        }
    }

    @Test
    public void testAddPhotoToAlbum() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            // Baza używa generatedColumns = {"PHOTO_ID"} i pobiera klucz główny
            when(mockConnection.prepareStatement(anyString(), any(String[].class))).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(777); // Symulowane nowe ID zdjęcia z bazy

            Album album = new Album(10, "Testowy", 1);
            Photo photo = new Photo(0, "nowe.png", new byte[]{1}, new byte[]{1}, "2026", "4K", 100L);

            databaseManager.addPhotoToAlbum(photo, album);

            // Sprawdzamy, czy przypisało ID wygenerowane przez bazę danych
            assertEquals(777, photo.getId());
            assertEquals(1, album.getPhotos().size());
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    public void testAddTagToPhotoNew() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            PreparedStatement stmtCheck = mock(PreparedStatement.class);
            PreparedStatement stmtInsert = mock(PreparedStatement.class);
            PreparedStatement stmtLink = mock(PreparedStatement.class);
            ResultSet rsCheck = mock(ResultSet.class);
            ResultSet rsInsert = mock(ResultSet.class);

            // 1. Sprawdzenie czy tag istnieje - zwracamy false (tag jest nowy)
            when(mockConnection.prepareStatement(contains("SELECT tag_id"))).thenReturn(stmtCheck);
            when(stmtCheck.executeQuery()).thenReturn(rsCheck);
            when(rsCheck.next()).thenReturn(false);

            // 2. Tworzenie nowego tagu
            when(mockConnection.prepareStatement(contains("INSERT INTO tags"), any(String[].class))).thenReturn(stmtInsert);
            when(stmtInsert.getGeneratedKeys()).thenReturn(rsInsert);
            when(rsInsert.next()).thenReturn(true);
            when(rsInsert.getInt(1)).thenReturn(88); // nowe ID tagu

            // 3. Łączenie zdjęcia z tagiem
            when(mockConnection.prepareStatement(contains("INSERT INTO photo_tags"))).thenReturn(stmtLink);

            Photo photo = new Photo(5, "foto.jpg", new byte[]{0}, new byte[]{0}, "2026", "1080p", 100L);

            databaseManager.addTagToPhoto(photo, " NOWY_TAG "); // Przekazujemy z dużych liter i spacjami, żeby sprawdzić .trim().toLowerCase()

            assertTrue(photo.getTags().contains("nowy_tag"));
            verify(stmtInsert).executeUpdate();
            verify(stmtLink).executeUpdate();
        }
    }

    @Test
    public void testRemoveTagFromPhoto() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            PreparedStatement stmtGetTag = mock(PreparedStatement.class);
            PreparedStatement stmtUnlink = mock(PreparedStatement.class);
            ResultSet rsTag = mock(ResultSet.class);

            when(mockConnection.prepareStatement(contains("SELECT tag_id FROM tags"))).thenReturn(stmtGetTag);
            when(stmtGetTag.executeQuery()).thenReturn(rsTag);
            when(rsTag.next()).thenReturn(true);
            when(rsTag.getInt(1)).thenReturn(99); // znalezione ID tagu

            when(mockConnection.prepareStatement(contains("DELETE FROM photo_tags"))).thenReturn(stmtUnlink);

            Photo photo = new Photo(5, "foto.jpg", new byte[]{0}, new byte[]{0}, "2026", "1080p", 100L);
            photo.getTags().add("usuwany");

            databaseManager.removeTagFromPhoto(photo, "USUWANY");

            assertFalse(photo.getTags().contains("usuwany"));
            verify(stmtUnlink).executeUpdate();
        }
    }

    // ==========================================
    // 2. TESTY MODELI (Dostosowane do DatabaseManager.java)
    // ==========================================

    @Test
    public void testUserModelGetters() {
        User user = new User(10, "gracz", "haslo123");
        assertEquals(10, user.getId());
        assertEquals("gracz", user.getLogin());
        assertEquals("haslo123", user.getPassword());
    }

    @Test
    public void testAlbumModelAndPhotoHandling() {
        Album album = new Album(1, "CS2 Clips", 10);
        assertEquals(1, album.getId());
        assertEquals("CS2 Clips", album.getName());
        assertEquals(10, album.getOwnerId());
        assertTrue(album.getPhotos().isEmpty());

        Photo photo = new Photo(2, "aim_rush.jpg", new byte[]{0}, new byte[]{0}, "2026", "1080p", 100L);
        album.getPhotos().add(photo);

        assertEquals(1, album.getPhotos().size());
        assertEquals("aim_rush.jpg", album.getPhotos().get(0).getFilename());
        assertEquals("CS2 Clips", album.toString());
    }

    @Test
    public void testPhotoModelGettersAndTags() {
        byte[] full = {1, 2, 3, 4};
        byte[] thumb = {1, 2};
        Photo photo = new Photo(5, "screenshot.png", full, thumb, "2026-06", "2K", 2048L);

        assertEquals(5, photo.getId());
        assertEquals("screenshot.png", photo.getFilename());
        assertArrayEquals(full, photo.getFullData());
        assertArrayEquals(thumb, photo.getThumbnailData());
        assertEquals("2K", photo.getResolution());
        assertEquals(2048L, photo.getFileSize());

        assertNotNull(photo.getTags());
        photo.getTags().add("gaming");

        assertEquals(1, photo.getTags().size());
        assertTrue(photo.getTags().contains("gaming"));
    }
}