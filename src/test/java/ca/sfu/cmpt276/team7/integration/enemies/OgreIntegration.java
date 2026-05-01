package ca.sfu.cmpt276.team7.integration.enemies;

import ca.sfu.cmpt276.team7.core.Direction;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.cells.WallCell;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.reward.PunishmentCell;
import ca.sfu.cmpt276.team7.reward.TrapPunishment;
import ca.sfu.cmpt276.team7.enemies.Goblin;
import ca.sfu.cmpt276.team7.enemies.Ogre;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.enemies.Enemy;
import ca.sfu.cmpt276.team7.Game;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class OgreIntegrationTest{
    // Ogre Patrol Flow
    @Test
    void TestOgrePatrolFlow() {
	// Make board
	Board board = new Board(new Cell[][] {
			{	 new FloorCell(new Position(0, 0)), new FloorCell(new Position(1, 0)) , new FloorCell(new Position(2, 0)), new FloorCell(new Position(3, 0))}, 
			{	 new FloorCell(new Position(0, 1)), new WallCell(new Position(1, 1)) , new PunishmentCell(new Position(2, 1), new TrapPunishment(1)), new FloorCell(new Position(3, 1))},
			{	 new FloorCell(new Position(0, 2)), new FloorCell(new Position(1, 2)) , new FloorCell(new Position(2, 2)), new FloorCell(new Position(3, 2))},

	    });

	Player player = new Player(board, new Position(3, 2));
	Ogre ogre = new Ogre(board, new Position(0, 0), Direction.EAST);

	// === Spawn enemies ===
	ArrayList<Enemy> enemies = new ArrayList<>();

	enemies.add(ogre);

	Game game = new Game(board, player, enemies, 0, 0, new ArrayList<>(), new ArrayList<>());
	game.startGame();

	// Test goblin against real player, using real game ticks
	

	// Verify initial pos
	player.setPosition(new Position(3, 2));
	assertTrue(player.getPosition().equals(new Position(3, 2)));
	assertTrue(ogre.getPosition().equals(new Position(0, 0)));

	// Ogre moves east on game tick
	game.updateTick();
	assertTrue(ogre.getPosition().equals(new Position(1, 0)));

	// Ogre moves east on game tick
	game.updateTick();
	assertTrue(ogre.getPosition().equals(new Position(2, 0)));

	// Ogre moves east on game tick
	game.updateTick();
	assertTrue(ogre.getPosition().equals(new Position(3, 0)));

	// Ogre turns around when hitting edge of board
	game.updateTick();
	assertTrue(ogre.getPosition().equals(new Position(2, 0)));

	// This ogre should be stuck
	Ogre ogre2 = new Ogre(board, new Position(1, 0), Direction.NORTH);
	enemies.add(ogre2);
	// Verify position
	assertTrue(ogre2.getPosition().equals(new Position(1, 0)));

	// Ogre1 turns around, blocked by ogre 2
	game.updateTick();
	// ogre 2 is stuck between the edge of the board and the wall, and can't move
	assertTrue(ogre2.getPosition().equals(new Position(1, 0)));
	assertTrue(ogre.getPosition().equals(new Position(3, 0)));





	}
}
