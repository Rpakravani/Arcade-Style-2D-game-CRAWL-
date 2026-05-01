package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.PunishmentCell;
import ca.sfu.cmpt276.team7.reward.TrapPunishment;
import ca.sfu.cmpt276.team7.Game;
import ca.sfu.cmpt276.team7.EndReason;
import ca.sfu.cmpt276.team7.ScreenState;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
/**
 * Integration test verifying the game-over logic when the player steps onto
 * a PunishmentCell that reduces their score below zero.
 *
 * <p>This test ensures that:
 * <ul>
 *     <li>A TrapPunishment with a sufficiently large penalty correctly drops the player's score below zero.</li>
 *     <li>The game transitions immediately into the END screen state.</li>
 *     <li>The end reason is reported as LOSE_BY_TRAP, matching trap‑based defeat conditions.</li>
 *     <li>No other game elements (enemies, rewards, traps list) interfere with the punishment logic.</li>
 * </ul>
 *
 * <p>Scenario:
 * <ol>
 *     <li>Player starts at (2,2) with score 0.</li>
 *     <li>A TrapPunishment worth 999 points is placed at (2,1).</li>
 *     <li>Player moves north onto the punishment.</li>
 *     <li>Score becomes negative, triggering immediate game over.</li>
 * </ol>
 */
public class PunishmentResultTest {

    private Board makeEmptyBoard(int w, int h) {
        Cell[][] grid = new Cell[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                grid[y][x] = new FloorCell(new Position(x, y));
            }
        }
        return new Board(grid);
    }
/**
     * Tests that stepping on a punishment with a value large enough to drop
     * the player's score below zero results in an immediate game over.
     *
     * <p>Validates:
     * <ul>
     *     <li>The game enters the END state after the punishment is applied.</li>
     *     <li>The end reason is LOSE_BY_TRAP, indicating trap‑based defeat.</li>
     * </ul>
*/
    @Test
    void testPunishmentCausesGameOverWhenScoreBelowZero() {
        Board board = makeEmptyBoard(5, 5);

        Position start = new Position(2, 2);
        board.setStartPosition(start);

        // Place a punishment north of the player
        Position punishPos = new Position(2, 1);
        TrapPunishment punishment = new TrapPunishment(999); // guaranteed to drop score below 0
        board.setCell(2, 1, new PunishmentCell(punishPos, punishment));

        Player player = new Player(board, start);

        Game game = new Game(
                board,
                player,
                List.of(),      // no enemies
                0,              // no regular rewards
                0,              // no bonus rewards
                List.of(),      // no keys
                List.of()       // no traps list needed
        );

        game.startGame();

        // Move player onto punishment
        game.handleInput(87); // W
        game.updateTick();

        // Game must end
        assertEquals(ScreenState.END, game.getScreenState(),
                "Game should enter END state when punishment reduces score below zero");

        // End reason must be LOSE_BY_TRAP
        assertEquals(EndReason.LOSE_BY_TRAP, game.getEndReason(),
                "End reason should be LOSE_BY_TRAP when punishment kills the player");
    }
}
