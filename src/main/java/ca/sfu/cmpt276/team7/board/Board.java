package ca.sfu.cmpt276.team7.board;

import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.core.Position;


/**
 * represents the dungeon board as a 2D grid of {@link Cell}
 * more specifically: store the dungeon as a full Cell[][] grid
 * providing getCell(x,y) and setCell(x, y, cell)
 * <p>
 * coordinates are in cell units (not pixels!):
 * x = coloumn, y = row
 * </p>
 */
public class Board {

    /** Backing 2D grid that stores all board cells. */
    private final Cell[][] grid;
    /** Number of columns in the board. */
    private final int width;
    /** Number of rows in the board. */
    private final int height;

    /** Starting position defined by the map. */
    private Position startPosition;
    /** Exit position defined by the map. */
    private Position endPosition;

    /**
     * creates a new board with the given grid 
     * <p>
     * important: this grid should contain a cell at every coordinate, no null entries 
     * </p>
     * 
     * @param grid rectangular non empty grid of cells
     * @throws IllegalArgumentException if grid is null, empty, not rectangle shaped or contains null rows/cells
     */
    public Board(Cell[][] grid) {
        if (grid == null || grid.length == 0) throw new IllegalArgumentException("grid must be non null and not empty");
        if (grid[0] == null || grid[0].length == 0) throw new IllegalArgumentException("grid rows must be non null and not empty");

        this.height = grid.length;
        this.width = grid[0].length; 

        // validate rectangular grid + no null cells 
        for (int y = 0; y < height; y++) {
            if (grid[y] == null || grid[y].length != width) throw new IllegalArgumentException("grid must be rectangular (all rows same length and non null)");

            for (int x = 0; x < width; x++) {
                if (grid[y][x] == null) throw new IllegalArgumentException("grid contains nullc ell at (" + x + "," + y + ")");
            }
        }

        this.grid = grid;
    }
    
    /**@return width aka the number of coloumns */
    public int getWidth() {
        return width;
    }

    /**@return height aka the number of rows */
    public int getHeight() {
        return height;
    }

    /**@return returns if a given position is inside the board*/
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

    /**
     *@return returns the grid
     */
    public Cell[][] getGrid() {
	return grid;
    }

    /**@return start position defined by the map */
    public Position getStartPosition() {
        return startPosition; 
    }

    /**
     * sets the start position 
     * 
     * @param startPosition start position
     * @throws IllegalArgumentException if null or out of bounds 
     * @throws IndexOutOfBoundsException if the position is outside the board
     */
    public void setStartPosition(Position startPosition) {
        if (startPosition == null) throw new IllegalArgumentException("startPosition cannot be null");
        ensureInBounds(startPosition.getX(), startPosition.getY()); 
        this.startPosition = startPosition;
    }

    /**@return end/exit position defined by the map */
    public Position getEndPosition() {
        return endPosition;
    }

    /**
     * sets the end/exit position 
     * 
     * @param endPosition exit tile coordinate 
     * @throws IllegalArgumentException if null or out of bounds 
     * @throws IndexOutOfBoundsException if the position is outside the board
     */
    public void setEndPosition(Position endPosition) {
        if (endPosition == null) throw new IllegalArgumentException("endPosition cannot be null");
        ensureInBounds(endPosition.getX(), endPosition.getY());
        this.endPosition = endPosition; 
    }

    /**
     * returns the cell at (x,y)
     * 
     * @param x column index 
     * @param y row index
     * @return the cell at that coordinate 
     * @throws IndexOutOfBoundsException if out of bounds 
     */
    public Cell getCell(int x, int y) {
        ensureInBounds(x, y);
        return grid[y][x]; 
    }

    /**
     * replaces the cell at (x,y) 
     * <p>
     * one use case would be: whena RewardCell is collected, replace it with a FloorCell
     * </p>
     * 
     * @param x x coloumn index
     * @param y row index
     * @param cell new cell (must not be null)
     * @throws IndexOutOfBoundsException if out of bounds 
     * @throws IllegalArgumentException if cell is null 
     */
    public void setCell(int x, int y, Cell cell) {
        ensureInBounds(x, y);
        if (cell == null) throw new IllegalArgumentException("cell cannot be null");
        grid[y][x] = cell; 
    }

    /**
     * checks if (x,y) is inside the grid 
     * 
     * @param x coloumn 
     * @param y row
     * @throws IndexOutOfBoundsException if out of bounds 
     */
    private void ensureInBounds(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) throw new IndexOutOfBoundsException("Out of bounds: (" + x + "," + y + ")");
    }
}
