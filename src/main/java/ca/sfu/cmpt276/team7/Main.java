package ca.sfu.cmpt276.team7;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List; 

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.board.BoardLoader;
import ca.sfu.cmpt276.team7.core.Direction;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.enemies.Enemy;
import ca.sfu.cmpt276.team7.enemies.Goblin;
import ca.sfu.cmpt276.team7.enemies.Ogre;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.Punishment;
import ca.sfu.cmpt276.team7.reward.PunishmentCell;
import ca.sfu.cmpt276.team7.reward.RegularReward;
import ca.sfu.cmpt276.team7.reward.Reward;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.reward.TrapPunishment;
import ca.sfu.cmpt276.team7.ui.GameWindow;

/**
 * Entry point for the game application
 * 
 * <p>This class is responsible for bootstrapping the game world at startup
 * It loads the map file, creates the board and player, spawns enemies,
 * places key rewards and trap punishments onto the board, constructs the
 * {@link Game} controller, and launches the graphical user interface
 * 
 * <p>The startup flow is:
 * <ol>
 *   <li>Load the board and marker positions from the map file</li>
 *   <li>Create the player at the board's start position</li>
 *   <li>Spawn goblins and ogres at their configured positions</li>
 *   <li>Place reward cells for keys and punishment cells for traps</li>
 *   <li>Create the {@link Game} instance with the initialized objects</li>
 *   <li>Start the {@link GameWindow}</li>
 * </ol>
 */
public class Main {

    /**
     * Starts the game application
     * 
     * <p>This method loads the map data from the resource file, initializes all
     * core game objects, and opens the game window. Marker positions produced
     * by {@link BoardLoader} are used to spawn enemies and place special cells
     * such as rewards and punishments
     * 
     * <p>If the map file cannot be loaded, the resulting {@link IOException}
     * is caught and printed to the console
     * 
     * @param args command-line arguments that are not used by this application
     */
    public static void main(String[] args) {
        try {
            BoardLoader.Result result = BoardLoader.load(Path.of("./src/main/resources/maps/map3.txt"));

            Board board = result.getBoard();
            List<Position> goblin_spawns = result.getGoblinSpawns();
            List<Position> ogre_spawns = result.getOgreSpawns();
            List<Position> key_spawns = result.getKeyPositions();
            List<Position> trap_spawns = result.getTrapPositions();

            Player player = new Player(board, board.getStartPosition());

            // === Spawn enemies ===
            ArrayList<Enemy> enemies = new ArrayList<>();

            for (Position pos : goblin_spawns) {
                enemies.add(new Goblin(board, pos));
            }

            for (Position pos : ogre_spawns) {
                enemies.add(new Ogre(board, pos, Direction.EAST));
            }

            // === Spawn keys ===
            for (Position pos : key_spawns) {
                Reward reward = new RegularReward(10);
                board.setCell(pos.getX(), pos.getY(), new RewardCell(pos, reward));
            }

            // === Spawn traps ===
            for (Position pos : trap_spawns) {
                Punishment trap = new TrapPunishment(5);
                board.setCell(pos.getX(), pos.getY(), new PunishmentCell(pos, trap));
            }

            // Bonus rewards are runtime-spawned, so initial total is 0.
            Game game = new Game(board, player, enemies, key_spawns.size(), 0, key_spawns, trap_spawns);

            GameWindow.start(game, board);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}