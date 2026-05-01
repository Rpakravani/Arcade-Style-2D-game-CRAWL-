package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.RegularReward;
import ca.sfu.cmpt276.team7.Game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for verifying the Remaining Keys State rule.
 *
 * <p>This rule ensures that the game correctly tracks how many regular
 * rewards (keys) the player has collected. The game should increment
 * the collected key count when the player steps on a {@link RewardCell}
 * containing a {@link RegularReward}, and the total should match the
 * number of keys placed on the board.</p>
 *
 * <p>This test constructs a minimal board, places two keys, simulates
 * player movement using {@link Game#handleInput(int)}, and verifies that
 * the game state updates as expected after each collection.</p>
 */
public class RemainingKeysStateTest {

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
     * Tests that the game correctly updates the number of collected keys.
     *
     * <p>The test places two regular rewards on the board, moves the player
     * onto each reward tile, and verifies that:</p>
     *
     * <ul>
     *     <li>The collected key count increases after each pickup.</li>
     *     <li>The total number of keys remains constant.</li>
     *     <liThe game resumes properly after the key-collection popup.</li>
     * </ul>
     *
     * <p>This ensures that the Remaining Keys State rule behaves correctly
     * throughout normal gameplay.</p>
*/
    @Test
    void testRemainingKeysUpdateCorrectly() {
        Board board = makeEmptyBoard(5, 5);

        Position key1 = new Position(2, 1);
        Position key2 = new Position(3, 2);

        board.setCell(2, 1, new RewardCell(key1, new RegularReward(10)));
        board.setCell(3, 2, new RewardCell(key2, new RegularReward(10)));

        Player player = new Player(board, new Position(2, 2));

        Game game = new Game(
                board,
                player,
                java.util.List.of(),
                2,
                0,
                java.util.List.of(key1, key2),
                java.util.List.of()
        );

        board.setStartPosition(new Position(2, 2));
        game.startGame();

        assertEquals(0, game.getCollectedRegularRewards());
        assertEquals(2, game.getTotalRegularRewards());

        // Collect first key
        game.handleInput(87); // W = NORTH
        game.updateTick();

        assertEquals(1, game.getCollectedRegularRewards(),
        "Collected keys should increase after picking up a key");
        game.handleInput(32); // SPACE to resume from popup

        // Move EAST then SOUTH to reach second key
        game.handleInput(68); // D = EAST
        game.updateTick();

        game.handleInput(83); // S = SOUTH
        game.updateTick();

        assertEquals(2, game.getCollectedRegularRewards(),
            "Collected keys should reach total after picking up all keys");

    }
}
