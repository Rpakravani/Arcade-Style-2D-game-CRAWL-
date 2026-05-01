package ca.sfu.cmpt276.team7.ui;

/**
 * The type of drawable represented by a {@link RenderItem}.
 * <p>
 * This determines which fields inside {@code RenderItem} are meaningful during rendering.
 * 
 * @author Yui Matsumoto
 * @version 1.0
 */
public enum RenderKind {
    /** Filled rectangle rendered using the item's destination rectangle and color. */
    RECTANGLE,

    /** Text rendered at (x, y) using the item's color, font, and string content. */
    TEXT,

    /** Sprite rendered from a source rectangle on a sprite sheet onto a destination rectangle. */
    SPRITE
}
