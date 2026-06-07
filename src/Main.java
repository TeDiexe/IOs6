import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;

public class Main extends JFrame {
    private DatabaseManager dataManager;
    private User currentUser;
    private DefaultListModel<Album> albumListModel;
    private JList<Album> albumList;
    private DefaultListModel<Photo> photoListModel;
    private JList<Photo> photoList;
    private JTextField tagFilterField;
    private ImagePreviewPanel previewPanel;
    private JTextArea metadataArea;
    private JList<String> tagsDisplayList;
    private DefaultListModel<String> tagsListModel;

    public Main() {
        dataManager = new DatabaseManager(); // Inicjalizacja bazy
        loginScreen();
        setupGUI();
    }

    private void loginScreen() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {"Login:", usernameField, "Hasło:", passwordField};
        int option = JOptionPane.showConfirmDialog(null, message, "Logowanie", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            currentUser = dataManager.login(usernameField.getText(), new String(passwordField.getPassword()));
            if (currentUser == null) {
                JOptionPane.showMessageDialog(null, "Błędny login lub hasło.");
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    private void setupGUI() {
        setTitle("System Albumu");
        setSize(1300, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        albumListModel = new DefaultListModel<>();
        refreshAlbums();
        albumList = new JList<>(albumListModel);
        albumList.addListSelectionListener(e -> {
            updatePhotoList();
            clearPreview();
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Twoje Albumy"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(albumList), BorderLayout.CENTER);

        JButton addAlbumBtn = new JButton("Nowy Album");
        addAlbumBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Nazwa albumu:");
            if (name != null && !name.trim().isEmpty()) {
                dataManager.createAlbum(name, currentUser);
                refreshAlbums(); // Odświeża listę po dodaniu z bazy
            }
        });
        leftPanel.add(addAlbumBtn, BorderLayout.SOUTH);

        photoListModel = new DefaultListModel<>();
        photoList = new JList<>(photoListModel);
        photoList.setCellRenderer(new PhotoListCellRenderer());
        photoList.addListSelectionListener(e -> showPhotoPreview());

        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Szukaj:"));
        tagFilterField = new JTextField(12);
        JButton filterBtn = new JButton("Filtruj");
        filterBtn.addActionListener(e -> updatePhotoList());
        filterPanel.add(tagFilterField);
        filterPanel.add(filterBtn);

        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(photoList), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(1, 2));
        JButton uploadBtn = new JButton("Dodaj zdjęcie");
        uploadBtn.addActionListener(e -> uploadPhoto());
        JButton exportBtn = new JButton("Eksportuj");
        exportBtn.addActionListener(e -> exportPhoto());
        actionPanel.add(uploadBtn);
        actionPanel.add(exportBtn);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        previewPanel = new ImagePreviewPanel();

        metadataArea = new JTextArea(5, 20);
        metadataArea.setEditable(false);
        metadataArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        tagsListModel = new DefaultListModel<>();
        tagsDisplayList = new JList<>(tagsListModel);

        JPanel tagEditPanel = new JPanel(new BorderLayout());
        tagEditPanel.setBorder(BorderFactory.createTitledBorder("Tagi"));
        tagEditPanel.add(new JScrollPane(tagsDisplayList), BorderLayout.CENTER);

        JPanel tagButtons = new JPanel(new GridLayout(1, 2));
        JButton addTagBtn = new JButton("Dodaj tag");
        addTagBtn.addActionListener(e -> addTagToSelected());
        JButton removeTagBtn = new JButton("Usuń tag");
        removeTagBtn.addActionListener(e -> removeTagFromSelected());
        tagButtons.add(addTagBtn);
        tagButtons.add(removeTagBtn);
        tagEditPanel.add(tagButtons, BorderLayout.SOUTH);

        JPanel infoContainer = new JPanel(new BorderLayout());
        infoContainer.add(new JScrollPane(metadataArea), BorderLayout.NORTH);
        infoContainer.add(tagEditPanel, BorderLayout.CENTER);

        rightPanel.add(previewPanel, BorderLayout.CENTER);
        rightPanel.add(infoContainer, BorderLayout.SOUTH);

        JSplitPane innerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerPanel, rightPanel);
        innerSplit.setDividerLocation(450);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, innerSplit);
        mainSplit.setDividerLocation(200);
        add(mainSplit, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    private void clearPreview() {
        previewPanel.setImage(null);
        metadataArea.setText("");
        tagsListModel.clear();
    }

    private void refreshAlbums() {
        albumListModel.clear();
        for (Album a : dataManager.getAlbumsForUser(currentUser)) {
            albumListModel.addElement(a);
        }
    }

    private void uploadPhoto() {
        Album selected = albumList.getSelectedValue();
        if (selected == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Obrazy", "jpg", "png", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                byte[] fullData = Files.readAllBytes(file.toPath());
                BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(fullData));
                String res = bimg.getWidth() + "x" + bimg.getHeight();
                byte[] thumbData = createHighQualityThumb(bimg, 600);

                // Id ustawiamy na 0, bo baza danych sama wygeneruje ID podczas zapisu
                Photo photo = new Photo(0, file.getName(), fullData, thumbData, LocalDate.now().toString(), res, file.length());
                dataManager.addPhotoToAlbum(photo, selected);
                updatePhotoList();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage());
            }
        }
    }

    private byte[] createHighQualityThumb(BufferedImage img, int targetWidth) throws IOException {
        double ratio = (double) targetWidth / img.getWidth();
        int targetHeight = (int) (img.getHeight() * ratio);

        BufferedImage thumb = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = thumb.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.95f);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(thumb, null, null), param);
        }
        writer.dispose();
        return baos.toByteArray();
    }

    private void addTagToSelected() {
        Photo selected = photoList.getSelectedValue();
        if (selected != null) {
            String newTag = JOptionPane.showInputDialog("Tag:");
            if (newTag != null && !newTag.trim().isEmpty()) {
                // Wywołanie zapisu tagu do bazy DANYCH
                dataManager.addTagToPhoto(selected, newTag);
                showPhotoPreview();
                photoList.repaint();
            }
        }
    }

    private void removeTagFromSelected() {
        Photo selectedPhoto = photoList.getSelectedValue();
        String selectedTag = tagsDisplayList.getSelectedValue();
        if (selectedPhoto != null && selectedTag != null) {
            // Wywołanie usunięcia tagu z bazy DANYCH
            dataManager.removeTagFromPhoto(selectedPhoto, selectedTag);
            showPhotoPreview();
            photoList.repaint();
        }
    }

    private void exportPhoto() {
        Photo selected = photoList.getSelectedValue();
        if (selected == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(selected.getFilename()));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile())) {
                fos.write(selected.getFullData());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updatePhotoList() {
        photoListModel.clear();
        Album selected = albumList.getSelectedValue();
        if (selected != null) {
            String filter = tagFilterField.getText().trim().toLowerCase();
            for (Photo p : selected.getPhotos()) {
                if (filter.isEmpty() || p.getTags().stream().anyMatch(t -> t.contains(filter))) {
                    photoListModel.addElement(p);
                }
            }
        }
    }

    private void showPhotoPreview() {
        Photo selected = photoList.getSelectedValue();
        if (selected != null) {
            previewPanel.setImage(selected.getFullData());

            double sizeInMb = selected.getFileSize() / (1024.0 * 1024.0);
            metadataArea.setText(String.format(
                    "NAZWA: %s\nROZDZIELCZOŚĆ: %s\nROZMIAR: %.2f MB",
                    selected.getFilename(), selected.getResolution(), sizeInMb
            ));
            tagsListModel.clear();
            for (String tag : selected.getTags()) tagsListModel.addElement(tag);
        } else {
            clearPreview();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}