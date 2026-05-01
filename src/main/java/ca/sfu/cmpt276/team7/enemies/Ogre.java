package ca.sfu.cmpt276.team7.enemies;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.core.Direction;
import ca.sfu.cmpt276.team7.core.Position;

/**
 * Enemy that patrols along a predefined route or along a straight line
 * determined from a starting position and direction.
 *
 * <p>
 * An ogre moves back and forth along its patrol path. If it reaches
 * the end of the path or cannot move forward, it reverses direction.
 * </p>
 */
public class Ogre extends Enemy {
	/**
	 * Small helper enum for directions in constructor
	 */

	/** Ordered list of board positions that define this ogre's patrol path. */
	private ArrayList<Position> patrolRoute;
	/** True if the ogre is currently moving forward through the patrol route. */
	private boolean forward_p;
	/** Current index of the ogre within {@link #patrolRoute}. */
	private int route_index;

	/** Initial position used to restore this ogre during reset. */
	private final Position initialPosition;
	/** Initial patrol direction used to restore this ogre during reset. */
	private final boolean initialForwardP;
	/** Initial patrol route index used to restore this ogre during reset. */
	private final int initialRouteIndex;

	/**
	 * Creates an ogre that follows the given patrol route.
	 *
	 * <p>
	 * The ogre starts at the first position in the route and initially
	 * moves forward through the list.
	 * </p>
	 *
	 * @param board board the ogre moves on
	 * @param route ordered positions that define the patrol route
	 */
	public Ogre(Board board, Position... route) {
		super(board);
		this.patrolRoute = new ArrayList<>(List.of(route));
		forward_p = true;
		position = patrolRoute.get(0);

		this.initialPosition = this.position;
		this.initialForwardP = this.forward_p;
		this.initialRouteIndex = this.route_index;
	}

	/**
	 * Creates an ogre that patrols back and forth in a straight line.
	 *
	 * <p>
	 * Starting from {@code first_position}, this constructor expands the patrol
	 * route in the given direction until movement is blocked or the board boundary
	 * is reached, then expands in the opposite direction as well.
	 * </p>
	 *
	 * @param board          board the ogre moves on
	 * @param first_position starting position of the ogre
	 * @param direction      direction used to build the straight-line patrol route
	 */
	public Ogre(Board board, Position first_position, Direction direction) {
		super(board);
		this.position = first_position;
		this.patrolRoute = new ArrayList<Position>();

		Position potential_position = position;
		Cell next_cell = board.getCell(potential_position.getX(), potential_position.getY());

		int add_x = (direction == Direction.EAST) ? 1 : (direction == Direction.WEST ? -1 : 0);
		int add_y = (direction == Direction.SOUTH) ? 1 : (direction == Direction.NORTH ? -1 : 0);

		// Go in one direction until a wall is hit
		while (canMoveto(next_cell)) {
			patrolRoute.add(potential_position);

			int nextX = potential_position.getX() + add_x;
			int nextY = potential_position.getY() + add_y;

			if (nextX < 0 || nextY < 0 || nextX >= board.getWidth() || nextY >= board.getHeight()) {
				break;
			}

			potential_position = new Position(nextX, nextY);
			next_cell = board.getCell(nextX, nextY);
		}

		// Go in the other direction, adding to the other side of the list

		potential_position = this.position;
		next_cell = board.getCell(potential_position.getX(), potential_position.getY());
		int num_added = 0;
		add_x *= -1;
		add_y *= -1;

		while (canMoveto(next_cell)) {
			if (num_added != 0) { // Check to make sure the beginning position isn't added twice
				patrolRoute.add(0, potential_position);
			}
			num_added++;

			int nextX = potential_position.getX() + add_x;
			int nextY = potential_position.getY() + add_y;

			if (nextX < 0 || nextY < 0 || nextX >= board.getWidth() || nextY >= board.getHeight()) {
				break;
			}

			potential_position = new Position(nextX, nextY);
			next_cell = board.getCell(nextX, nextY);
		}

		// set route_index to the middle
		route_index = num_added - 1;

		this.forward_p = (direction == Direction.SOUTH || direction == Direction.EAST) ? true : false;

		this.initialPosition = this.position;
		this.initialForwardP = this.forward_p;
		this.initialRouteIndex = this.route_index;

	}

	/**
	 * Restores the ogre's initial position, patrol direction, and route index.
	 */
	public void resetState() {
		this.position = initialPosition;
		this.forward_p = initialForwardP;
		this.route_index = initialRouteIndex;
	}

	/**
	 * Updates the ogre's movement by attempting to advance along its patrol route.
	 *
	 * <p>
	 * If movement in the current direction fails, the ogre reverses direction
	 * and attempts movement once more. If both attempts fail, the ogre remains
	 * still.
	 * </p>
	 *
	 * @param player_position    current player position; currently unused by this
	 *                           enemy type
	 * @param occupied_positions Dynamically updated set of positions the ogre
	 *                           cannot move into
	 */
	public void updateMovement(Position player_position, Set<Position> occupied_positions) {
		if (internalUpdate(occupied_positions)) {
			return;
		} else {
			// if it fails in both directions, it is stuck, stay still
			internalUpdate(occupied_positions);
		}

	}

	/**
	 * Attempts to move the ogre one step along its patrol route.
	 *
	 * <p>
	 * If the next route index is out of bounds or the next cell is not movable,
	 * the ogre reverses direction and returns {@code false}. Otherwise, it updates
	 * its position and route index and returns {@code true}.
	 * </p>
	 *
	 * @param occupied_positions Dynamically update set of positions the ogre cannot
	 *                           move into
	 *
	 * @return {@code true} if the ogre successfully moved, otherwise {@code false}
	 */
	private boolean internalUpdate(Set<Position> occupied_positions) {
		// update index

		int next_index = (forward_p) ? route_index + 1 : route_index - 1;

		if (next_index < 0 || next_index >= patrolRoute.size()) {
			forward_p = !forward_p;
			return false;
		}

		Position potential_position = patrolRoute.get(next_index);
		Cell next_cell = board.getCell(potential_position.getX(), potential_position.getY());

		if (this.canMoveto(next_cell) && !occupied_positions.contains(potential_position)) {
			position = potential_position;
			route_index = next_index;
			return true;
		} else {
			forward_p = !forward_p;
			return false;
		}
	}
}
