import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

public class ImagePreviewPanel extends JPanel {
    private BufferedImage currentImage;

    public ImagePreviewPanel() {
        setBackground(new Color(25, 25, 25));
    }

    public void setImage(byte[] imageData) {
        try {
            currentImage = (imageData != null) ? ImageIO.read(new ByteArrayInputStream(imageData)) : null;
            repaint();
        } catch (Exception e) {
            currentImage = null;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentImage == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double imgAspect = (double) currentImage.getHeight() / currentImage.getWidth();
        double canvasAspect = (double) getHeight() / getWidth();
        int x, y, w, h;

        if (canvasAspect > imgAspect) {
            w = getWidth();
            h = (int) (w * imgAspect);
            x = 0;
            y = (getHeight() - h) / 2;
        } else {
            h = getHeight();
            w = (int) (h / imgAspect);
            y = 0;
            x = (getWidth() - w) / 2;
        }
        g2.drawImage(currentImage, x, y, w, h, null);
    }
}

class PhotoListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Photo) {
            Photo p = (Photo) value;
            label.setText(" " + p.getFilename());
            if (p.getThumbnailData() != null) {
                try {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(p.getThumbnailData()));
                    Image scaled = img.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaled));
                } catch (Exception e) {}
            }
        }
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }
}