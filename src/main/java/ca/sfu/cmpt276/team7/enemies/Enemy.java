package ca.sfu.cmpt276.team7.enemies;

import java.util.Set;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.BarrierCell;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.WallCell;
import ca.sfu.cmpt276.team7.core.GameCharacter;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.PunishmentCell;

/**
 * Abstract base class for all enemy types in the game.
 *
 * <p>Enemy subclasses define their own movement behaviour through
 * {@link #updateMovement(Position, Set<Position>)}, while this class provides shared
 * board access and default movement rules.</p>
 */
public abstract class Enemy extends GameCharacter {
    /**
     * updateMovement() updates the position of the enemy by a single tick.
     * @param player_position current player position, which may be used
     *        by subclasses to decide movement
     * @param occupied_positions A set of dynamically updated positions that the enemy should avoid
     */
    abstract public void updateMovement(Position player_position, Set<Position> occupied_positions);
    /**
     * The Enemy constructor takes a pointer to the board.
     * 
     * @param board board the enemy moves on
     */
    public Enemy(Board board) {
	super(board);
    }

    /**
     * Returns whether this enemy can move onto the given cell.
     *
     * <p>By default, enemies cannot move onto {@link BarrierCell} or
     * {@link WallCell}, but may move onto other cell types.</p>
     *
     * @param cell target cell
     * @return {@code true} if the cell is traversable for this enemy,
     *         otherwise {@code false}
     */
    public boolean canMoveto(Cell cell) {
	if (!(cell instanceof BarrierCell ||
	      cell instanceof WallCell ||
	      cell instanceof PunishmentCell)) {
	    return true;
	}
	return false;
    }
}
