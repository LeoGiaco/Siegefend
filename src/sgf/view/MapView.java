package sgf.view;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import sgf.controller.TileController;
import sgf.model.GridPosition;
import sgf.model.Map;

/**
 * This class is responsible for the process of map showing.
 */
public class MapView extends JPanel implements ComponentListener, MouseListener {
    private static final long serialVersionUID = -7141712951441617040L;
    private final int matrixSize;
    private final TileController tileController;
    private final BufferedImage completeMap;
    private final Map map;
    private Consumer<MouseEvent> mouseHandler;
    private final int size;
    private boolean mapCreated;
    private boolean needUpdate;

    /**
     * Constructor that link this component in such a way that it can be listened.
     * @param map Initializes the internal map.
     */
    public MapView(final Map map, final int size) {
        this.matrixSize = map.getMatrixSize();
        this.completeMap = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        this.tileController = new TileController();
        this.addComponentListener(this);
        this.addMouseListener(this);   // Links this panel with a controller of mouse events.
        this.size = size;
        this.mapCreated = false;
        this.map = map;
    }

    /**
     * Initializes the internal field mouse handler.
     * @param m
     */
    public void addMouseHandler(final Consumer<MouseEvent> m) {
        this.mouseHandler = m;
    }




    /**
     * This method, reading the internal field map, calculates the correspondent grid of cells that will be composing the map.
     */
    public void createMapImage() {
        final Graphics g = completeMap.getGraphics();
        final int tileSize = this.size / this.matrixSize;
        for (int row = 0; row < matrixSize; row++) {
            for (int column = 0; column < matrixSize; column++) {
                final Image i = tileController.getImage(this.map.getTileFromGridPosition(new GridPosition(column, row)));
                g.drawImage(i, column * tileSize, row *  tileSize,  tileSize,  tileSize, null);
            }
        }
        try {
            ImageIO.write(completeMap, "PNG", new File("res" + File.separator + "testimage.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simple getter for the matrix size field.
     * @return an integer that is the matrix number of tiles per dimension.
     */
    public int getMatrixSize() {
        return this.matrixSize;
    }

    /**
     * Checks whether the window with the map has been updated and needs to be redrawn.
     * @return a boolean
     */
    public boolean isUpdateNeeded() {
        return needUpdate;
    }

    @Override
    public void componentResized(final ComponentEvent e) {
        this.needUpdate = true;
    }

    // TODO Find a way to remove this following void methods that compares after implementing interface.

    @Override
    public void componentMoved(final ComponentEvent e) {
    }

    @Override
    public void componentShown(final ComponentEvent e) {
    }

    @Override
    public void componentHidden(final ComponentEvent e) {
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        // Simple way to obtain and print mouse position when clicking.
        if (e.getButton() == MouseEvent.BUTTON1) {
            this.mouseHandler.accept(e);
        }
    }

 // TODO Find a way to remove this following void methods that compares after implementing interface.

    @Override
    public void mousePressed(final MouseEvent e) {

    }

    @Override
    public void mouseReleased(final MouseEvent e) {

    }

    @Override
    public void mouseEntered(final MouseEvent e) {

    }

    @Override
    public void mouseExited(final MouseEvent e) {

    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (!mapCreated) {
            mapCreated = true;
            this.createMapImage();
        }
        g.drawImage(completeMap, 0, 0, this.getWidth(), this.getHeight(), null);
    }
}
