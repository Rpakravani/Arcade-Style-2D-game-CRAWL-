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

class GoblinIntegrationTest{
    // Goblin Pursuit Flow
    @Test
    void TestGoblinPursuit() {
	// Make board
	Board board = new Board(new Cell[][] {
		{ new FloorCell(new Position(0, 0)), new FloorCell(new Position(1, 0)) , new FloorCell(new Position(2, 0)), new FloorCell(new Position(3, 0))}, 
		{ new FloorCell(new Position(0, 1)), new WallCell(new Position(1, 1)) , new PunishmentCell(new Position(2, 1), new TrapPunishment(1)), new FloorCell(new Position(3, 1))},
		{ new FloorCell(new Position(0, 2)), new FloorCell(new Position(1, 2)) , new FloorCell(new Position(2, 2)), new FloorCell(new Position(3, 2))},

	    });

	Player player = new Player(board, new Position(0, 0));
	Goblin goblin = new Goblin(board, new Position(0, 2));

	// === Spawn enemies ===
	ArrayList<Enemy> enemies = new ArrayList<>();

	enemies.add(goblin);

	Game game = new Game(board, player, enemies, 0, 0, new ArrayList<>(), new ArrayList<>());
	game.startGame();

	// Test goblin against real player, using real game ticks
	
	// Goblin should chase player around pillar

	// Verify initial pos
	player.setPosition(new Position(0, 0));
	assertTrue(player.getPosition().equals(new Position(0, 0)));
	assertTrue(goblin.getPosition().equals(new Position(0, 2)));

	// Player moves, goblin chases
	player.setPosition(new Position(1, 0));
	game.updateTick();
	assertTrue(goblin.getPosition().equals(new Position(0, 1)));

	// Player moves, goblin chases
	player.setPosition(new Position(2, 0));
	game.updateTick();
	assertTrue(goblin.getPosition().equals(new Position(0, 0)));

	// Player moves, goblin chases
	player.setPosition(new Position(3, 0));
	game.updateTick();
	assertTrue(goblin.getPosition().equals(new Position(1, 0)));

	// an ogre spawns, blocking the path
	Ogre ogre = new Ogre(board, new Position(2, 0), Direction.NORTH);
	enemies.add(ogre);
	
	// Player moves, goblin should recognize the ogre blocking the path, and move backwards around
	player.setPosition(new Position(3, 1));
	game.updateTick();
	assertTrue(goblin.getPosition().equals(new Position(0, 0)));

	}
}
