package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Direction;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.Reward;
import ca.sfu.cmpt276.team7.reward.RegularReward;
import ca.sfu.cmpt276.team7.reward.RewardCell;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Phase 3 requirement: Key Collection Rule.
 *
 * <p>When the player moves onto a {@link RewardCell} containing a
 * {@link RegularReward}, the following must occur:</p>
 *
 * <ul>
 *     <li>The reward is returned by {@code Player.move()}.</li>
 *     <li>The player's score increases by the reward's value.</li>
 *     <li>The reward cell is replaced with a {@link FloorCell}.</li>
 *     <li>The player's position updates correctly.</li>
 * </ul>
 */
public class KeyCollectionTest {

    /**
     * Creates a rectangular board filled with {@link FloorCell} instances.
     *
     * @param w width of the board (columns)
     * @param h height of the board (rows)
     * @return a fully initialized {@link Board}
     */
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
     * Tests that the player correctly collects a {@link RegularReward}
     * when stepping onto a {@link RewardCell}.
     */
    @Test
    void testPlayerCollectsRegularReward() {
        Board board = makeEmptyBoard(5, 5);
        Player player = new Player(board, new Position(2, 2));

        // Create a reward worth 10 points
        RegularReward reward = new RegularReward(10);

        // Place reward cell north of the player
        RewardCell rewardCell = new RewardCell(new Position(2, 1), reward);
        board.setCell(2, 1, rewardCell);

        // Move player into reward cell
        Reward collected = player.move(Direction.NORTH);

        // 1. The returned reward must be the same object
        assertNotNull(collected, "A reward should be returned when collected");
        assertEquals(reward, collected, "The returned reward must match the placed reward");

        // 2. Player score must increase
        assertEquals(10, player.getTotalScore(), "Player score should increase by reward value");

        // 3. The reward cell must be replaced with a FloorCell
        Cell newCell = board.getCell(2, 1);
        assertTrue(newCell instanceof FloorCell, "RewardCell should be replaced with FloorCell after collection");

        // 4. Player position must update correctly
        assertEquals(2, player.getPosition().getX());
        assertEquals(1, player.getPosition().getY());
    }
}
