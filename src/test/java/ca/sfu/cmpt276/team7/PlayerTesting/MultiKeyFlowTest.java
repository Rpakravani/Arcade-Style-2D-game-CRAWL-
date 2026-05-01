package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.reward.RegularReward;
import ca.sfu.cmpt276.team7.Game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
/**
 * Integration test verifying correct behavior when the player collects
 * multiple key-type RegularRewards in sequence.
 *
 * <p>This test ensures that:
 * <ul>
 *     <li>Each key (represented as a RegularReward with value 0) is collected properly.</li>
 *     <li>The game increments the regular reward counter after each collection.</li>
 *     <li>A popup is triggered after each key, requiring explicit resume via SPACE.</li>
 *     <li>Collected key cells are converted back into FloorCell instances.</li>
 *     <li>Movement and reward logic remain consistent across consecutive pickups.</li>
 * </ul>
 * 
 * The scenario:
 * <ol>
 *     <li>Player starts at (2,2).</li>
 *     <li>Two keys are placed at (3,2) and (4,2).</li>
 *     <li>Player moves right twice, collecting both keys.</li>
 *     <li>After each collection, the popup is resumed using SPACE.</li>
 * </ol>
 */
public class MultiKeyFlowTest {

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
     * Tests the full flow of collecting two consecutive key rewards.
     *
     * <p>Steps validated:
     * <ol>
     *     <li>Player moves right and collects the first key.</li>
     *     <li>Popup appears and is resumed with SPACE.</li>
     *     <li>Player moves right again and collects the second key.</li>
     *     <li>Both key cells are confirmed to be replaced with FloorCell.</li>
     *     <li>The game's collected regular reward counter reaches 2.</li>
     * </ol>
*/
    @Test
    void testMultiKeyCollectionFlow() {
        Board board = makeBoard();

        Position start = new Position(2, 2);
        board.setStartPosition(start);

        Position key1 = new Position(3, 2);
        Position key2 = new Position(4, 2);

        board.setCell(3, 2, new RewardCell(key1, new RegularReward(0)));
        board.setCell(4, 2, new RewardCell(key2, new RegularReward(0)));

        Player player = new Player(board, start);

        Game game = new Game(
                board,
                player,
                List.of(),
                2,      // totalRegularRewards = 2 keys
                0,
                List.of(key1, key2),
                List.of()
        );

        game.startGame();

        // Step 1: Collect key 1
        game.handleInput(68); // D
        game.updateTick();
        assertEquals(1, game.getCollectedRegularRewards());

        // MUST resume popup here
        game.handleInput(32); // SPACE

        // Step 2: Collect key 2
        game.handleInput(68); // D
        game.updateTick();
        assertEquals(2, game.getCollectedRegularRewards());

        assertTrue(board.getCell(3, 2) instanceof FloorCell);
        assertTrue(board.getCell(4, 2) instanceof FloorCell);
    }
}
