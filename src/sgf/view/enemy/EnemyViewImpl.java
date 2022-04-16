package sgf.view.enemy;

import sgf.controller.enemy.EnemyController;
import sgf.helpers.ImageLoader;
import sgf.helpers.ImgTileSize;
import sgf.managers.BarLifeImageManager;
import sgf.managers.EnemyImageManager;
import sgf.managers.EnemyManager;
import sgf.model.enemies.Enemy;
import sgf.model.enemies.EnemyType;
import sgf.utilities.LockClass;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import  java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Panel for enemy's movement and appearance.
 */
public class EnemyViewImpl extends AbstractEnemyView {
    private static final long serialVersionUID = 6345414040020937047L;
    private static final int RGB_MAX = 255;     // Maximum value that a RGB parameter must assume.
    private static final int BAR_HEIGHT = 8;
    private EnemyController enemyController;
    private final ImageLoader<EnemyType> imageEnemyController;      // Contains the links between enemy type and images.
    private final ImageLoader<Integer> imageBarController;
    private final BufferedImage image;  // Empty image of total panel size to replace and hide previous effective enemy image.
    private List<EnemyManager> enemyList;       // List of enemies to be showed.
    private boolean isControllerSet;
    private final int tileSize;
    private boolean ready;

    /**
     * Constructor that sets the image, image controller and list of enemies.
     * @param matrixSize Is the size that the enemy's image must have.
     */
    public EnemyViewImpl(final int matrixSize) {
        this.tileSize = ImgTileSize.getTileSize();
        this.image = new BufferedImage(matrixSize * this.tileSize, matrixSize * this.tileSize, BufferedImage.TYPE_INT_ARGB);
        this.imageEnemyController = new EnemyImageManager();
        this.imageBarController = new BarLifeImageManager();
        this.setVisible(false);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (this.ready) {
            final var gImage = (Graphics2D) this.image.getGraphics();
            gImage.setBackground(new Color(RGB_MAX, RGB_MAX, RGB_MAX, 0));
            gImage.clearRect(0, 0, this.image.getWidth(), this.image.getHeight());  // Clears the image area before repaint in another position.
            this.drawComponents(gImage);
            // The panel is covered with an empty image in order to hide the previous enemy image displayed.
            g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
        }
    }

    private void drawComponents(final Graphics2D gImage) {
        // For each enemy in the list repaint it.
            LockClass.getEnemySemaphore().acquireUninterruptibly();
            this.enemyList.forEach(x -> {
                final var enemy = x.getEnemy();
                this.drowSprite(gImage, enemy);
                this.drowLifeBar(gImage, enemy);
                //SwingUtilities.invokeLater(() -> x.damage(1)); // TODO TOGLIERE.
            });
            LockClass.getEnemySemaphore().release();
    }

    private void drowLifeBar(final Graphics2D gImage, final Enemy enemy) {
        gImage.drawImage(this.imageBarController.getImage(0),
                (int) enemy.getPosition().getX(),
                (int) enemy.getPosition().getY(),
                (int) (this.tileSize * enemy.getPercentHp()), BAR_HEIGHT, null);
    }

    private void drowSprite(final Graphics2D gImage, final Enemy enemy) {
        gImage.drawImage(this.imageEnemyController.getImage(enemy.getEnemyType()),
                (int) enemy.getPosition().getX(),
                (int) enemy.getPosition().getY(),
                this.tileSize, this.tileSize, null);
    }

    @Override
    public void setController(final EnemyController controller) {
        if (!isControllerSet) {
            this.isControllerSet = true;
            this.enemyController = controller;
            this.enemyList = this.enemyController.getManagerList();
        }
    }

    @Override
    public void winGame() {
        JOptionPane.showMessageDialog(new JFrame(), "You win the game, your progress will be saved and the game will close!!!", "The end", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0); // TODO MIGLIORARE.
    }

    @Override
    public void start() {
        if (isControllerSet) {
            this.ready = true;
            this.setVisible(true);
        } else {
            throw new IllegalStateException("Cannot invoke start() if the controller has not been set.");
        }
    }

    @Override
    public void stopView() {
        this.enemyController.stopController();
        this.enemyList.forEach(x -> x.stopThread());
    }
}
