import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BinaryDataManager {
    private static final String DATA_FILE = "baza_v2.dat";
    private AppData appData;

    public BinaryDataManager() {
        load();
        if (appData.users.isEmpty()) {
            appData.users.add(new User("1", "admin", "admin"));
            save();
        }
    }

    public User login(String l, String p) {
        return appData.users.stream()
                .filter(u -> u.getLogin().equals(l) && u.getPassword().equals(p))
                .findFirst().orElse(null);
    }

    public List<Album> getAlbumsForUser(User u) {
        return appData.albums.stream()
                .filter(a -> a.getOwnerId().equals(u.getId()))
                .collect(Collectors.toList());
    }

    public void createAlbum(String n, User o) {
        appData.albums.add(new Album(n, o.getId()));
        save();
    }

    public void addPhotoToAlbum(Photo p, Album a) {
        a.getPhotos().add(p);
        save();
    }

    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(appData);
        } catch (IOException e) {}
    }

    private void load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) {
            appData = new AppData();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            appData = (AppData) ois.readObject();
        } catch (Exception e) {
            appData = new AppData();
        }
    }
}

class User implements Serializable {
    private static final long serialVersionUID = 6L;
    private String id, login, password;
    public User(String id, String login, String password) { this.id = id; this.login = login; this.password = password; }
    public String getId() { return id; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
}

class Photo implements Serializable {
    private static final long serialVersionUID = 6L;
    private String filename, date, resolution;
    private byte[] fullData, thumbnailData;
    private long fileSize;
    private List<String> tags = new ArrayList<>();

    public Photo(String filename, byte[] fullData, byte[] thumbnailData, String date, String resolution, long fileSize) {
        this.filename = filename; this.fullData = fullData; this.thumbnailData = thumbnailData;
        this.date = date; this.resolution = resolution; this.fileSize = fileSize;
    }

    public void addTag(String tag) { if(!tags.contains(tag.toLowerCase())) tags.add(tag.toLowerCase()); }
    public String getFilename() { return filename; }
    public byte[] getFullData() { return fullData; }
    public byte[] getThumbnailData() { return thumbnailData; }
    public String getResolution() { return resolution; }
    public long getFileSize() { return fileSize; }
    public List<String> getTags() { return tags; }
}

class Album implements Serializable {
    private static final long serialVersionUID = 6L;
    private String name, ownerId;
    private List<Photo> photos = new ArrayList<>();

    public Album(String name, String ownerId) { this.name = name; this.ownerId = ownerId; }
    public String getName() { return name; }
    public String getOwnerId() { return ownerId; }
    public List<Photo> getPhotos() { return photos; }
    @Override public String toString() { return name; }
}

class AppData implements Serializable {
    private static final long serialVersionUID = 6L;
    public List<User> users = new ArrayList<>();
    public List<Album> albums = new ArrayList<>();
}