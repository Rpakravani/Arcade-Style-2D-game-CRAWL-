package ca.sfu.cmpt276.team7.ui;

/**
 * Identifiers for sprite sheet resources used by {@link RenderItem} of kind SPRITE.
 * <p>
 * {@code NONE} is used for non-sprite render items (RECTANGLE/TEXT) or as a sentinel value.
 * 
 * @author Yui Matsumoto
 * @version 1.0
 */
public enum SheetId {
    /** Sprite sheet containing in-game tiles/characters/icons. */
    GAME_ATLAS,

    /** Sprite sheet containing full-screen images (start/end/popup screens). */
    SCREEN_ATLAS,

    /** Sentinel value indicating "no sheet" (not valid for drawing sprites). */
    NONE
}
