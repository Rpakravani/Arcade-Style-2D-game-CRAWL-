package ca.sfu.cmpt276.team7.core;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;

/**
 * Abstract base class for movable characters in the game.
 *
 * <p>This class stores shared state such as the character's current
 * position and the board the character belongs to.</p>
 */
abstract public class GameCharacter {
    /** Current board position of the character. */
    protected Position position;
    /** Board the character exists and moves on. */
    protected Board board;

    /**
     * Creates a game character associated with the given board.
     *
     * @param board board the character belongs to
     */
    public GameCharacter(Board board){
	this.board = board;
    }

    /**
     * Returns whether this character can move onto the given cell.
     *
     * @param cell target cell
     * @return {@code true} if the character can move onto the cell,
     *         otherwise {@code false}
     */
    abstract public boolean canMoveto(Cell cell);

    /**
     * Returns the character's current position.
     *
     * @return current position
     */
    public Position getPosition() {
	return position;
    }

    /**
     * Sets the character's current position.
     *
     * @param position new position
     */
    public void setPosition(Position position) {
	this.position = position;
    }
}
