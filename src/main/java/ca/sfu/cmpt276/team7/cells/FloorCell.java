package ca.sfu.cmpt276.team7.cells;

import ca.sfu.cmpt276.team7.core.Position;

/**
 * concrete cell; a normal walkable tile 
 */
public final class FloorCell extends Cell {

    /**
     * creates a floor cell at the given position 
     * 
     * @param position grid coordinate for this cell 
     * @throws IllegalArgumentException if position is null 
     */
    public FloorCell(Position position) {
        super(position); 
    }

    /**
     * a FloorCell is walkable 
     * 
     * @return true always
     */
    @Override
    public boolean isWalkable() {
        return true;
    }
}
