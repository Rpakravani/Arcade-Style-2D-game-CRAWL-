package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.BonusReward;
import ca.sfu.cmpt276.team7.reward.BonusRewardSpawn;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.Game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests the timed bonus reward spawning rule.
 *
 * <p>The game attempts to spawn a temporary {@link BonusReward}
 * every 15 ticks, provided that:</p>
 *
 * <ul>
 *     <li>no bonus reward is currently active,</li>
 *     <li>the spawn tile is a valid {@link FloorCell},</li>
 *     <li>the tile is not reserved (start, end, key, trap, enemy),</li>
 *     <li>the tile is not occupied by the player or an enemy.</li>
 * </ul>
 *
 * <p>This test verifies that:</p>
 * <ul>
 *     <li>no bonus reward spawns before tick 15,</li>
 *     <li>a bonus reward spawns exactly at tick 15,</li>
 *     <li>the spawned reward appears on a legal spawn tile,</li>
 *     <li>the board cell becomes a {@link RewardCell} containing a {@link BonusReward}.</li>
 * </ul>
 */
public class BonusRewardSpawnTest { //chest = bonus 

    /** Creates a simple board filled with FloorCells. */
    private Board makeEmptyBoard(int w, int h) {
        Cell[][] grid = new Cell[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                grid[y][x] = new FloorCell(new Position(x, y));
            }
        }
        return new Board(grid);
    }

    @Test
    void testBonusRewardSpawnsAtCorrectInterval() {
        Board board = makeEmptyBoard(5, 5);

        // No end position is set (endPosition == null)
        board.setStartPosition(new Position(0, 0));

        Player player = new Player(board, new Position(0, 0));

        Game game = new Game(
                board,
                player,
                List.of(),      // no enemies
                0,              // no regular rewards
                0,              // no bonus rewards yet
                List.of(),      // no key positions
                List.of()       // no trap positions
        );

        game.startGame();

        // Tick 1–14: no bonus reward should spawn
        for (int i = 1; i < 15; i++) {
            game.updateTick();
            assertTrue(game.getBonusRewards().isEmpty(),
                    "No bonus reward should spawn before tick 15");
        }

        // Tick 15: bonus reward should spawn
        game.updateTick();

        List<BonusRewardSpawn> active = game.getBonusRewards();
        assertEquals(1, active.size(),
                "Exactly one bonus reward should spawn at tick 15");

        BonusRewardSpawn spawned = active.get(0);
        Position pos = spawned.getPosition();

        // The board cell at the spawn position must be a RewardCell with a BonusReward
        Cell cell = board.getCell(pos.getX(), pos.getY());
        assertTrue(cell instanceof RewardCell,
                "Spawned bonus reward must appear as a RewardCell");

        RewardCell rewardCell = (RewardCell) cell;
        assertTrue(rewardCell.getReward() instanceof BonusReward,
                "RewardCell must contain a BonusReward");

        // The spawn position must be one of the legal spawn positions
        assertTrue(game.getBonusSpawnPositions().contains(pos),
                "Spawned bonus reward must appear on a legal spawn tile");
    }
}
