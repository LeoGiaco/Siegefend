package sgf.view.turret;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;
import sgf.controller.turret.TurretController;
import sgf.helpers.ImgTileSize;
import sgf.managers.TurretImageManager;
import sgf.model.game.Stoppable;
import sgf.model.map.GridPosition;
import sgf.model.map.Map;
import sgf.model.map.Position;
import sgf.model.map.TileType;
import sgf.model.turret.Turret;
import sgf.utilities.ThreadAndViewObservable;

/**
 * View of a turret.
 */
public class TurretViewImpl extends AbstractTurretView implements MouseListener, Stoppable {

    private static final long serialVersionUID = 5374324545913407109L; 
    private final int matrixSize;
    private final int tileSize;
    private final BufferedImage image;
    private final Semaphore semaphore;
    private final TurretImageManager imgManager;
    private final Map map;
    private boolean isControllerSet;
    private boolean ready;
    private TurretController turretController;
    private Turret clickedTurret;

    /**
     * Creates a new instance of the class.
     * @param map the {@link Map}
     * @param semaphore the {@link Semaphore}
     */
    public TurretViewImpl(final Map map, final Semaphore semaphore) {
        this.setVisible(false);
        this.map = map;
        this.matrixSize = map.getSize();
        this.tileSize = ImgTileSize.getTileSize();
        this.image = new BufferedImage(matrixSize * this.tileSize, matrixSize * this.tileSize, BufferedImage.TYPE_INT_ARGB);
        this.semaphore = semaphore;
        this.imgManager = new TurretImageManager();
    }

    @Override
    public void setController(final TurretController controller) {
        if (!isControllerSet) {
            this.isControllerSet = true;
            this.turretController = controller;
        }
    }

    @Override
    public void start() {
        if (this.isControllerSet) {
            ThreadAndViewObservable.register(this);
            this.ready = true;
            this.setVisible(true);
            this.addMouseListener(this);
        } else {
            throw new IllegalStateException("Cannot invoke start() if the controller has not been set.");
        }
    }

    @Override
    public void stop() {
        this.ready = false;
        this.setVisible(false);
    }

    /**
     * Returns a rotated image.
     * @param img the {@link Image} we want to rotate
     * @param rads the rotation angle in radians
     * @return the rotated image
     */
    public static BufferedImage rotateImage(final Image img, final double rads) {
        final double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        final int w = img.getWidth(null);
        final int h = img.getHeight(null);
        final int newWidth = (int) Math.floor(w * cos + h * sin);
        final int newHeight = (int) Math.floor(h * cos + w * sin);

        final BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = rotated.createGraphics();
        final AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        final int x = w / 2;
        final int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            final int gridColumn = this.convertCoordinate(e.getX(), this.getWidth());
            final int gridRow = this.convertCoordinate(e.getY(), this.getHeight());
            final GridPosition pos = new GridPosition(gridRow, gridColumn);
            if (this.turretController.isTurretSelected()) {
                if (this.map.getTileFromGridPosition(pos).getTileType() == TileType.GRASS) {
                    this.turretController.addSelectedTurret(new GridPosition(gridRow, gridColumn));
                }
            } else {
                this.clickedTurret = this.turretController.getTurretAt(pos).orElse(null);
            }
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) { }

    @Override
    public void mouseReleased(final MouseEvent e) { }

    @Override
    public void mouseEntered(final MouseEvent e) { }

    @Override
    public void mouseExited(final MouseEvent e) { }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (this.ready) {
            final var gImage = (Graphics2D) this.image.getGraphics();
            gImage.setBackground(new Color(0, 0, 0, 0));
            gImage.clearRect(0, 0, this.image.getWidth(), this.image.getHeight());
            this.drawTurrets(gImage);
            // Draws a circle representing the range of the turret
            if (clickedTurret != null) {
                final Position p = clickedTurret.getPosition();
                gImage.setStroke(new BasicStroke(3));
                gImage.setColor(Color.LIGHT_GRAY);
                gImage.drawOval((int) (p.getX() - clickedTurret.getRange() + this.tileSize / 2), (int) (p.getY() - clickedTurret.getRange() + this.tileSize / 2), 
                                (int) clickedTurret.getRange() * 2, (int) clickedTurret.getRange() * 2);
            }
            g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
        }
    }

    private void drawTurrets(final Graphics2D gImage) {
        semaphore.acquireUninterruptibly();
        final var iterator = this.turretController.getTurretsIterator();
        while (iterator.hasNext()) {
            final var entry = iterator.next();
            final int entryID = entry.getValue().getID();
            final Position p = entry.getValue().getPosition();
            final int origW = imgManager.getImage(entryID).getWidth(null);
            final int origH = imgManager.getImage(entryID).getHeight(null);
            final BufferedImage bimg = rotateImage(imgManager.getImage(entryID), entry.getValue().getAngle());
            final double scaleX = (double) bimg.getWidth() / origW;
            final double scaleY = (double) bimg.getHeight() / origH;
            gImage.drawImage(bimg, (int) (p.getX() - (this.tileSize * (scaleX - 1) / 2)), (int) (p.getY() - (this.tileSize * (scaleY - 1) / 2)), (int) (this.tileSize * scaleX), (int) (this.tileSize * scaleY), null);
        }
        semaphore.release();
    }

    // Taken a value and the dimension in refers to, it returns an integer value that is the corresponding tile position in the dimension.
    private int convertCoordinate(final int x, final double dimension) {
        final double sizeOfASingleTile = dimension / this.matrixSize;
        return (int) (x / sizeOfASingleTile);
    }

}
