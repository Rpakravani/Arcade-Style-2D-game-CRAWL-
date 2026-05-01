package ca.sfu.cmpt276.team7.unit.enemies;

import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.cells.WallCell;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.core.Direction;
import ca.sfu.cmpt276.team7.reward.PunishmentCell;
import ca.sfu.cmpt276.team7.reward.TrapPunishment;
import ca.sfu.cmpt276.team7.enemies.Ogre;

import java.util.Set;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class OgreTest {
	// Ogre Patrol Rule
	// Tests that the ogre can follow a short patrol route
	@Test
	void TestOgrePatrol() {
		// This is messy, but it reduces the use of other classes in unit tests
		Board board = new Board(new Cell[][] {
				{ new FloorCell(new Position(0, 0)), new FloorCell(new Position(1, 0)) },
				{ new FloorCell(new Position(0, 1)), new FloorCell(new Position(1, 1)) },

		});
		Ogre ogre = new Ogre(board, new Position(0, 0), new Position(0, 1), new Position(1, 1), new Position(1, 0));
		Set<Position> occupied = new HashSet<>();

		// Placeholder player position, doesn't affect ogre's movement
		Position playerPos = new Position(0, 0);
		// Ensure the ogre follows the exact route
		assertTrue(ogre.getPosition().equals(new Position(0, 0)));
		ogre.updateMovement(playerPos, occupied);
		assertTrue(ogre.getPosition().equals(new Position(0, 1)));
		ogre.updateMovement(playerPos, occupied);
		assertTrue(ogre.getPosition().equals(new Position(1, 1)));
		ogre.updateMovement(playerPos, occupied);
		assertTrue(ogre.getPosition().equals(new Position(1, 0)));
	}

	// Ogre Reverse Rule
	// Tests that the ogre will turn around at the end of a patrol route
	@Test
	void TestOgreReverse() {
		// This is messy, but it reduces the use of other classes in unit tests
		Board board = new Board(new Cell[][] {
				{ new FloorCell(new Position(0, 0)), new FloorCell(new Position(1, 0)),
						new FloorCell(new Position(2, 0)) },
		});
		Ogre ogre = new Ogre(board, new Position(0, 0), new Position(1, 0), new Position(2, 0));
		Set<Position> occupied = new HashSet<>();

		// Placeholder player position, doesn't affect ogre's movement
		Position playerPos = new Position(0, 0);

		// Ensure the ogre follows the exact route, reversing when necessary, instead of
		// looping around
		assertTrue(ogre.getPosition().equals(new Position(0, 0)));
		ogre.updateMovement(playerPos, occupied);
		assertTrue(ogre.getPosition().equals(new Position(1, 0)));
		ogre.updateMovement(playerPos, occupied);
		assertTrue(ogre.getPosition().equals(new Position(2, 0)));
		ogre.updateMovement(playerPos, occupied);
		assertTrue(ogre.getPosition().equals(new Position(1, 0)));
	}

	// Ogre Collision Rule
	// Tests that the ogre will turn around when running into a wall
	@Test
	void TestOgreCollision() {
		// This is messy, but it reduces the use of other classes in unit tests
		Board board = new Board(new Cell[][] {
				{ new FloorCell(new Position(0, 0)), new FloorCell(new Position(1, 0)),
						new WallCell(new Position(2, 0)), new FloorCell(new Position(3, 0)) },
		});
		Ogre ogre = new Ogre(board, new Position(0, 0), Direction.EAST);
		Set<Position> occupied = new HashSet<>();

		// Placeholder player position, doesn't affect ogre's movement
		Position playerPos = new Position(0, 0);

		// Ensure the ogre follows the exact route, reversing when necessary, instead of
		// looping around
		assertTrue(ogre.getPosition().equals(new Position(0, 0)));
		ogre.updateMovement(playerPos, occupied);
		assertTrue(ogre.getPosition().equals(new Position(1, 0)));
		ogre.updateMovement(playerPos, occupied);
		// here, the ogre should turn and go back instead of walking through the wall
		assertTrue(ogre.getPosition().equals(new Position(0, 0)));
	}

}
