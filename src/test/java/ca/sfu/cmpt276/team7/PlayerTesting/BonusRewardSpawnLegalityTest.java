package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.BonusReward;
import ca.sfu.cmpt276.team7.reward.BonusRewardSpawn;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.reward.RegularReward;
import ca.sfu.cmpt276.team7.reward.PunishmentCell;
import ca.sfu.cmpt276.team7.reward.TrapPunishment;
import ca.sfu.cmpt276.team7.Game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests the legality rules for bonus reward spawning.
 *
 * <p>A bonus reward may only spawn on tiles that satisfy:</p>
 * <ul>
 *     <li>the tile is a {@link FloorCell},</li>
 *     <li>the tile is not the start tile,</li>
 *     <li>the tile is not the end tile (null in this test),</li>
 *     <li>the tile is not an initial key position,</li>
 *     <li>the tile is not an initial trap position,</li>
 *     <li>the tile is not an initial enemy position,</li>
 *     <li>the tile is not currently occupied by the player.</li>
 * </ul>
 *
 * <p>This test ensures that the bonus reward spawns only on tiles
 * included in {@link Game#getBonusSpawnPositions()}, which already
 * encodes all legality constraints.</p>
 */
public class BonusRewardSpawnLegalityTest {

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
    void testBonusRewardSpawnsOnlyOnLegalTiles() {
        Board board = makeEmptyBoard(5, 5);

        // Start tile (illegal for spawning)
        Position start = new Position(0, 0);
        board.setStartPosition(start);

        // Key tile (illegal)
        Position keyPos = new Position(1, 1);
        board.setCell(1, 1, new RewardCell(keyPos, new RegularReward(10)));

        // Trap tile (illegal)
        Position trapPos = new Position(2, 2);
        board.setCell(2, 2, new PunishmentCell(trapPos, new TrapPunishment(5)));

        // Player starts on start tile
        Player player = new Player(board, start);

        Game game = new Game(
                board,
                player,
                List.of(),                 // no enemies
                1,                         // one key
                0,                         // no bonus rewards yet
                List.of(keyPos),           // initial key
                List.of(trapPos)           // initial trap
        );

        game.startGame();

        // Advance to tick 15 to force a spawn attempt
        for (int i = 0; i < 15; i++) {
            game.updateTick();
        }

        // One bonus reward should now exist
        List<BonusRewardSpawn> active = game.getBonusRewards();
        assertEquals(1, active.size(),
                "A bonus reward should spawn at tick 15");

        BonusRewardSpawn spawned = active.get(0);
        Position pos = spawned.getPosition();

        // The spawn position must be one of the legal positions
        assertTrue(game.getBonusSpawnPositions().contains(pos),
                "Bonus reward must spawn on a legal tile");

        // The board cell must be a RewardCell containing a BonusReward
        Cell cell = board.getCell(pos.getX(), pos.getY());
        assertTrue(cell instanceof RewardCell,
                "Spawned bonus reward must appear as a RewardCell");

        RewardCell rewardCell = (RewardCell) cell;
        assertTrue(rewardCell.getReward() instanceof BonusReward,
                "RewardCell must contain a BonusReward");

        // Ensure it did NOT spawn on illegal tiles
        assertNotEquals(start, pos, "Bonus reward must not spawn on start tile");
        assertNotEquals(keyPos, pos, "Bonus reward must not spawn on key tile");
        assertNotEquals(trapPos, pos, "Bonus reward must not spawn on trap tile");
    }
}
