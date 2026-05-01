package ca.sfu.cmpt276.team7.ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.imageio.ImageIO;

/**
 * Manages a queue of {@link RenderItem}s to be drawn in layer order.
 * <p>
 * The typical flow per frame is:
 * <ol>
 *  <li>Enqueue render items (sprites, text, rectangles).</li>
 *  <li>Sort by {@link RenderItem#getLayer()}.</li>
 *  <li>Render everything onto the provided {@link Graphics} context.</li>
 * </ol>
 * This class also owns and provides access to the sprite sheets (atlases).
 * 
 * @author Yui Matsumoto
 * @version 1.0
 */
public class DrawQueue {

    /** FIFO queue storing render commands for the current frame. */
    private final Queue<RenderItem> queue;

    /** Sprite atlases used by SPRITE render items. */
    private final BufferedImage gameAtlas;
    private final BufferedImage screensAtlas;

    /**
     * Creates an empty draw queue and loads required sprite atlases from resources.
     */
    public DrawQueue() {
        this.queue = new LinkedList<>();
        this.gameAtlas = loadImage("/sprites/atlas_game.png");
        this.screensAtlas = loadImage("/sprites/atlas_screens.png");
    }

    /**
     * Loads an image resource from the classpath.
     *
     * @param resourcePath absolute resource path (starting with '/')
     * @return loaded {@link BufferedImage}
     * @throws RuntimeException if the resource cannot be loaded
     */
    private static BufferedImage loadImage(String resourcePath) {
        try (InputStream is = DrawQueue.class.getResourceAsStream(resourcePath)) {
            return ImageIO.read(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load image: " + resourcePath, e);
        }
    }

    /**
     * Selects the correct sprite sheet image for the given {@link SheetId}.
     *
     * @param id which atlas to use
     * @return the corresponding {@link BufferedImage}
     * @throws IllegalArgumentException if {@code id} is NONE or invalid
     */
    private BufferedImage selectSheet(SheetId id) {
        switch (id) {
            case GAME_ATLAS:
                return gameAtlas;

            case SCREEN_ATLAS:
                return screensAtlas;

            case NONE:
            default:
                throw new IllegalArgumentException("Invalid sheet id: " + id);
        }
    }


    /**
     * Adds a render item to the queue.
     *
     * @param item render item to enqueue
     */
    public void enqueue(RenderItem item) {
        this.queue.offer(item);
    }

    /**
     * Removes all queued render items (typically called at the start of each frame).
     */
    public void clear() {
        this.queue.clear();
    }


    /**
     * Renders all queued items to the given graphics context.
     * <p>
     * Items are sorted by layer before drawing (lower layer first).
     *
     * @param g graphics context to draw onto
     */
    public void renderAll(Graphics g) {
        List<RenderItem> items = new ArrayList<>(queue);
        items.sort((a, b) -> Integer.compare(a.getLayer(), b.getLayer()));

        for (RenderItem item : items) {
            switch (item.getKind()) {
                case RECTANGLE:
                    g.setColor(item.getColor());
                    g.fillRect(item.getX(), item.getY(), item.getWidth(), item.getHeight());
                    break;
                    
                case TEXT:
                    g.setColor(item.getColor());
                    g.setFont(item.getFont());
                    g.drawString(item.getText(), item.getX(), item.getY());
                    break;

                case SPRITE:
                    BufferedImage sheet = selectSheet(item.getSheetId());

                    int x1 = item.getX();
                    int y1 = item.getY();
                    int x2 = x1 + item.getWidth();
                    int y2 = y1 + item.getHeight();

                    int srcX1 = item.getSrcX();
                    int srcY1 = item.getSrcY();
                    int srcX2 = srcX1 + item.getSrcW();
                    int srcY2 = srcY1 + item.getSrcH();

                    g.drawImage(sheet, x1, y1, x2, y2, srcX1, srcY1, srcX2, srcY2, null);
                    break;
            }
        }
    }

    /**
     * Returns a snapshot of the currently queued render items.
     * <p>
     * The returned list is a copy of the queue contents, so modifying the list
     * does not affect the internal draw queue.
     *
     * @return a list containing the render items currently queued for rendering
     */
    public List<RenderItem> getRenderItems() {
        return new ArrayList<>(queue);
    }
}
