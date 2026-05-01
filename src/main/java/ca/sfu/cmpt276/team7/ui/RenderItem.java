package ca.sfu.cmpt276.team7.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * A lightweight, immutable render command to be consumed by {@link DrawQueue}.
 * <p>
 * A {@code RenderItem} represents exactly one drawable element.
 * The {@link RenderKind} determines which fields are meaningful:
 * <ul>
 *  <li><b>RECTANGLE</b>: uses x/y/width/height and {@link #color}</li>
 *  <li><b>TEXT</b>: uses x/y, {@link #color}, {@link #text}, {@link #font}</li>
 *  <li><b>SPRITE</b>: uses x/y/width/height, {@link #sheetId} and src rect (srcX/srcY/srcW/srcH)</li>
 * </ul>
 * Items are sorted by {@link #layer} before drawing (lower layers render first).
 * 
 * @author Yui Matsumoto
 * @version 1.0
 */
public class RenderItem {
    private final int layer;
    private final RenderKind kind;

    /** Common destination rectangle (or anchor point for TEXT). */
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    /** Color used for RECTANGLE and TEXT (unused for SPRITE). */
    private final Color color;

    /** Text content and font (used only for TEXT). */
    private final String text;
    private final Font font;

    /** Sprite sheet source information (used only for SPRITE). */
    private final SheetId sheetId;
    private final int srcX;
    private final int srcY;
    private final int srcW;
    private final int srcH;
    
    /**
     * Private constructor. Use the static factory methods:
     * {@link #rect(int, int, int, int, int, Color)},
     * {@link #text(int, int, int, Color, String, Font)},
     * {@link #sprite(int, int, int, int, int, SheetId, int, int, int, int)}.
     */
    private RenderItem(int layer, RenderKind kind, int x, int y, int width, int height,
                        Color color, String text, Font font,
                        SheetId sheetId, int srcX, int srcY, int srcW, int srcH) {
        this.layer = layer;
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.text = text;
        this.font = font;
        this.sheetId = sheetId;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcW = srcW;
        this.srcH = srcH;
    }

    /**
     * Creates a rectangle render item.
     * 
     * @param layer draw layer (lower drawn first)
     * @param x destination x (pixels)
     * @param y destination y (pixels)
     * @param width rectangle width (pixels)
     * @param height rectangle height (pixels)
     * @param color fill color
     * @return a new rectangle RenderItem
     */
    public static RenderItem rect(int layer, int x, int y, int width, int height, Color color) {
        return new RenderItem(layer, RenderKind.RECTANGLE, x, y, width, height, color, "", null, SheetId.NONE, 0, 0, 0, 0);
    }

    /**
     * Creates a text render item. Width/height are not used for text.
     * 
     * @param layer draw layer (lower drawn first)
     * @param x text anchor x (pixels)
     * @param y text baseline y (pixels)
     * @param color text color
     * @param text string to draw
     * @param font font used to draw the text
     * @return a new text RenderItem
     */
    public static RenderItem text(int layer, int x, int y, Color color, String text, Font font) {
        return new RenderItem(layer, RenderKind.TEXT, x, y, 0, 0, color, text, font, SheetId.NONE, 0, 0, 0, 0);
    }

    /**
     * Creates a sprite render item.
     * 
     * @param layer draw layer (lower drawn first)
     * @param x destination x (pixels)
     * @param y destination y (pixels)
     * @param width destination width (pixels)
     * @param height destination height (pixels)
     * @param sheetId sprite sheet identifier
     * @param srcX source x in the sprite sheet (pixels)
     * @param srcY source y in the sprite sheet (pixels)
     * @param srcW source width in the sprite sheet (pixels)
     * @param srcH source height in the sprite sheet (pixels)
     * @return a new sprite RenderItem
     */
    public static RenderItem sprite(int layer, int x, int y, int width, int height, SheetId sheetId, int srcX, int srcY, int srcW, int srcH) {
        return new RenderItem(layer, RenderKind.SPRITE, x, y, width, height, null, "", null, sheetId, srcX, srcY, srcW, srcH);
    }


    /**
     * @return the draw layer (lower layers are rendered first)
     */
    public int getLayer() {
        return layer;
    }
    
    /**
     * @return the render kind (RECTANGLE, TEXT, or SPRITE)
     */
    public RenderKind getKind() {
        return kind;
    }

    /**
     * @return destination x in pixels
     */
    public int getX() {
        return x;
    }

    /**
     * @return destination y in pixels
     */
    public int getY() {
        return y;
    }

    /**
     * @return destination width in pixels (unused for TEXT)
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return destination height in pixels (unused for TEXT)
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return color for RECTANGLE/TEXT, or null for SPRITE
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return text content (empty string for non-TEXT)
     */
    public String getText() {
        return text;
    }
    
    /**
     * @return font for TEXT, or null otherwise
     */
    public Font getFont() {
        return font;
    }

    /**
     * @return sprite sheet id for SPRITE, or {@link SheetId#NONE} otherwise
     */
    public SheetId getSheetId() {
        return sheetId;
    }

    /**
     * @return source x in the sprite sheet (SPRITE only)
     */
    public int getSrcX() {
        return srcX;
    }

    /**
     * @return source y in the sprite sheet (SPRITE only)
     */
    public int getSrcY() {
        return srcY;
    }
    
    /**
     * @return source width in the sprite sheet (SPRITE only)
     */
    public int getSrcW() {
        return srcW;
    }

    /**
     * @return source height in the sprite sheet (SPRITE only)
     */
    public int getSrcH() {
        return srcH;
    }
}
