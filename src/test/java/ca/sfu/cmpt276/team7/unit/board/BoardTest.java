package ca.sfu.cmpt276.team7.unit.board;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Position;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Board} class.
 *
 * <p>This test class checks the main responsibilities of Board, including:
 * creating a valid board, rejecting invalid board data, getting and setting cells,
 * checking whether positions are inside the board, and validating start/end positions
 *
 * <p>The goal is to test Board in isolation and make sure its rules are enforced properly
 */
public class BoardTest {
    /*
     * Planned tests:
     *
     * constructor validations:
        * constructor_setsWidthAndHeight DONE 
        * constructor_rejectsNullGrid DONE 
        * constructor_rejectsEmptyGrid DONE 
        * constructor_rejectsNonRectangularGrid DONE 
        * constructor_rejectsNullCell DONE 
     *
     * getCell_returnsCellInBounds DONE
     * getCell_throwsWhenOutOfBounds (when X too small or large, when y too small or large, 4 tests total) DONE 
     *
     * setCell_replacesCellInBounds DONE 
     * setCell_rejectsNullCell DONE 
     * setCell_throwsWhenOutOfBounds DONE 
     *
     * Position related logic: 
        * isInside_returnsTrueForValidPositions DONE 
        * isInside_returnsFalseForOutsidePositions DONE 
        *
        * 6 tests here: 
            * setStartPosition_setsValidPosition DONE
            * setStartPosition_rejectsNull DONE
            * setStartPosition_rejectsOutOfBounds DONE
            *
            * setEndPosition_setsValidPosition DONE
            * setEndPosition_rejectsNull DONE
            * setEndPosition_rejectsOutOfBounds DONE
     */

    /**
     * Tests that the Board constructor correctly stores width and height
     * based on the size of the input grid
     *
     * <p>In this test, a 2x3 grid is created. That means the board should
     * have height 2 and width 3
     */
    @Test // tells JUnit this is a test method 
    void constructor_setsWidthAndHeight() { // trying to follow naming concention: method_beingTested_expectedBehaviour 
        // ========================
        // Arrange
        // ========================
        Cell[][] grid = new Cell[2][3];

        for (int y = 0; y < 2; y++) { // filling b/c constructor requires no null rows and cells 
            for (int x = 0; x < 3; x++) {
                grid[y][x] = new FloorCell(new Position(x, y));
            }
        }

        // ========================
        // Act
        // ========================
        Board board = new Board(grid);

        // ========================
        // Assert (checks expected vs actual)
        // ========================
        assertEquals(3, board.getWidth());
        assertEquals(2, board.getHeight());
    }

    /*
    testing if Board constructor throws the right exception
    learning to use assertThrows(), which will also be used to test:
        invalid maps
        bad symbols
        missing start/end
        out of bounds access 

    Board constructor starts with: 
        if (grid == null || grid.length == 0) {
        throw new IllegalArgumentException("grid must be non null and not empty");
        }
    so 
        new Board(null);
    should throw
        IllegalArgumentException 

    lets tests it 
    */
   /*
    the basic form of assert throws is:
        assertThrows(ExceptionType.class, () -> {
        // code that should throw
        });

    () -> { ...}
    is a lambda, a function with not arguments 
    JUnit needs it because it wants to "run this code and watch whether it throws"
    for testing just think that lambas, () -> { ...}, just mean "here is the code I want JUnit to execute"
    */

    /**
     * Tests that the Board constructor rejects a null grid
     *
     * <p>A Board cannot be created from null, so this should throw
     * an {@link IllegalArgumentException}.
     */
    @Test
    void constructor_rejectsNullGrid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Board(null);
        });
    }

    /**
     * Tests that the Board constructor rejects an empty grid
     *
     * <p>A grid with length 0 is not a valid board, so the constructor
     * should throw an {@link IllegalArgumentException}.
     */
    @Test
    void constructor_rejectsEmptyGrid() {
        Cell[][] grid = new Cell[0][0];

        assertThrows(IllegalArgumentException.class, () -> {
            new Board(grid);
        });
    }

    /**
     * Tests that the Board constructor rejects a non-rectangular grid
     *
     * <p>This means different rows have different lengths, which would
     * make the board shape inconsistent
     */
    @Test
    void constructor_rejectsNonRectangularGrid() {
        // Arrange
        Cell[][] grid = new Cell[2][];
        grid[0] = new Cell[3];
        grid[1] = new Cell[2];

        for (int x = 0; x < 3; x++) {
            grid[0][x] = new FloorCell(new Position(x, 0));
        }

        for (int x = 0; x < 2; x++) {
            grid[1][x] = new FloorCell(new Position(x, 1));
        }

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Board(grid);
        });
    }

    /*
    Board constructor ensures every coordinate contains a real Cell object:
        if (grid[y][x] == null) {
        throw new IllegalArgumentException(...);
        }
     */

    /**
     * Tests that the Board constructor rejects grids containing null cells
     *
     * <p>Every spot in the board must contain a real Cell object
     * If even one cell is null, the constructor should reject the grid
     */
    @Test
    void constructor_rejectsNullCell() {
        // Arrange
        Cell[][] grid = new Cell[2][2];

        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = null; // the one invalid cell

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Board(grid);
        });
    }

    /*
    make a valid board
    ask for a cell at a valid coordinate 
    verify the object is correct 

        public Cell getCell(int x, int y) {
        ensureInBounds(x, y);
        return grid[y][x];
        }
    so if (x,y) is valid then it should reutrn the cell stored at that spot 
    this test checks if the board stores cells correctly and if getCell(x,y) uses the coordinates correctly 

    How do we know if we get back the exact same cell object that we put into the grid??
    we use 
        assertSame(expected, actual)
    instead of
        asserEquals
    because Cell does not define a custom equals() method, and in this case we don't want the same value, but the same object reference 
    assertEquals = same value
    assertSame = same object in memory 
     */

    /**
     * Tests that getCell returns the exact cell stored at a valid coordinate
     *
     * <p>This checks both that the board stores cells correctly and that
     * getCell(x, y) looks up the correct location in the grid
     */
    @Test
    void getCell_returnsCellInBounds() {
        // Arrange
        Cell[][] grid = new Cell[2][2];

        FloorCell topLeft = new FloorCell(new Position(0, 0));
        FloorCell topRight = new FloorCell(new Position(1, 0));
        FloorCell bottomLeft = new FloorCell(new Position(0, 1));
        FloorCell bottomRight = new FloorCell(new Position(1, 1));

        grid[0][0] = topLeft;
        grid[0][1] = topRight;
        grid[1][0] = bottomLeft;
        grid[1][1] = bottomRight;

        Board board = new Board(grid);

        // Act
        Cell result = board.getCell(1, 0);

        // Assert
        assertSame(topRight, result);
    }

    /*
        private void ensureInBounds(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            throw new IndexOutOfBoundsException(...);
        }
    so any invalid coordinate should throw IndexOutofBoundsException
    The next four tests will cover all boundary directions 
    */

    /**
     * Tests that getCell throws an exception when x is smaller than the valid range
     *
     * <p>Using x = -1 should be outside the board and should throw
     * an {@link IndexOutOfBoundsException}.
     */
    @Test
    void getCell_throwsWhenXTooSmall() {
        // Arrange
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        // Act + Assert
        assertThrows(IndexOutOfBoundsException.class, () -> {
            board.getCell(-1, 0);
        });
    }

    /**
     * Tests that getCell throws an exception when x is larger than the valid range
     *
     * <p>For a 2-wide board, x = 2 is out of bounds and should throw
     * an {@link IndexOutOfBoundsException}.
     */
    @Test
    void getCell_throwsWhenXTooLarge() {
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            board.getCell(2, 0);
        });
    }

    /**
     * Tests that getCell throws an exception when y is smaller than the valid range
     *
     * <p>Using y = -1 should be outside the board and should throw
     * an {@link IndexOutOfBoundsException}.
     */
    @Test
    void getCell_throwsWhenYTooSmall() {
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            board.getCell(0, -1);
        });
    }

    /**
     * Tests that getCell throws an exception when y is larger than the valid range
     *
     * <p>For a 2-high board, y = 2 is out of bounds and should throw
     * an {@link IndexOutOfBoundsException}.
     */
    @Test
    void getCell_throwsWhenYTooLarge() {
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            board.getCell(0, 2);
        });
    }

    /*
        public void setCell(int x, int y, Cell cell) {
        ensureInBounds(x, y);
        if (cell == null) throw new IllegalArgumentException("cell cannot be null");
        grid[y][x] = cell;
        }
    so there are 3 behaviours to test:
        1. valid case -> replaces cell 
        2. null cell -> throws IllegalArgumentException
        3. out of bounds -> throws IndexOutofBoundsException 
     */

    /**
     * Tests that setCell successfully replaces a cell at a valid coordinate
     *
     * <p>After calling setCell, getCell should return the new cell object
     * at that same location
     */
    @Test
    void setCell_replacesCellInBounds() {
        // Arrange
        Cell[][] grid = new Cell[2][2];

        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        FloorCell newCell = new FloorCell(new Position(0, 0));

        // Act
        board.setCell(0, 0, newCell);

        // Assert
        assertSame(newCell, board.getCell(0, 0));
    }

    /**
     * Tests that setCell rejects a null cell value
     *
     * <p>A board location cannot be replaced with null, so this should
     * throw an {@link IllegalArgumentException}.
     */
    @Test
    void setCell_rejectsNullCell() {
        // Arrange
        Cell[][] grid = new Cell[2][2];

        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> {
            board.setCell(0, 0, null);
        });
    }

    /**
     * Tests that setCell throws an exception when the target coordinate
     * is out of bounds
     *
     * <p>Trying to place a cell outside the board should throw
     * an {@link IndexOutOfBoundsException}.
     */
    @Test
    void setCell_throwsOutOfBounds() {
        // Arrange
        Cell[][] grid = new Cell[2][2];

        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        FloorCell newCell = new FloorCell(new Position(0, 0));

        // Act + Assert
        assertThrows(IndexOutOfBoundsException.class, () -> {
            board.setCell(2, 0, newCell);
        });
    }

    /*
    test isInside(...): (this will teach boundary value testing directly)

        public boolean isInside(Position position) {
            if (position.getX() < 0 ||
                position.getY() < 0 ||
                position.getX() >= width ||
                position.getY() >= height) {
                return false;
            } else {
                return true;
            }
        }

        i.e. for a board of width and height 2 the valid x and y calues are 0, 1
             so (0,0), (1, 1) is inside, (-1, 0), (2, 0), (0, -1) are outside
             this is the edge testing mentioned in lectures
    
    have to be aware, the Position constructor does this:
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException(...)
        }
    so we cannot create: 
        new Position(-1, 0);
        new Position(0, -1);
    meaning we can't directly test the negative coordiante branches of isInside using Position because Position itself rejects negative inputs first 
    but we can still test the branches that are reachable through valid Position objects like the corners 
     */

    /**
     * Tests that isInside returns true for valid board positions.
     *
     * <p>This checks coordinates that are on the board, including corners.
     */
    @Test // valid positions return true 
    void isInside_returnsTrueForValidPositions() {
        // Arrange
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        // Act + Assert
        assertTrue(board.isInside(new Position(0, 0)));
        assertTrue(board.isInside(new Position(1, 1)));
    }

    /**
     * Tests that isInside returns false for positions outside the board
     *
     * <p>Since Position does not allow negative values, this test checks
     * positions that are too large instead
     */
    @Test // too large coordinates return false 
    void isInside_returnsFalseForOutsidePositions() {
        // Arrange
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        // Act + Assert
        assertFalse(board.isInside(new Position(2, 0)));
        assertFalse(board.isInside(new Position(0, 2)));
    }

    /*
    testing setStartPosition(...) and setEndPosition(...)
        public void setStartPosition(Position startPosition) {
            if (startPosition == null) throw new IllegalArgumentException(...);
            ensureInBounds(startPosition.getX(), startPosition.getY());
            this.startPosition = startPosition;
        }
    setEndPosition works the same 

    So we need 3 tests for each one:
        1. valid position gets stored
        2. null is rejected
        3. out of bounds is rejected 
     */

    /**
     * Tests that setStartPosition stores a valid position successfully
     *
     * <p>After setting the start position, getStartPosition should return
     * the same position object
     */
    @Test
    void setStartPosition_setsValidPosition() {
        // Arrange
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);
        Position start = new Position(1, 0);

        // Act
        board.setStartPosition(start);

        // Assert
        assertEquals(start, board.getStartPosition());
    }

    /**
     * Tests that setStartPosition rejects null
     *
     * <p>The board should not accept a null start position, so this should
     * throw an {@link IllegalArgumentException}
     */
    @Test
    void setStartPosition_rejectsNull() {
        // Arrange
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> {
            board.setStartPosition(null);
        });
    }

    /**
     * Tests that setStartPosition rejects positions outside the board
     *
     * <p>If the given position is out of bounds, the board should throw
     * an {@link IndexOutOfBoundsException}.
     */
    @Test
    void setStartPosition_rejectsOutOfBounds() {
        // Arrange
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        // Act + Assert
        assertThrows(IndexOutOfBoundsException.class, () -> {
            board.setStartPosition(new Position(2, 0));
        });
    }

    /**
     * Tests that setEndPosition stores a valid end position successfully
     *
     * <p>After setting the end position, getEndPosition should return
     * the same position object
     */
    @Test
    void setEndPosition_setsValidPosition() {
        // Arrange
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);
        Position end = new Position(1, 1);

        // Act
        board.setEndPosition(end);

        // Assert
        assertEquals(end, board.getEndPosition());
    }

    /**
     * Tests that setEndPosition rejects null
     *
     * <p>The board should not accept a null end position, so this should
     * throw an {@link IllegalArgumentException}
     */
    @Test
    void setEndPosition_rejectsNull() {
        // Arrange
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> {
            board.setEndPosition(null);
        });
    }

    /**
     * Tests that setEndPosition rejects positions outside the board
     *
     * <p>If the given end position is out of bounds, the board should throw
     * an {@link IndexOutOfBoundsException}.
     */
    @Test
    void setEndPosition_rejectsOutOfBounds() {
        // Arrange
        Cell[][] grid = new Cell[2][2];
        grid[0][0] = new FloorCell(new Position(0, 0));
        grid[0][1] = new FloorCell(new Position(1, 0));
        grid[1][0] = new FloorCell(new Position(0, 1));
        grid[1][1] = new FloorCell(new Position(1, 1));

        Board board = new Board(grid);

        // Act + Assert
        assertThrows(IndexOutOfBoundsException.class, () -> {
            board.setEndPosition(new Position(0, 2));
        });
    }
}