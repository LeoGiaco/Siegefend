package sgf.view;

import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
/**
 * 
 * This class is responsible for the process of map's creation and showing.
 *
 */
public class MapCreator extends JPanel {
    private static final long serialVersionUID = -7141712951441617040L;
    private static final int MATRIX_SIZE = 20;
    private BufferedImage grassImage;

    /**
     * Constructor for a map creator.
     */
    public MapCreator() {
        try {
            grassImage = ImageIO.read(new FileInputStream("res" + File.separator + "grass.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Auto-called method that loads and show a simple image from res folder.
     * @param graphic Is the argument for this method.
     */
    public void paintComponent(final Graphics graphic) {
        super.paintComponent(graphic);
        final int widthImage = this.sizeImage(this.getWidth());
        final int heightImage =  this.sizeImage(this.getHeight());
        final Image modifiedImage = this.dinamicResize(widthImage, heightImage);
        for (int x = 0; x < MATRIX_SIZE; x++) {
            for (int y = 0; y < MATRIX_SIZE; y++) {
                graphic.drawImage(modifiedImage, x * widthImage, y * heightImage, null);
            }
        }
    }

    private Image dinamicResize(final int widthImage, final int heightImage) {
        return  grassImage.getScaledInstance(widthImage, heightImage, Image.SCALE_SMOOTH);
    }

    private int sizeImage(final double dimension) {
        return (int) Math.round(dimension / MATRIX_SIZE);
    }
}
