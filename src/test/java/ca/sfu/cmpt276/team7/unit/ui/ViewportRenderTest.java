package ca.sfu.cmpt276.team7.unit.ui;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import ca.sfu.cmpt276.team7.Game;
import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.ui.GamePanel;
import ca.sfu.cmpt276.team7.ui.RenderItem;

/**
 * Small Map Padding Display Rule
 * Large Map Viewport Display Rule
 */
public class ViewportRenderTest {
    @Test
    void smallMap_addsPaddingWallsAndUsesMinimumViewport() {
        Board board = UiTestSupport.makeSimpleBoard(5, 5);
        Player player = new Player(board, board.getStartPosition());
        Game game = new Game(board, player, new ArrayList<>(), 0, 0, List.of(), List.of());

        game.startGame();

        GamePanel panel = new GamePanel(game, board);
        panel.setSize(panel.getPreferredSize());

        panel.buildRenderItemsForTest();

        // Verify that small maps use the minimum render area with padding applied.
        assertEquals(11, panel.getRenderXForTest());
        assertEquals(9, panel.getRenderYForTest());

        // Check that the board is centered with positive offsets inside the viewport.
        assertTrue(panel.getXOffsetForTest() > 0);
        assertTrue(panel.getYOffsetForTest() > 0);

        // Ensure that the full board remains visible when the map is smaller than the viewport.
        assertEquals(0, panel.getViewStartXForTest());
        assertEquals(0, panel.getViewStartYForTest());
        assertEquals(board.getWidth(), panel.getViewEndXForTest());
        assertEquals(board.getHeight(), panel.getViewEndYForTest());
    }

    @Test
    void smallMap_rendersTopAndBottomOffsetWalls() {
        Board board = UiTestSupport.makeSimpleBoard(5, 5);
        Player player = new Player(board, board.getStartPosition());
        Game game = new Game(board, player, new ArrayList<>(), 0, 0, List.of(), List.of());

        game.startGame();

        GamePanel panel = new GamePanel(game, board);
        panel.setSize(panel.getPreferredSize());

        List<RenderItem> items = panel.buildRenderItemsForTest();

        // Verify that a wall sprite is rendered in the top padding row.
        assertTrue(UiTestSupport.containsSpriteAt(items, UiTestSupport.wallSprite, 0, 0));

        // Verify that a wall sprite is also rendered in the bottom padding row.
        int bottomY = (panel.getRenderYForTest() - 1);
        assertTrue(UiTestSupport.containsSpriteAt(items, UiTestSupport.wallSprite, 0, bottomY));
    }

    @Test
    void largeMap_usesPlayerCenteredViewport() {
        Board board = UiTestSupport.makeSimpleBoard(31, 31);
        Player player = new Player(board, board.getStartPosition());
        Game game = new Game(board, player, new ArrayList<>(), 0, 0, List.of(), List.of());

        game.startGame();

        player.setPosition(new Position(15, 15));

        GamePanel panel = new GamePanel(game, board);
        panel.setSize(panel.getPreferredSize());

        panel.buildRenderItemsForTest();

        // Verify that large maps use the fixed viewport size instead of showing the whole board.
        assertEquals(21, panel.getViewEndXForTest() - panel.getViewStartXForTest());
        assertEquals(11, panel.getViewEndYForTest() - panel.getViewStartYForTest());

        // Check that the viewport is centered around the player's current position.
        assertEquals(5, panel.getViewStartXForTest());
        assertEquals(10, panel.getViewStartYForTest());
        assertEquals(26, panel.getViewEndXForTest());
        assertEquals(21, panel.getViewEndYForTest());
    }
}