package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.BonusReward;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.Game;
import ca.sfu.cmpt276.team7.PopupReason;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests the collection rule for temporary bonus rewards.
 *
 * <p>When the player steps onto a {@link BonusReward}, the game must:</p>
 * <ul>
 *     <li>increment the collected bonus reward counter,</li>
 *     <li>remove the reward from the board (restore a {@link FloorCell}),</li>
 *     <li>remove the reward from the active bonus reward list,</li>
 *     <li>pause the game and show a {@link PopupReason#BONUS_COLLECTED} popup.</li>
 * </ul>
 *
 * <p>This test verifies that all of these behaviors occur correctly.</p>
 */
public class BonusRewardCollectionTest {

    /** Creates a simple board filled with FloorCells. */
    private Board makeEmptyBoard(int w, int h) {
        Cell[][] grid = new Cell[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                grid[y][x] = new FloorCell(new Position(x, y));
            }
        }
        return new Board(grid);
    }

    @Test
    void testBonusRewardCollection() {
        Board board = makeEmptyBoard(5, 5);

        Position start = new Position(2, 2);
        board.setStartPosition(start);

        // Place a bonus reward directly north of the player
        Position bonusPos = new Position(2, 1);
        BonusReward bonus = new BonusReward(25, 20);
        board.setCell(2, 1, new RewardCell(bonusPos, bonus));

        Player player = new Player(board, start);

        Game game = new Game(
                board,
                player,
                List.of(),          // no enemies
                0,                  // no regular rewards
                0,                  // no bonus rewards yet
                List.of(),          // no keys
                List.of()           // no traps
        );

        game.startGame();

        assertEquals(0, game.getCollectedBonusRewards(),
                "Initially, no bonus rewards should be collected");

        // Move NORTH onto the bonus reward
        game.handleInput(87); // W
        game.updateTick();

        // After collection, the game should pause with BONUS_COLLECTED popup
        assertEquals(PopupReason.BONUS_COLLECTED, game.getPopupReason(),
                "Collecting a bonus reward should trigger a BONUS_COLLECTED popup");

        assertEquals(1, game.getCollectedBonusRewards(),
                "Collected bonus reward counter should increment");

        // The reward should be removed from active list
        assertTrue(game.getBonusRewards().isEmpty(),
                "Active bonus reward list should be empty after collection");

        // The board tile should revert to FloorCell
        Cell cell = board.getCell(bonusPos.getX(), bonusPos.getY());
        assertTrue(cell instanceof FloorCell,
                "Bonus reward tile should revert to FloorCell after collection");
    }
}
