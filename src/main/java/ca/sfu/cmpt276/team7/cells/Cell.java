package ca.sfu.cmpt276.team7.cells;

import ca.sfu.cmpt276.team7.core.Position; 
// tells Java which Position class to use
// so we don't have to write the fuly qualified name every time, i.e. private final ca.sfu.cmpt276.team7.core.Position position;

/**
 * represents one tile on the board grid 
 * <p>
 * subclasses define what kind of tile it is (floor, wall, barrier, reward cell, etc.) and can and whether the tile is walkable or not 
 * </p>
 */
public abstract class Cell {
    private final Position position; // private final means only Cell can access it and it can only be assigned once (in the constructor) and never changed afterwards 

    /**
     * Creates a cell at the given grid position 
     * @param position cell coordinate in grid units (not pixel!)
     * @throws IllegalArgumentException if position is NULL 
     */
    protected Cell(Position position) { // protected so only subclasses can class this constructor 
        if(position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        this.position = position;
    }

    /**
     * @return this cell's grid coordinate 
     */
    public Position getPosition() {
        return position; 
    }

    /**
     * Whther a character can move into this cell 
     * @return true if a character can enter this cell
     */
    public abstract boolean isWalkable();
}
