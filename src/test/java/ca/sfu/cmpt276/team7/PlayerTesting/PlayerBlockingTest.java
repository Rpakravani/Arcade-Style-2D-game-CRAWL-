package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.cells.WallCell;
import ca.sfu.cmpt276.team7.core.Direction;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.Player;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Player Blocking Rule:
 *
 * <p>The player must NOT move into a non-walkable cell (e.g., a wall).
 * When attempting such a move:
 * <ul>
 *     <li>the player's position must remain unchanged</li>
 *     <li>move() must return null</li>
 *     <li>the board must remain unchanged</li>
 * </ul>
 */
public class PlayerBlockingTest {

    /** Creates a board filled with FloorCells. */
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
    void testPlayerCannotMoveIntoWall() {
        Board board = makeEmptyBoard(5, 5);
        Player player = new Player(board, new Position(2, 2));

        // Place a wall north of the player
        Position wallPos = new Position(2, 1);
        board.setCell(2, 1, new WallCell(wallPos));

        // Attempt to move into the wall
        var result = player.move(Direction.NORTH);

        // 1. move() must return null
        assertNull(result, "Player should not collect anything when blocked");

        // 2. Player must NOT move
        assertEquals(2, player.getPosition().getX());
        assertEquals(2, player.getPosition().getY(),
                "Player position must remain unchanged when blocked");

        // 3. Wall must remain unchanged
        assertTrue(board.getCell(2, 1) instanceof WallCell,
                "WallCell should remain unchanged after blocked movement");
    }
}
