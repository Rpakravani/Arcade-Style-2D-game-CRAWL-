package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Direction;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.Reward;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Player basic movement rules:
 * - Moves into valid walkable cells
 * - Cannot move outside board boundaries
 * - Cannot move into non-walkable cells
 */
public class PlayerTest {

    /**
     * Creates a rectangular board filled entirely with {@link FloorCell} instances.
     * 
     * <p>This helper is used to isolate movement logic by ensuring that all cells
     * are walkable unless explicitly replaced in a test.</p>
     *
     * @param w width of the board (number of columns)
     * @param h height of the board (number of rows)
     * @return a fully initialized {@link Board} containing only floor cells
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
     * Tests that the player successfully moves into a valid walkable cell.
     *
     * <p>Expected behavior:</p>
     * <ul>
     *     <li>No reward is collected.</li>
     *     <li>The player's position updates correctly.</li>
     * </ul>
     */

    @Test
    void testPlayerMovesIntoValidCell() {
        Board board = makeEmptyBoard(5, 5);
        Player player = new Player(board, new Position(2, 2));

        Reward collected = player.move(Direction.NORTH);

        assertNull(collected, "No reward should be collected on empty floor");
        assertEquals(2, player.getPosition().getX());
        assertEquals(1, player.getPosition().getY());
    }
    /**
     * Tests that the player cannot move outside the boundaries of the board.
     *
     * <p>Expected behavior:</p>
     * <ul>
     *     <li>The move returns null.</li>
     *     <li>The player's position remains unchanged.</li>
     * </ul>
     */

    @Test
    void testPlayerCannotMoveOutsideBoard() {
        Board board = makeEmptyBoard(5, 5);
        Player player = new Player(board, new Position(0, 0));

        Reward collected = player.move(Direction.NORTH);

        assertNull(collected, "Move should fail and return null");
        assertEquals(0, player.getPosition().getX());
        assertEquals(0, player.getPosition().getY());
    }
    /**
     * Tests that the player cannot move into a non-walkable cell.
     *
     * <p>Expected behavior:</p>
     * <ul>
     *     <li>The move returns null.</li>
     *     <li>The player's position remains unchanged.</li>
     * </ul>
     */

    @Test
    void testPlayerCannotMoveIntoBlockedCell() {
        Board board = makeEmptyBoard(5, 5);
        Player player = new Player(board, new Position(2, 2));

        // Create a non-walkable cell
        Cell blocked = new Cell(new Position(2, 1)) {
            @Override
            public boolean isWalkable() { return false; }
        };
        board.setCell(2, 1, blocked);

        Reward collected = player.move(Direction.NORTH);

        assertNull(collected, "Move should fail into blocked cell");
        assertEquals(2, player.getPosition().getX());
        assertEquals(2, player.getPosition().getY());
    }
}
