package ca.sfu.cmpt276.team7.cells;

import ca.sfu.cmpt276.team7.core.Position;


/**
 * concrete cell; a solid wall tile that blocks movement 
 */
public final class WallCell extends Cell{

    /**
     * Creates a wall cell at the given position 
     * 
     * @param position grid coordinate for this cell
     * @throws IllegalArgumentException if position is null 
     */
    public WallCell(Position position) {
        super(position);
    }

    /**
     * walls are not walkable 
     * 
     * @return alwats return false 
     */
    @Override 
    public boolean isWalkable() {
        return false;
    }
}
