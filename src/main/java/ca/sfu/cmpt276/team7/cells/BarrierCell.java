package ca.sfu.cmpt276.team7.cells;

import ca.sfu.cmpt276.team7.core.Position;

/**
 * concrete cell; a barrier tile that blocks movement 
 * <p>
 * kept seperately form WallCell so we can treat it different later, even though both block movement!
 * </p>
 */
public final class BarrierCell extends Cell {

    /**
     * creates a barrier cell at the given position 
     * 
     * @param position grid coordinate for this cell
     * @throws IllegalArgumentException if position is null
     */
    public BarrierCell(Position position) {
        super(position);
    }

    /**
     * barrier are not walkable
     * 
     * @return always return false 
     */
    @Override 
    public boolean isWalkable() {
        return false; 
    }
}
