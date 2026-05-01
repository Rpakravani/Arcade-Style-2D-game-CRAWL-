package ca.sfu.cmpt276.team7.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import ca.sfu.cmpt276.team7.EndReason;
import ca.sfu.cmpt276.team7.Game;
import ca.sfu.cmpt276.team7.PopupReason;
import ca.sfu.cmpt276.team7.ScreenState;
import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.BarrierCell;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.cells.WallCell;
import ca.sfu.cmpt276.team7.core.GameCharacter;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.enemies.Goblin;
import ca.sfu.cmpt276.team7.enemies.Ogre;
import ca.sfu.cmpt276.team7.reward.BonusReward;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.PunishmentCell;
import ca.sfu.cmpt276.team7.reward.RegularReward;
import ca.sfu.cmpt276.team7.reward.Reward;
import ca.sfu.cmpt276.team7.reward.RewardCell;

/**
 * Swing panel responsible for rendering the game.
 *
 * <p>This panel builds a {@link DrawQueue} each frame based on the current
 * {@link ScreenState}, then renders all {@link RenderItem}s in layer order.</p>
 *
 * <p>This renderer supports:</p>
 * <ul>
 *   <li>Padding walls when the board is smaller than a minimum viewport size</li>
 *   <li>A player-centered viewport when the board is larger than a maximum viewport size</li>
 * </ul>
 *
 * @author Yui Matsumo
 * @version 1.0
 */
public class GamePanel extends JPanel {

    /** Queue of render commands for the current frame. */
    private final DrawQueue drawQueue = new DrawQueue();
    /** Game model used to query state, score, timers, and characters. */
    private final Game game;
    /** Board model used to query cells and special positions. */
    private final Board board;
    
    /** Board width (in cells). */
    private final int gameX;
    /** Board height (in cells). */
    private final int gameY;
    /** Viewport/render width (in cells), after applying padding and max clamp. */
    private final int renderX;
    /** Viewport/render height (in cells), after applying padding and max clamp. */
    private final int renderY;
    /** Horizontal padding (in cells) added on each side when board is smaller than minX. */
    private final int xOffset;
    /** Vertical padding (in cells) added on each side when board is smaller than minY. */
    private final int yOffset;

    /** Current viewport left bound in board coordinates (inclusive). */
    private int viewStartX = 0;
    /** Current viewport top bound in board coordinates (inclusive). */
    private int viewStartY = 0;
    /** Current viewport right bound in board coordinates (exclusive). */
    private int viewEndX;
    /** Current viewport bottom bound in board coordinates (exclusive). */
    private int viewEndY;

    /** Minimum viewport width (in cells). If board is smaller, padding walls are drawn. */
    private final int minX = 11;
    /** Minimum viewport height (in cells). If board is smaller, padding walls are drawn. */
    private final int minY = 8;

    /** Must be odd (keeps a centered, symmetric player-centered viewport). */
    private final int maxX = 21;
    /** Must be odd (keeps a centered, symmetric player-centered viewport). */
    private final int maxY = 11;

    /**
     *  Creates a panel that renders the given game and board.
     * 
     * @param game  the game model (screen state, score, timers, etc.)
     * @param board the board model (grid, characters, etc.)
     */
    public GamePanel(Game game, Board board) {
        this.game = game;
        this.board = board;

        this.gameX = board.getWidth();
        this.gameY = board.getHeight();
        this.viewEndX = gameX;
        this.viewEndY = gameY;

        this.xOffset = calculateOffset(gameX, minX);
        this.yOffset = calculateOffset(gameY, minY);
        this.renderX = calculateRenderSize(gameX, xOffset, maxX);
        this.renderY = calculateRenderSize(gameY, yOffset, maxY);

        setBackground(Color.BLACK);
        setOpaque(true);
    }
    
    /**
     * Computes padding (per side) when the board is smaller than the minimum viewport.
     * <p>
     * If {@code gameSize >= min}, returns 0.
     * Otherwise, returns how many cells of padding should be added to each side
     * so the content can be centered.
     *
     * @param gameSize board dimension (in cells)
     * @param min      minimum viewport dimension (in cells)
     * @return padding cells per side
     */
    private int calculateOffset(int gameSize, int min) {
        if (gameSize >= min) {
            return 0;
        }
        int shortage = min - gameSize;
        return (shortage + 1) / 2;
    }
    /**
     * Computes final viewport size (in cells), clamped by {@code max}.
     * <p>
     * If the board is larger than {@code max}, the viewport is fixed to {@code max}.
     * Otherwise, it is the board size plus both-side padding.
     *
     * @param gameSize board dimension (in cells)
     * @param offset   padding per side (in cells)
     * @param max      maximum viewport dimension (in cells)
     * @return final render dimension (in cells)
     */
    private int calculateRenderSize(int gameSize, int offset, int max) {
        if (gameSize > max) {
            return max;
        } else {
            return gameSize + offset * 2;
        }
    }

    /** Layer indices (lower values are drawn first / behind). */
    private final int offsetLayer = 0;
    private final int cellLayer = 1;
    private final int markerLayer = 2;
    private final int rewardLayer = 3;
    private final int characterLayer = 4;
    private final int playerLayer = 5;
    private final int hudLayer = 6;
    private final int popupRectLayer = 7;
    private final int popupContentsLayer = 8;

    /** On-screen cell size in pixels. */
    private final int cellWidth = 50;
    private final int cellHeight = 50;

    /** Sprite-sheet tile sizes and padding (source coordinates). */
    private final int gameSrcSize = 64;
    private final int screenSrcSize = 200;
    private final int srcPadding = 5;

    /**
     * Computes the X/Y offset in a sprite sheet for a given tile "order".
     * 
     * @param order tile index (0-based) along the sheet
     * @param srcSize tile size (without padding) in the source sheet
     * @return the source coordinate (x or y) including padding
     */
    private int srcSize(int order, int srcSize) {
        return ((srcSize + (srcPadding * 2)) * order) + srcPadding;
    }

    /**
     * Measures the width of a string in pixels for a given font.
     * 
     * @param text the text to measure
     * @param font the font used for measurement
     * @return width in pixels
     */
    private int getTextWidth(String text, Font font) {
        FontMetrics fm = getFontMetrics(font);
        return fm.stringWidth(text);
    }

    /**
     * Measures the height of a font in pixels (ascent + descent).
     * 
     * @param font the font used for measurement
     * @return height in pixels
     */
    private int getTextHeight(Font font) {
        FontMetrics fm = getFontMetrics(font);
        return fm.getAscent() + fm.getDescent();
    }

    /**
     * Computes the top-left pixel position to center text in the panel.
     * 
     * @param text the text to center
     * @param font the font to use
     * @return {x, y} where y is the text baseline position
     */
    private int[] getCenteredTextXY(String text, Font font) {
        FontMetrics fm = getFontMetrics(font);
        int textW = fm.stringWidth(text);

        int x = getWidth()/2 - textW/2;

        int ascent = fm.getAscent();
        int descent = fm.getDescent();

        int y = getHeight()/2 + (ascent - descent)/2;

        return new int[]{x, y};
    }

    /**
     * Computes the x position to center text horizontally in the panel.
     * 
     * @param text the text to center
     * @param font the font to use
     * @return x coordinate for the text origin
     */
    private int getCenteredTextX(String text, Font font) {
        int x = getWidth()/2 - getTextWidth(text, font)/2;
        return x;
    }

    /**
     * Computes the top-left pixel position to center a rectangle in the panel.
     * 
     * @param width rectangle width
     * @param height rectangle height
     * @return {x, y} top-left corner
     */
    private int[] getCenteredRectXY(int width, int height) {
        int x = getWidth()/2 - width/2;
        int y = getHeight()/2 - height/2;
        return new int[]{x, y};
    }

    /**
     * Preferred size is derived from the render dimensions and cell size.
     * 
     * @return preferred panel size
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(renderX * cellWidth, renderY * cellHeight);
    }


    /**
     * Enqueues a floor tile at the given pixel position.
     * 
     * @param x pixel x
     * @param y pixel y
     */
    private void enqueueFloor(int x, int y) {
        int srcSize = srcSize(0, gameSrcSize);
        RenderItem floor = RenderItem.sprite(cellLayer, x, y, cellWidth + 1, cellHeight + 1, SheetId.GAME_ATLAS, srcSize, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(floor);
    }

    /**
     * Enqueues a wall tile at the given pixel position.
     * <p>
     * Used for both board walls and padding walls (offset regions).
     *
     * @param x pixel x
     * @param y pixel y
     * @param layer the layer index to render this wall tile on
     */
    private void enqueueWall(int x, int y, int layer) {
        int srcSize = srcSize(1, gameSrcSize);
        RenderItem wall = RenderItem.sprite(layer, x, y, cellWidth + 1, cellHeight + 1, SheetId.GAME_ATLAS, srcSize, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(wall);
    } 

    /**
     * Enqueues a barrier tile at the given pixel position.
     * 
     * @param x pixel x
     * @param y pixel y
     */
    private void enqueueBarrier(int x, int y) {
        int srcSize = srcSize(1, gameSrcSize);
        RenderItem barrier = RenderItem.sprite(cellLayer, x, y, cellWidth + 1, cellHeight + 1, SheetId.GAME_ATLAS, srcSize, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(barrier);
    }

    /**
     * Enqueues a bonus reward sprite at the given pixel position.
     * 
     * @param x pixel x
     * @param y pixel y
     */
    private void enqueueBonusReward(int x, int y) {
        int srcSize = srcSize(4, gameSrcSize);
        RenderItem bonusReward = RenderItem.sprite(rewardLayer, x, y, cellWidth, cellHeight, SheetId.GAME_ATLAS, srcSize, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(bonusReward);
    }

    /**
     * Enqueues a regular reward sprite at the given pixel position.
     * 
     * @param x pixel x
     * @param y pixel y
     */
    private void enqueueRegularReward(int x, int y) {
        int srcSize = srcSize(3, gameSrcSize);
        RenderItem regularReward = RenderItem.sprite(rewardLayer, x, y, cellWidth, cellHeight, SheetId.GAME_ATLAS, srcSize, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(regularReward);
    }

    /**
     * Enqueues a trap/punishment tile at the given pixel position.
     * 
     * @param x pixel x
     * @param y pixel y
     */
    private void enqueuePunishment(int x, int y) {
        int srcSize = srcSize(2, gameSrcSize);
        RenderItem punishment = RenderItem.sprite(cellLayer, x, y, cellWidth + 1, cellHeight + 1, SheetId.GAME_ATLAS, srcSize, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(punishment);
    }

    /**
     * Enqueues the visible portion of the board grid (a viewport region).
     * <p>
     * The region is defined by [startX, endX) and [startY, endY) in board coordinates.
     * Pixel coordinates are converted to viewport-relative positions and also include
     * padding offsets when the board is smaller than the minimum viewport.
     *
     * @param grid   the board grid of cells
     * @param startX left edge of visible region (inclusive), in cells
     * @param startY top edge of visible region (inclusive), in cells
     * @param endX   right edge of visible region (exclusive), in cells
     * @param endY   bottom edge of visible region (exclusive), in cells
     */
    private void enqueueGameCells(Cell[][] grid, int startX, int startY, int endX, int endY) {
        for (int row = startY; row < endY; row++) {
            for (int col = startX; col < endX; col++) {
                Cell cell = grid[row][col];

                // World-to-viewport conversion:
                // - subtract startX/startY so the viewport's top-left becomes (0,0)
                // - add xOffset/yOffset so small boards are centered with padding walls
                int x = (col - startX + xOffset) * cellWidth;
                int y = (row - startY + yOffset) * cellHeight;

                if (cell instanceof WallCell) {
                    enqueueWall(x, y, cellLayer);
                } else if (cell instanceof FloorCell) {
                    enqueueFloor(x, y);
                } else if (cell instanceof BarrierCell) {
                    enqueueBarrier(x, y);
                } else if (cell instanceof RewardCell) {
                    enqueueFloor(x, y);
                    Reward r = ((RewardCell) cell).getReward();
                    if (r instanceof BonusReward) {
                        enqueueBonusReward(x, y);
                    } else if (r instanceof RegularReward) {
                        enqueueRegularReward(x, y);
                    }
                } else if (cell instanceof PunishmentCell) {
                    enqueuePunishment(x, y);
                }
            }
        }
    }

    /**
     * Fills a horizontal padding band (top or bottom) with wall tiles.
     *
     * @param startRow the starting row (in render coordinates) to begin filling
     */
    private void fillHorizontalOffset(int startRow) {
        for (int row = 0; row < yOffset; row++) {
            for (int col = 0; col < renderX; col++) {
                int x = col * cellWidth;
                int y = (startRow + row) * cellHeight;
                enqueueWall(x, y, offsetLayer);
            }
        }
    }
    /**
     * Fills a vertical padding band (left or right) with wall tiles.
     *
     * @param startCol the starting column (in render coordinates) to begin filling
     */
    private void fillVerticalOffset(int startCol) {
        for (int col = 0; col < xOffset; col++) {
            for (int row = 0; row < gameY; row++) {
                int x = (startCol + col) * cellWidth;
                int y = (yOffset + row) * cellHeight;
                enqueueWall(x, y, offsetLayer);
            }
        }
    }

    /**
     * Enqueues a marker sprite at the given board position if it is inside the current viewport.
     *
     * <p>The position is converted from board coordinates to viewport-relative
     * pixel coordinates before the marker is added to the draw queue.</p>
     *
     * @param pos board position where the marker should be drawn
     * @param srcOrder sprite index in the game atlas
     */
    private void enqueueMarker(Position pos, int srcOrder) {
        int px = pos.getX();
        int py = pos.getY();

        if (px < viewStartX || px >= viewEndX || py < viewStartY || py >= viewEndY) {
            return;
        }

        int x = (px - viewStartX + xOffset) * cellWidth;
        int y = (py - viewStartY + yOffset) * cellHeight;

        int srcSize = srcSize(srcOrder, gameSrcSize);
        RenderItem marker = RenderItem.sprite(markerLayer, x, y, cellWidth, cellHeight, SheetId.GAME_ATLAS, srcSize, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(marker);
    }

    /**
     * Enqueues cells for the current frame, including:
     * <ul>
     *   <li>Padding walls when the board is smaller than {@code minX/minY}</li>
     *   <li>A player-centered viewport when the board is larger than {@code maxX/maxY}</li>
     * </ul>
     * Updates the current viewport bounds used for character rendering,
     * and enqueues the start and end markers if they are visible in the viewport.
     *
     * @param grid the board grid of cells
     */
    private void enqueueCells(Cell[][] grid) {
        int startX = 0;
        int startY = 0;
        int endX = gameX;
        int endY = gameY;

        // Draw padding walls if the board is smaller than the minimum viewport.
        if (yOffset > 0) {
            fillHorizontalOffset(0);
            fillHorizontalOffset(gameY + yOffset);
        }
        if (xOffset > 0) {
            fillVerticalOffset(0);
            fillVerticalOffset(gameX + xOffset);
        }

        // If the board is wider than maxX, compute a horizontal viewport centered on the player.
        if (maxX < gameX) {
            startX = game.getPlayer().getPosition().getX() - maxX / 2;
            startX = Math.max(0, Math.min(startX, gameX - maxX));
            endX = startX + maxX;
        }
        // If the board is taller than maxY, compute a vertical viewport centered on the player.
        if (maxY < gameY) {
            startY = game.getPlayer().getPosition().getY() - maxY / 2;
            startY = Math.max(0, Math.min(startY, gameY - maxY));
            endY = startY + maxY;
        }
        // Save viewport bounds so character rendering uses the same world-to-viewport transform.
        viewStartX = startX;
        viewStartY = startY;
        viewEndX = endX;
        viewEndY = endY;
        enqueueGameCells(grid, startX, startY, endX, endY);
        enqueueMarker(board.getStartPosition(), 10);
        enqueueMarker(board.getEndPosition(), 10);
    }


    /**
     * Enqueues the player sprite at the given pixel position.
     * 
     * @param x pixel x
     * @param y pixel y
     */
    private void enqueuePlayer(int x, int y) {
        int srcSize = srcSize(7, gameSrcSize);
        RenderItem player = RenderItem.sprite(playerLayer, x, y, cellWidth, cellHeight, SheetId.GAME_ATLAS, srcSize, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(player);
    }

    /**
     * Enqueues the goblin sprite at the given pixel position.
     * 
     * @param x pixel x
     * @param y pixel y
     */
    private void enqueueGoblin(int x, int y) {
        int srcSize = srcSize(6, gameSrcSize);
        RenderItem goblin = RenderItem.sprite(characterLayer, x, y, cellWidth, cellHeight, SheetId.GAME_ATLAS, srcSize, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(goblin);
    }

    /**
     * Enqueues the ogre sprite at the given pixel position.
     * 
     * @param x pixel x
     * @param y pixel y
     */
    private void enqueueOgre(int x, int y) {
        int srcSize = srcSize(5, gameSrcSize);
        RenderItem ogre = RenderItem.sprite(characterLayer, x, y, cellWidth, cellHeight, SheetId.GAME_ATLAS, srcSize, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(ogre);
    }

    /**
     * Enqueues all characters visible in the current viewport.
     * <p>
     * Converts world (board) coordinates into viewport-relative pixel coordinates,
     * and skips characters outside the visible region.
     *
     * @param characters list of active characters
     */
    private void enqueueCharacters(List<GameCharacter> characters) {
        for (GameCharacter character : characters) {
            int cx = character.getPosition().getX();
            int cy = character.getPosition().getY();

            // Skip characters outside the visible viewport
            if (cx < viewStartX || cx >= viewEndX || cy < viewStartY || cy >= viewEndY) {
                continue;
            }

            // World-to-viewport conversion (same idea as enqueueGameCells)
            int x = (cx - viewStartX + xOffset) * cellWidth;
            int y = (cy - viewStartY + yOffset) * cellHeight;
            
            if (character instanceof Player) {
                enqueuePlayer(x, y);
            } else if (character instanceof Goblin) {
                enqueueGoblin(x, y);
            } else if (character instanceof Ogre) {
                enqueueOgre(x, y);
            }
        }
    }


    /**
     * Enqueues the start screen UI (title and prompt).
     */
    private void enqueueStartScreen() {
        String titleText = "Dungeon Crawl";
        Font titleFont = new Font("SansSerif", Font.BOLD, 50);
        int[] titleXY = getCenteredTextXY(titleText, titleFont);
        
        RenderItem title = RenderItem.text(0, titleXY[0], titleXY[1], Color.WHITE, titleText, titleFont);
        drawQueue.enqueue(title);

        String startText = "Press Space to Start";
        Font startFont = new Font("SansSerif", Font.BOLD, 24);
        int textPadding = 20;
        int startX = getCenteredTextX(startText, startFont);
        int startY = titleXY[1] + textPadding + getTextHeight(startFont);

        RenderItem start = RenderItem.text(0, startX, startY, Color.WHITE, startText, startFont);
        drawQueue.enqueue(start);
    }

    /**
     * Enqueues the end screen UI, including the image, result text, comment,
     * final score/time, and replay prompt.
     *
     * @param score final score
     * @param endReason reason the game ended
     * @param sec elapsed seconds for the completed run
     */
    private void enqueueEndScreen(int score, EndReason endReason, int sec) {
        int srcSize = srcSize(2, screenSrcSize);
        int w = cellWidth * 4;
        int h = cellHeight * 4;
        int[] imageXY = getCenteredRectXY(w, h);

        String resultText = "GAME OVER";
        String commentText = "";

        switch (endReason) {
            case WIN:
                srcSize = srcSize(1, screenSrcSize);
                resultText = "YOU WIN";
                commentText = "You braved the terrors of the dungeon and emerged a rich man"; 
                break;
            
            case LOSE_BY_TRAP:
                srcSize = srcSize(2, screenSrcSize);
                commentText = "Lost your footing near a pit of spikes!";
                break;

            case LOSE_BY_OGRE:
                srcSize = srcSize(5, screenSrcSize);
                commentText = "Stuck, mashed, and boiled into a stew!";
                break;
            
            case LOSE_BY_GOBLIN:
                srcSize = srcSize(3, screenSrcSize);
                commentText = "Caught by a goblin!";
                break;
        }

        int imageY = imageXY[1] - 60;
        RenderItem endImage = RenderItem.sprite(0, imageXY[0], imageY, w, h, SheetId.SCREEN_ATLAS, srcSize, srcPadding, screenSrcSize, screenSrcSize);
        drawQueue.enqueue(endImage);

        //result
        int textPadding = 10;
        Font resultFont = new Font("SansSerif", Font.BOLD, 40);
        int resultX = getCenteredTextX(resultText, resultFont);
        int resultY = imageY + h + textPadding + getTextHeight(resultFont)/2 + 10;
        
        RenderItem result = RenderItem.text(0, resultX, resultY, Color.WHITE, resultText, resultFont);
        drawQueue.enqueue(result);

        //comment
        Font commentFont = new Font("SansSerif", Font.PLAIN, 17);
        int commentX = getCenteredTextX(commentText, commentFont);
        int commentY = resultY + textPadding-5 + getTextHeight(commentFont);
        
        RenderItem comment = RenderItem.text(0, commentX, commentY, Color.WHITE, commentText, commentFont);
        drawQueue.enqueue(comment);

        //finalScore/Time
        String scoreTimeText = "Final Score: " + score + "     Final Time: " + String.format("%d:%02d", sec / 60, sec % 60);
        int scoreTimeX = getCenteredTextX(scoreTimeText, commentFont);
        int scoreTimeY = commentY + getTextHeight(commentFont);
        
        RenderItem scoreTime = RenderItem.text(0, scoreTimeX, scoreTimeY+2, Color.WHITE, scoreTimeText, commentFont);
        drawQueue.enqueue(scoreTime);

        //playAgain
        String playAgainText = "Press Space to Play Again";
        Font playAgainFont = new Font("SansSerif", Font.BOLD, 15);
        int playAgainX = getCenteredTextX(playAgainText, playAgainFont);
        int playAgainY = scoreTimeY + textPadding + getTextHeight(playAgainFont) + 7;
        
        RenderItem playAgain = RenderItem.text(0, playAgainX, playAgainY, Color.WHITE, playAgainText, playAgainFont);
        drawQueue.enqueue(playAgain);
    }


    /**
     * Computes aligned positions for HUD text and images.
     * 
     * @param font font used for the text measurements
     * @param keyText key counter text
     * @param coinText score text
     * @param imageH icon height
     * @param textX base x for text
     * @param centerY vertical center line for the HUD row
     * @param padding padding between text and icon
     * @return {imageX, textY, imageY}
     */
    private int[] hudGetXY(Font font, String keyText, String coinText, int imageH, int textX, int centerY, int padding) {
        //imageX
        int keyW = getTextWidth(keyText, font);
        int coinW = getTextWidth(coinText, font);

        int longerW = (keyW >= coinW) ? keyW : coinW;
        int imageX = textX + longerW + padding;
        
        //y
        FontMetrics fm = getFontMetrics(font);
        int textY = centerY + (fm.getAscent() - fm.getDescent())/2;

        int imageY = centerY - imageH/2;

        return new int[]{imageX, textY, imageY};
    }

    /**
     * Enqueues the HUD elements: timer, keys collected, and score.
     * 
     * @param score current score
     * @param totalKey total keys available
     * @param collectedKey keys collected so far
     * @param sec elapsed seconds
     */
    private void enqueueHud(int score, int totalKey, int collectedKey, int sec) {
        //Key/score
        Font font = new Font("SansSerif", Font.BOLD, 15);
        String keyText = collectedKey + " / " + totalKey;
        String coinText = Integer.toString(score);

        int w = 20;
        int h = 20;
        int textX = 15;
        int centerY = 30;
        int padding = 5;

        // Note: Alternative positions to swap the image and text (left/right):
        // int imageX = 10;
        // int textX = imageX + w;
        // Note: In that case, change the timer's x position from textX to imageX. 

        int[] imageXTextYImageY = hudGetXY(font, keyText, coinText, h, textX, centerY, padding);
        int imageX = imageXTextYImageY[0];
        int textY = imageXTextYImageY[1];
        int imageY = imageXTextYImageY[2];


        //key text      
        RenderItem keyItem = RenderItem.text(hudLayer, textX, textY + h, Color.WHITE, keyText, font);
        drawQueue.enqueue(keyItem);
        //image
        int srcSizeKey = srcSize(8, gameSrcSize);
        RenderItem keyImage = RenderItem.sprite(hudLayer, imageX, imageY + h, w, h, SheetId.GAME_ATLAS, srcSizeKey, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(keyImage);

        //score text        
        RenderItem coinItem = RenderItem.text(hudLayer, textX, textY + h*2, Color.WHITE, coinText, font);
        drawQueue.enqueue(coinItem);
        //image
        int srcSizeCoin = srcSize(9, gameSrcSize);
        RenderItem coinImage = RenderItem.sprite(hudLayer, imageX, imageY + h*2, w, h, SheetId.GAME_ATLAS, srcSizeCoin, srcPadding, gameSrcSize, gameSrcSize);
        drawQueue.enqueue(coinImage);

        //timer
        Font timerFont = new Font("SansSerif", Font.BOLD, 20);
        String timeText = String.format("%d:%02d", sec / 60, sec % 60);
        RenderItem timer = RenderItem.text(hudLayer, textX, textY - 3, Color.WHITE, timeText, timerFont);
        drawQueue.enqueue(timer);
    }

    /**
     * Enqueues the popup overlay for the given reason (image, text box, and prompt).
     *
     * @param popupReason the reason the popup is being shown
     */
    private void enqueuePopups(PopupReason popupReason) {
        int imageW = cellWidth * 4;
        int imageH = cellHeight * 4;

        int[] imageXY = getCenteredRectXY(imageW, imageH);
        int imageX = imageXY[0];
        int imageY = imageXY[1] - 40;
        int imagePadding = 5;

        int textBoxW = cellWidth * 7;
        int textBoxH = cellHeight;
        int[] textBoxXY = getCenteredRectXY(textBoxW, textBoxH);
        int textBotX = textBoxXY[0];
        int textBoxY = textBoxXY[1] + imageH/2 + 15;
        int textXPadding = 8;

        String commentText = "";
        int srcSize = srcSize(1, screenSrcSize);

        RenderItem imageRect = RenderItem.rect(popupRectLayer, imageX - imagePadding, imageY - imagePadding, imageW + imagePadding*2, imageH + imagePadding*2, Color.BLACK);
        drawQueue.enqueue(imageRect);
        RenderItem textRect = RenderItem.rect(popupRectLayer, textBotX, textBoxY, textBoxW, textBoxH, Color.BLACK);
        drawQueue.enqueue(textRect);

        switch (popupReason) {
            case OGRE_HIT:
                srcSize = srcSize(0, screenSrcSize);
                commentText = "* An ogre emerges from the darkness.";
                break;
        
            case BONUS_COLLECTED:
                srcSize = srcSize(1, screenSrcSize);
                commentText = "* You found a treasure chest!";
                break;

			case KEY_COLLECTED:
				srcSize = srcSize(4, screenSrcSize);
				commentText = "* After some 'convincing', the gnome gives up his key.";
				break;
        }
        
        RenderItem popupImage = RenderItem.sprite(popupContentsLayer, imageX, imageY, imageW, imageH, SheetId.SCREEN_ATLAS, srcSize, srcPadding, screenSrcSize, screenSrcSize);
        drawQueue.enqueue(popupImage);

        Font commentFont = new Font("SansSerif", Font.PLAIN, 11);
        int commentH = getTextHeight(commentFont);
        
        RenderItem comment = RenderItem.text(popupContentsLayer, textBotX + textXPadding, textBoxY + commentH, Color.WHITE, commentText, commentFont);
        drawQueue.enqueue(comment);


        String operationText = "Press space to continue...";
        Font operationFont = new Font("SansSerif", Font.PLAIN, 11);
        int operationX = getCenteredTextX(operationText, operationFont);
        
        RenderItem operation = RenderItem.text(popupContentsLayer, operationX, textBoxY + textBoxH - 10, Color.WHITE, operationText, operationFont);
        drawQueue.enqueue(operation);
    }

    /**
     * Enqueues the pause overlay shown when the game is paused without an active popup.
     *
     * <p>This includes a centered background rectangle, a pause title,
     * and a prompt explaining how to resume play.</p>
     */
    private void enqueuePause() {
        int pauseRectW = cellWidth * 6;
        int pauseRextH = cellHeight * 3;
        int[] pauseRectXY = getCenteredRectXY(pauseRectW, pauseRextH);
        int pauseRectX = pauseRectXY[0];
        int pauseRectY = pauseRectXY[1];

        RenderItem pauseRect = RenderItem.rect(popupRectLayer, pauseRectX, pauseRectY, pauseRectW, pauseRextH, Color.BLACK);
        drawQueue.enqueue(pauseRect);


        String pauseText = "Game Paused";
        Font pauseFont = new Font("SansSerif", Font.BOLD, 30);
        int[] pauseXY = getCenteredTextXY(pauseText, pauseFont);
        int pauseY = pauseXY[1] - 15;
        RenderItem pause = RenderItem.text(popupContentsLayer, pauseXY[0], pauseY, Color.WHITE, pauseText, pauseFont);
        drawQueue.enqueue(pause);

        String operationText = "Press space to continue";
        Font operationFont = new Font("SansSerif", Font.PLAIN, 17);
        int operationX = getCenteredTextX(operationText, operationFont);
        int operationY = pauseY + getTextHeight(operationFont) + 10;
        RenderItem operation = RenderItem.text(popupContentsLayer, operationX, operationY, Color.WHITE, operationText, operationFont);
        drawQueue.enqueue(operation);
    }


    /**
     * Rebuilds the draw queue for the current frame based on the current {@link ScreenState}.
     *
     * @param game the game model
     * @param board the board model
     */
    private void buildDrawQueue(Game game, Board board) {
        drawQueue.clear();

        ScreenState screenState = game.getScreenState();
        int sec = game.getSeconds();
        int score = game.getDisplayedScore();
        int totalKey = game.getTotalRegularRewards();
        int collectedKey = game.getCollectedRegularRewards();
        PopupReason popupReason = game.getPopupReason();

        switch (screenState) {
            case START:
                enqueueStartScreen();
                break;

            case PLAYING:
                enqueueCells(board.getGrid());
                enqueueCharacters(game.getCharacters());
                enqueueHud(score, totalKey, collectedKey, sec);
                break;
            
            case PAUSE:
                enqueueCells(board.getGrid());
                enqueueCharacters(game.getCharacters());
                enqueueHud(score, totalKey, collectedKey, sec);
                if (popupReason != null) {
                    enqueuePopups(popupReason);
                } else {
                    enqueuePause();
                }
                break;
                
            case END:
                enqueueEndScreen(score, game.getEndReason(), sec);
                break;
        }
    }
    

    /**
     * Paints the panel by rebuilding the draw queue and rendering all items.
     * 
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        buildDrawQueue(game, board);
        drawQueue.renderAll(g);
    }

    /**
     * Rebuilds the draw queue for the current state and returns the render items.
     *
     * <p>This method is intended for tests that need to verify what would be
     * rendered without calling {@link #paintComponent(Graphics)}.</p>
     *
     * @return the render items generated for the current frame
     */
    public List<RenderItem> buildRenderItemsForTest() {
        buildDrawQueue(game, board);
        return drawQueue.getRenderItems();
    }

    /**
     * Returns the viewport/render width in cells.
     *
     * @return the render width in cells
     */
    public int getRenderXForTest() {
        return renderX;
    }
    
    /**
     * Returns the viewport/render height in cells.
     *
     * @return the render height in cells
     */
    public int getRenderYForTest() {
        return renderY;
    }

    /**
     * Returns the horizontal padding applied on each side when the board width
     * is smaller than the minimum viewport width.
     *
     * @return the horizontal padding in cells
     */
    public int getXOffsetForTest() {
        return xOffset;
    }

    /**
     * Returns the vertical padding applied on each side when the board height
     * is smaller than the minimum viewport height.
     *
     * @return the vertical padding in cells
     */
    public int getYOffsetForTest() {
        return yOffset;
    }
    
    /**
     * Returns the current viewport left bound in board coordinates (inclusive).
     *
     * @return the viewport start x-coordinate
     */
    public int getViewStartXForTest() {
        return viewStartX;
    }

    /**
     * Returns the current viewport top bound in board coordinates (inclusive).
     *
     * @return the viewport start y-coordinate
     */
    public int getViewStartYForTest() {
        return viewStartY;
    }

    /**
     * Returns the current viewport right bound in board coordinates (exclusive).
     *
     * @return the viewport end x-coordinate
     */
    public int getViewEndXForTest() {
        return viewEndX;
    }

    /**
     * Returns the current viewport bottom bound in board coordinates (exclusive).
     *
     * @return the viewport end y-coordinate
     */
    public int getViewEndYForTest() {
        return viewEndY;
    }
}
