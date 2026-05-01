package ca.sfu.cmpt276.team7.enemies;

import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.board.Board;

import java.util.PriorityQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * The goblin pathfinds towards the player
 */
public class Goblin extends Enemy {
	/**
	 * Creates a goblin at the given starting position on the board.
	 *
	 * @param board board the goblin moves on
	 * @param pos initial goblin position
	 */
	public Goblin(Board board, Position pos) {
		super(board);
		this.position = pos;
	}

	/**
	 * Moves the goblin 1 tile towards the player
	 */
	@Override
	public void updateMovement(Position player_position, Set<Position> occupied_positions) {
		// A* pathing algorithm
		Map<Position, Integer> costSoFar = new HashMap<Position, Integer>();
		Map<Position, Position> parent = new HashMap<Position, Position>();
		PriorityQueue<Position> open = new PriorityQueue<>((a, b) -> Integer.compare(
				pathingHeuristic(player_position, a) + costSoFar.get(a),
				pathingHeuristic(player_position, b) + costSoFar.get(b)));

		// Add the starting position to the open set
		costSoFar.put(this.position, 0);
		open.add(this.position);

		while (!open.isEmpty()) {
			Position next_pos = open.poll();

			if (next_pos.equals(player_position)) {
				// Get the full path
				Position tmp = next_pos;
				Position next_move = next_pos;
				while (!tmp.equals(this.position)) {
					next_move = tmp;
					tmp = parent.get(tmp);
				}
				this.position = next_move;
				return;
			}

			// Explore valid neighbouring cells and update their path cost

			int[][] deltas = { {0, 1}, {1, 0}, {0, -1}, {-1, 0} };

			for (int[] d : deltas) {
				int nx = next_pos.getX() + d[0];
				int ny = next_pos.getY() + d[1];

				if (nx < 0 || ny < 0 || nx >= board.getWidth() || ny >= board.getHeight()) {
					continue;
				}

				Position neighbour = new Position(nx, ny);

				if (canMoveto(board.getCell(nx, ny)) && !occupied_positions.contains(new Position(nx, ny))) { // check if valid
					// If this neighbour was already discovered, only update it if this path is cheaper
					if (costSoFar.containsKey(neighbour)) {
						if (costSoFar.get(neighbour) > costSoFar.get(next_pos) + 1) {
							costSoFar.put(neighbour, costSoFar.get(next_pos) + 1);
							parent.put(neighbour, next_pos);
							open.add(neighbour);
						}
					} else {
						costSoFar.put(neighbour, costSoFar.get(next_pos) + 1);
						parent.put(neighbour, next_pos);
						open.add(neighbour);
					}
				}
			}
		}
		// If it reaches this point player is unreachable
		return;

	}

	/**
	 * Internal function, calculates manhattan distance between the player and a
	 * given cell
	 */
	private int pathingHeuristic(Position player_position, Position cell_position) {
		return Math.abs(player_position.getX() - cell_position.getX()) +
				Math.abs(player_position.getY() - cell_position.getY());
	}

}
