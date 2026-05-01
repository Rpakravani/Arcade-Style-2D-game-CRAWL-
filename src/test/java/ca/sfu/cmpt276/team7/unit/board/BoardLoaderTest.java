package ca.sfu.cmpt276.team7.unit.board;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.board.BoardLoader;
import ca.sfu.cmpt276.team7.cells.BarrierCell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.cells.WallCell;
import ca.sfu.cmpt276.team7.core.Position;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BoardLoader
 *
 * These tests make sure that maps are loaded correctly from files,
 * including structure, special tiles, and error handling
 *
 * We also check that invalid maps are rejected properly
 */
public class BoardLoaderTest {

    /*
     * Planned tests:
     *
     * load_validMap_returnsBoardWithExpectedDimensions DONE
     * load_validMap_setsStartAndExitPositions DONE
     * load_validMap_recordsEntityMarkerPositions DONE
     *   - recordsGoblinSpawnPositions COVERED BY recordsEntityMarkerPositions
     *   - recordsOgreSpawnPositions COVERED BY recordsEntityMarkerPositions
     *   - recordsKeyPositions COVERED BY recordsEntityMarkerPositions
     *   - recordsTrapPositions COVERED BY recordsEntityMarkerPositions
     *
     * load_validMap_parsesWallBarrierAndFloorCells (barrier cell seperate test)
     * load_validMap_parsesWallAndFloorCells DONE
     * load_validMap_appliesWalkabilityRules DONE
     * load_markerTilesBecomeFloorCells DONE
     * load_walkabilityRulesMatchCellTypes (barrier cell seperate test)
     *
     * load_rejectsNullPath DONE
     * load_rejectsEmptyMap DONE
     * load_rejectsNonRectangularMap DONE
     * load_rejectsUnknownSymbol DONE
     * load_rejectsMissingStart DONE
     * load_rejectsMissingExit DONE
     */

    /**
     * Tests that a valid map file loads successfully and
     * produces a board with the correct width and height
     */
    @Test
    void load_validMap_returnsBoardWithExpectedDimensions() throws IOException {
        Path mapPath = Path.of("src/test/resources/maps/simpleValidMap.txt");

        BoardLoader.Result result = BoardLoader.load(mapPath);
        Board board = result.getBoard();

        assertEquals(5, board.getWidth());
        assertEquals(4, board.getHeight());
    }

    /**
     * Tests that the start and exit positions are correctly
     * identified and stored in the board
     */
    @Test
    void load_validMap_setsStartAndExitPositions() throws IOException {
        Path mapPath = Path.of("src/test/resources/maps/simpleValidMap.txt");

        BoardLoader.Result result = BoardLoader.load(mapPath);
        Board board = result.getBoard();

        assertEquals(new Position(1, 1), board.getStartPosition());
        assertEquals(new Position(3, 2), board.getEndPosition());
    }

    /**
     * Tests that wall and floor cells are parsed correctly
     * from the map file
     */
    @Test
    void load_validMap_parsesWallAndFloorCells() throws IOException {
        Path mapPath = Path.of("src/test/resources/maps/simpleValidMap.txt");

        BoardLoader.Result result = BoardLoader.load(mapPath);
        Board board = result.getBoard();

        assertTrue(board.getCell(0, 0) instanceof WallCell);
        assertTrue(board.getCell(1, 1) instanceof FloorCell);
        assertTrue(board.getCell(2, 1) instanceof FloorCell);
        assertTrue(board.getCell(3, 2) instanceof FloorCell);
    }

    /**
     * Tests that walkability rules are applied correctly:
     * walls should not be walkable, floors should be
     */
    @Test
    void load_validMap_appliesWalkabilityRules() throws IOException {
        Path mapPath = Path.of("src/test/resources/maps/simpleValidMap.txt");

        BoardLoader.Result result = BoardLoader.load(mapPath);
        Board board = result.getBoard();

        assertFalse(board.getCell(0, 0).isWalkable());
        assertTrue(board.getCell(1, 1).isWalkable());
        assertTrue(board.getCell(2, 1).isWalkable());
        assertTrue(board.getCell(3, 2).isWalkable());
    }

    /**
     * Tests that a non-rectangular map (rows of different lengths)
     * is rejected by the loader
     */
    @Test
    void load_rejectsNonRectangularMap() {
        Path mapPath = Path.of("src/test/resources/maps/nonRectangleMap.txt");

        assertThrows(IllegalArgumentException.class, () -> {
            BoardLoader.load(mapPath);
        });
    }

    /**
     * Tests that maps containing unknown/invalid symbols
     * are rejected
     */
    @Test
    void load_rejectsUnknownSymbol() {
        Path mapPath = Path.of("src/test/resources/maps/invalidSymbolMap.txt");

        assertThrows(IllegalArgumentException.class, () -> {
            BoardLoader.load(mapPath);
        });
    }

    /**
     * Tests that a map without a start position is rejected
     */
    @Test
    void load_rejectsMissingStart() {
        Path mapPath = Path.of("src/test/resources/maps/missingStartMap.txt");

        assertThrows(IllegalArgumentException.class, () -> {
            BoardLoader.load(mapPath);
        });
    }

    /**
     * Tests that a map without an exit position is rejected
     */
    @Test
    void load_rejectsMissingExit() {
        Path mapPath = Path.of("src/test/resources/maps/missingExitMap.txt");

        assertThrows(IllegalArgumentException.class, () -> {
            BoardLoader.load(mapPath);
        });
    }

    /**
     * Tests that entity markers (keys, traps, goblins, ogres)
     * are correctly recorded from the map
     */
    @Test
    void load_validMap_recordsEntityMarkerPositions() throws IOException {
        Path mapPath = Path.of("src/test/resources/maps/validWithEntitiesMap.txt");

        BoardLoader.Result result = BoardLoader.load(mapPath);

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
     * Tests that tiles which originally contained entity markers
     * are converted into normal floor cells after loading
     */
    @Test
    void load_markerTilesBecomeFloorCells() throws IOException {
        Path mapPath = Path.of("src/test/resources/maps/validWithEntitiesMap.txt");

        BoardLoader.Result result = BoardLoader.load(mapPath);
        Board board = result.getBoard();

        assertTrue(board.getCell(3, 1) instanceof FloorCell);
        assertTrue(board.getCell(3, 2) instanceof FloorCell);
        assertTrue(board.getCell(4, 1) instanceof FloorCell);
        assertTrue(board.getCell(4, 2) instanceof FloorCell);
    }

    /**
     * Tests that barrier cells are parsed correctly and that
     * they are not walkable
     */
    @Test
    void load_validMap_parsesBarrierCellAndAppliesBarrierWalkability() throws IOException {
        Path mapPath = Path.of("src/test/resources/maps/validBarrierMap.txt");

        BoardLoader.Result result = BoardLoader.load(mapPath);
        Board board = result.getBoard();

        assertTrue(board.getCell(2, 1) instanceof BarrierCell);
        assertFalse(board.getCell(2, 1).isWalkable());

        assertTrue(board.getCell(1, 1).isWalkable());
        assertTrue(board.getCell(3, 1).isWalkable());
    }

    /**
     * Tests that passing a null path throws an exception
     */
    @Test
    void load_rejectsNullPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            BoardLoader.load(null);
        });
    }

    /**
     * Tests that an empty map file is rejected
     */
    @Test
    void load_rejectsEmptyMap() {
        Path mapPath = Path.of("src/test/resources/maps/emptyMap.txt");

        assertThrows(IllegalArgumentException.class, () -> {
            BoardLoader.load(mapPath);
        });
    }
}