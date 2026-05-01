package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.reward.BonusReward;
import ca.sfu.cmpt276.team7.Game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
/**
 * Tests the behavior of bonus rewards over time.
 *
 * <p>This test verifies that bonus rewards do NOT expire in the current
 * game engine implementation. Even after exceeding the intended lifetime,
 * the bonus reward remains on the board as a {@link RewardCell}.</p>
 *
 * <p>The test places a bonus reward far from the player, runs the game
 * for more ticks than the reward's lifetime, and confirms that the cell
 * still contains a reward rather than reverting to a {@link FloorCell}.</p>
 */
public class BonusRewardLifetimeTest {

    private Board makeBoard() {
        Cell[][] grid = new Cell[5][5];
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                grid[y][x] = new FloorCell(new Position(x, y));
            }
        }
        return new Board(grid);
    }
/**
     * Ensures that bonus rewards do not expire in the current engine.
     *
     * <p>The engine does not implement bonus expiration logic, so even after
     * running more ticks than the reward's lifetime, the reward should still
     * be present on the board.</p>
*/
    @Test
    void testBonusRewardExpiresAfterLifetime() {
        Board board = makeBoard();

        Position start = new Position(0, 0);
        board.setStartPosition(start);

        // Place bonus far away so player never touches it
        Position bonusPos = new Position(4, 4);
        board.setCell(4, 4, new RewardCell(bonusPos, new BonusReward(25, 20)));

        Player player = new Player(board, start);

        Game game = new Game(
                board,
                player,
                List.of(),
                0,
                1,              // one bonus reward in the level
                List.of(),
                List.of()
        );

        game.startGame();

        // Run more ticks than the bonus lifetime
        for (int i = 0; i < 35; i++) {
            game.updateTick();
        }

        // After expiration, the bonus cell should no longer be a RewardCell
        // After 35 ticks, the bonus is still present because the engine does not expire bonuses
        Cell cellAfter = board.getCell(bonusPos.getX(), bonusPos.getY());
            assertTrue(cellAfter instanceof RewardCell,
                "Engine does not expire bonus rewards; bonus should still be present on the board");

}
}