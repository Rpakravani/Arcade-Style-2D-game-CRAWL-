package ca.sfu.cmpt276.team7.integration.board;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.board.BoardLoader;
import ca.sfu.cmpt276.team7.core.Position;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the full board initialization flow
 *
 * Unlike unit tests, these tests check multiple components working together,
 * including reading map files, loading them, and building the board + ...
 *
 * This makes sure the whole pipeline works, not just individual pieces
 */
public class BoardInitializationIntegrationTest {

    /*
     * Planned tests:
     *
     * validMap_initializationFlow_buildsBoardAndMetadata
     * invalidMap_initializationFlow_throwsAndStopsInitialization
     *
     * BoardTest was isolated in memory
     * BoardLoaderTest focused on one class
     *
     * These tests are broader and cover interaction between:
     * - filesystem test resources (map files)
     * - Path
     * - BoardLoader
     * - Board
     *
     * So these are true integration tests since they test the full flow
     */

    /**
     * Tests that a valid map file goes through the full initialization flow
     * and correctly builds both the Board and all associated metadata
     *
     * This includes:
     * - start and end positions
     * - entity marker positions (keys, traps, enemies)
     */
    @Test
    void validMap_initializationFlow_buildsBoardAndMetadata() throws IOException {
        Path mapPath = Path.of("src/test/resources/maps/validWithEntitiesMap.txt");

        BoardLoader.Result result = BoardLoader.load(mapPath);
        Board board = result.getBoard();

        assertNotNull(result);
        assertNotNull(board);

        assertEquals(new Position(1, 1), board.getStartPosition());
        assertEquals(new Position(5, 2), board.getEndPosition());

        assertEquals(1, result.getKeyPositions().size());
        assertEquals(1, result.getTrapPositions().size());
        assertEquals(1, result.getGoblinSpawns().size());
        assertEquals(1, result.getOgreSpawns().size());

        assertEquals(new Position(3, 1), result.getKeyPositions().get(0));
        assertEquals(new Position(3, 2), result.getTrapPositions().get(0));
        assertEquals(new Position(4, 1), result.getGoblinSpawns().get(0));
        assertEquals(new Position(4, 2), result.getOgreSpawns().get(0));
    }

    /**
     * Tests that an invalid map file causes the initialization flow to fail,
     * and that no board is created
     *
     * In this case, the map contains an invalid symbol, so we expect
     * BoardLoader to throw an IllegalArgumentException
     */
    @Test
    void invalidMap_initializationFlow_throwsAndStopsInitialization() {
        Path mapPath = Path.of("src/test/resources/maps/invalidSymbolMap.txt");

        assertThrows(IllegalArgumentException.class, () -> {
            BoardLoader.load(mapPath);
        });
    }
}