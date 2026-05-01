package ca.sfu.cmpt276.team7.unit.ui;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import ca.sfu.cmpt276.team7.Game;
import ca.sfu.cmpt276.team7.PopupReason;
import ca.sfu.cmpt276.team7.ScreenState;
import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.enemies.Enemy;
import ca.sfu.cmpt276.team7.enemies.Ogre;
import ca.sfu.cmpt276.team7.reward.BonusReward;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.RegularReward;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.ui.GamePanel;
import ca.sfu.cmpt276.team7.ui.RenderItem;

/**
 * Key Popup Visibility and Input Blocking
 * Chest Popup Visibility and Input Blocking
 * Ogre Popup Visibility and Input Blocking
 * Popup Resume Control
 */
public class PopupRenderTest {
    @Test
    void keyPopup_isRenderedAndBlocksMovementUntilResume() {
        Board board = UiTestSupport.makeSimpleBoard(11, 10);

        Position keyPos = new Position(1, 1);
        board.setCell(keyPos.getX(), keyPos.getY(), new RewardCell(keyPos, new RegularReward(10)));

        Player player = new Player(board, board.getStartPosition());
        Game game = new Game(board, player, new ArrayList<>(), 1, 0, List.of(keyPos), List.of());

        game.startGame();
        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Verify that collecting the key pauses the game and shows the key popup.
        assertEquals(ScreenState.PAUSE, game.getScreenState());
        assertEquals(PopupReason.KEY_COLLECTED, game.getPopupReason());
        assertEquals(new Position(1, 1), player.getPosition());

        GamePanel panel = new GamePanel(game, board);
        panel.setSize(panel.getPreferredSize());

        List<RenderItem> items = panel.buildRenderItemsForTest();
        List<String> texts = UiTestSupport.getOnlyTexts(items);

        // Check that the popup text and resume instruction are rendered.
        assertTrue(texts.contains("* After some 'convincing', the gnome gives up his key."));
        assertTrue(texts.contains("Press space to continue..."));

        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Ensure that movement is blocked while the popup is active.
        assertEquals(new Position(1, 1), player.getPosition());
        assertEquals(ScreenState.PAUSE, game.getScreenState());

        game.handleInput(KeyEvent.VK_SPACE);

        // Verify that pressing space closes the popup and resumes gameplay.
        assertEquals(ScreenState.PLAYING, game.getScreenState());
        assertNull(game.getPopupReason());

        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Confirm that movement works again after resuming.
        assertEquals(new Position(2, 1), player.getPosition());
    }

    @Test
    void chestPopup_isRenderedAndBlocksMovementUntilResume() {
        Board board = UiTestSupport.makeSimpleBoard(11, 10);

        Position chestPos = new Position(1, 1);
        board.setCell(chestPos.getX(), chestPos.getY(), new RewardCell(chestPos, new BonusReward(25, 20)));

        Player player = new Player(board, board.getStartPosition());
        Game game = new Game(board, player, new ArrayList<>(), 0, 1, List.of(), List.of());

        game.startGame();
        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Verify that collecting the chest pauses the game and opens the bonus popup.
        assertEquals(ScreenState.PAUSE, game.getScreenState());
        assertEquals(PopupReason.BONUS_COLLECTED, game.getPopupReason());
        assertEquals(new Position(1, 1), player.getPosition());
        assertEquals(1, game.getCollectedBonusRewards());

        GamePanel panel = new GamePanel(game, board);
        panel.setSize(panel.getPreferredSize());

        List<RenderItem> items = panel.buildRenderItemsForTest();
        List<String> texts = UiTestSupport.getOnlyTexts(items);

        // Check that the chest popup text and resume instruction are displayed.
        assertTrue(texts.contains("* You found a treasure chest!"));
        assertTrue(texts.contains("Press space to continue..."));

        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Ensure that movement is blocked while the chest popup is active.
        assertEquals(new Position(1, 1), player.getPosition());
        assertEquals(ScreenState.PAUSE, game.getScreenState());

        game.handleInput(KeyEvent.VK_SPACE);

        // Verify that pressing space dismisses the popup and resumes play.
        assertEquals(ScreenState.PLAYING, game.getScreenState());
        assertNull(game.getPopupReason());

        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Confirm that the player can move again after the popup closes.
        assertEquals(new Position(2, 1), player.getPosition());
    }

    @Test
    void ogrePopup_isRenderedAndBlocksMovementUntilResume() {
        Board board = UiTestSupport.makeSimpleBoard(11, 10);
        Player player = new Player(board, board.getStartPosition());

        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Ogre(board, new Position(1, 1)));

        Game game = new Game(board, player, enemies, 0, 0, List.of(), List.of());

        game.startGame();
        player.setScore(30);
        
        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Verify that hitting the ogre pauses the game and shows the ogre popup.
        assertEquals(ScreenState.PAUSE, game.getScreenState());
        assertEquals(PopupReason.OGRE_HIT, game.getPopupReason());
        assertEquals(new Position(1, 1), player.getPosition());

        GamePanel panel = new GamePanel(game, board);
        panel.setSize(panel.getPreferredSize());

        List<RenderItem> items = panel.buildRenderItemsForTest();
        List<String> texts = UiTestSupport.getOnlyTexts(items);

        // Check that the ogre popup message and resume instruction are rendered.
        assertTrue(texts.contains("* An ogre emerges from the darkness."));
        assertTrue(texts.contains("Press space to continue..."));

        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Ensure that movement is blocked while the ogre popup is active.
        assertEquals(new Position(1, 1), player.getPosition());
        assertEquals(ScreenState.PAUSE, game.getScreenState());

        game.handleInput(KeyEvent.VK_SPACE);

        // Verify that pressing space closes the popup and returns to playing state.
        assertEquals(ScreenState.PLAYING, game.getScreenState());
        assertNull(game.getPopupReason());

        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Confirm that movement resumes normally after the popup is dismissed.
        assertEquals(new Position(2, 1), player.getPosition());
    }

    @Test
    void popup_resumeControl() {
        Board board = UiTestSupport.makeSimpleBoard(11, 10);

        Position keyPos = new Position(1, 1);
        board.setCell(keyPos.getX(), keyPos.getY(), new RewardCell(keyPos, new RegularReward(10)));

        Player player = new Player(board, board.getStartPosition());
        Game game = new Game(board, player, new ArrayList<>(), 1, 0, List.of(keyPos), List.of());

        game.startGame();
        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Verify that the key popup is active after the reward is collected.
        assertEquals(ScreenState.PAUSE, game.getScreenState());
        assertEquals(PopupReason.KEY_COLLECTED, game.getPopupReason());
        assertEquals(new Position(1, 1), player.getPosition());

        game.handleInput(KeyEvent.VK_RIGHT);

        // Ensure that non-resume input does not close the popup or move the player.
        assertEquals(ScreenState.PAUSE, game.getScreenState());
        assertEquals(PopupReason.KEY_COLLECTED, game.getPopupReason());
        assertEquals(new Position(1, 1), player.getPosition());

        game.handleInput(KeyEvent.VK_SPACE);

        // Verify that the resume key returns the game to normal play.
        assertEquals(ScreenState.PLAYING, game.getScreenState());
        assertNull(game.getPopupReason());

        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Confirm that movement works again after resuming.
        assertEquals(new Position(2, 1), player.getPosition());
    }
}