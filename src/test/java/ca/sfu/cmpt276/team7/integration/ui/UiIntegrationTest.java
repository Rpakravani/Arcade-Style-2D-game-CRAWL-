package ca.sfu.cmpt276.team7.integration.ui; 

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import ca.sfu.cmpt276.team7.EndReason;
import ca.sfu.cmpt276.team7.Game;
import ca.sfu.cmpt276.team7.PopupReason;
import ca.sfu.cmpt276.team7.ScreenState;
import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.BonusReward;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.ui.GamePanel;
import ca.sfu.cmpt276.team7.ui.RenderItem;
import ca.sfu.cmpt276.team7.unit.ui.UiTestSupport;

/**
 * Score / Time -> UI Update Flow
 * Chest Popup Gameplay Flow
 * End Screen Message -> Replay UI Flow
 */
public class UiIntegrationTest {
    @Test
    void scoreAndTimeChanges_areReflectedInHud() {
        Board board = UiTestSupport.makeSimpleBoard(11, 10);
        Player player = new Player(board, board.getStartPosition());
        UiTestSupport.FakeClock clock = new UiTestSupport.FakeClock();

        Game game = new Game(board, player, new ArrayList<>(), 0, 0, List.of(), List.of(), clock);

        game.startGame();
        player.setScore(55);
        clock.advanceMs(63_000);

        GamePanel panel = new GamePanel(game, board);
        panel.setSize(panel.getPreferredSize());

        List<RenderItem> items = panel.buildRenderItemsForTest();
        List<String> texts = UiTestSupport.getOnlyTexts(items);

        // Verify that the HUD shows the updated score, elapsed time, and reward progress.
        assertTrue(texts.contains("55"));
        assertTrue(texts.contains("1:03"));
        assertTrue(texts.contains("0 / 0"));
    }

    @Test
    void collectingChest_showsPopup_thenResumeContinuesPlay() {
        Board board = UiTestSupport.makeSimpleBoard(11, 10);

        Position chestPos = new Position(1, 1);
        board.setCell(chestPos.getX(), chestPos.getY(), new RewardCell(chestPos, new BonusReward(25, 20)));

        Player player = new Player(board, board.getStartPosition());
        Game game = new Game(board, player, new ArrayList<>(), 0, 1, List.of(), List.of());

        game.startGame();
        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Check that collecting a chest pauses the game and opens the bonus popup.
        assertEquals(ScreenState.PAUSE, game.getScreenState());
        assertEquals(PopupReason.BONUS_COLLECTED, game.getPopupReason());
        assertEquals(new Position(1, 1), player.getPosition());
        assertEquals(1, game.getCollectedBonusRewards());

        GamePanel panel = new GamePanel(game, board);
        panel.setSize(panel.getPreferredSize());

        List<RenderItem> popupItems = panel.buildRenderItemsForTest();
        List<String> popupTexts = UiTestSupport.getOnlyTexts(popupItems);

        // Verify that the popup UI shows the chest message and resume instruction.
        assertTrue(popupTexts.contains("* You found a treasure chest!"));
        assertTrue(popupTexts.contains("Press space to continue..."));

        game.handleInput(KeyEvent.VK_SPACE);

        // Ensure that pressing space closes the popup and returns the game to playing state.
        assertEquals(ScreenState.PLAYING, game.getScreenState());
        assertNull(game.getPopupReason());

        game.handleInput(KeyEvent.VK_RIGHT);
        game.updateTick();

        // Confirm that the player can continue moving after the popup is dismissed.
        assertEquals(new Position(2, 1), player.getPosition());

        List<RenderItem> resumedItems = panel.buildRenderItemsForTest();
        List<String> resumedTexts = UiTestSupport.getOnlyTexts(resumedItems);

        // Verify that the popup text is no longer rendered after resuming play.
        assertFalse(resumedTexts.contains("* You found a treasure chest!"));
        assertFalse(resumedTexts.contains("Press space to continue..."));
    }

    @Test
    void endScreen_replayFlow_returnsToPlayingUi() {
        Board board = UiTestSupport.makeSimpleBoard(11, 10);
        Player player = new Player(board, board.getStartPosition());
        Game game = new Game(board, player, new ArrayList<>(), 0, 0, List.of(), List.of());

        game.startGame();
        player.setScore(-1);
        game.checkLoss();

        // Check that the game enters the end screen with the correct losing reason.
        assertEquals(ScreenState.END, game.getScreenState());
        assertEquals(EndReason.LOSE_BY_TRAP, game.getEndReason());

        GamePanel panel = new GamePanel(game, board);
        panel.setSize(panel.getPreferredSize());

        List<RenderItem> endItems = panel.buildRenderItemsForTest();
        List<String> endTexts = UiTestSupport.getOnlyTexts(endItems);

        // Verify that the replay instruction is displayed on the end screen.
        assertTrue(endTexts.contains("Press Space to Play Again"));
        
        game.handleInput(KeyEvent.VK_SPACE);

        // Ensure that replay resets the game state and returns to normal play.
        assertEquals(ScreenState.PLAYING, game.getScreenState());
        assertNull(game.getEndReason());
        assertNull(game.getPopupReason());
        assertEquals(board.getStartPosition(), player.getPosition());
        assertEquals(0, player.getTotalScore());

        List<RenderItem> replayItems = panel.buildRenderItemsForTest();
        List<String> replayTexts = UiTestSupport.getOnlyTexts(replayItems);

        // Verify that the end screen text disappears and the HUD is shown again after replay.
        assertFalse(replayTexts.contains("Press Space to Play Again"));
        assertTrue(replayTexts.contains("0:00"));
        assertTrue(replayTexts.contains("0 / 0"));
    }
}