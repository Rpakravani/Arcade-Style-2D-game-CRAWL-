package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.Game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Verifies that the engine correctly expires and removes a bonus reward
 * after its 30-tick lifetime elapses.
 *
 * <p>The engine auto-spawns a bonus reward every 15 ticks when no bonus
 * is currently active, via {@code trySpawnBonusReward()}, and expires it
 * after 30 ticks via {@code updateActiveBonusRewards()}. Rather than
 * asserting on the active list (which may immediately refill at tick 45
 * since 45 is also a multiple of 15), this test asserts that the board
 * cell at the original spawn position is restored to a {@link FloorCell}
 * by {@code clearBonusRewardFromBoard()} — which is guaranteed regardless
 * of whether a new bonus spawns elsewhere on the same tick.</p>
 */
public class ExpiredBonusRewardTest {

    /**
     * Builds a blank 5x5 {@link Board} filled entirely with
     * {@link FloorCell} instances.
     *
     * @return a fresh board ready for test use
     */
    private Board makeBoard() {
        Cell[][] grid = new Cell[5][5];
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                grid[y][x] = new FloorCell(new Position(x, y));
            }
        }
        return new Board(grid);
    }

    /**
     * Confirms that a bonus reward spawned at tick 15 is expired and its
     * board cell restored to {@link FloorCell} by tick 45.
     *
     * <p>Tick-by-tick reasoning:
     * <ul>
     *   <li>Ticks 1–15: at tick 15 ({@code timeElapsed % 15 == 0}),
     *       {@code trySpawnBonusReward()} places a {@link RewardCell} on
     *       the board and adds a {@code BonusRewardSpawn(lifetime=30)} to
     *       the active list.</li>
     *   <li>Ticks 16–44: {@code updateActiveBonusRewards()} calls
     *       {@code tick()} each tick, decrementing lifetime from 30 down
     *       to 1. The spawn is still active at tick 44.</li>
     *   <li>Tick 45: the 30th decrement brings lifetime to 0;
     *       {@code isExpired()} returns true; the spawn is removed and
     *       {@code clearBonusRewardFromBoard()} restores the cell to
     *       {@link FloorCell}.</li>
     * </ul>
     * </p>
     *
     * <p>Win condition is made unreachable by setting
     * {@code totalRegularRewards=1} with no key positions, so the player
     * can never satisfy it and the game stays in PLAYING state throughout.</p>
     */
    @Test
    void testExpiredBonusRewardIsRemovedFromActiveList() {
        Board board = makeBoard();

        Position start = new Position(0, 0);
        Position end   = new Position(4, 4);
        board.setStartPosition(start);
        board.setEndPosition(end);

        Player player = new Player(board, start);

        // totalRegularRewards=1 with no key positions makes win impossible
        Game game = new Game(
                board,
                player,
                List.of(),   // no enemies
                1,           // need 1 key to win — never collectible
                0,           // totalBonusRewards at start
                List.of(),   // no key positions
                List.of()    // no trap positions
        );

        game.startGame();

        // ── Phase 1: drive to tick 15 to trigger auto-spawn ──────────────────
        for (int i = 0; i < 15; i++) {
            game.updateTick();
        }

        assertFalse(game.getBonusRewards().isEmpty(),
                "Engine should have spawned a bonus reward at tick 15.");

        // Record the spawned position — we verify this cell is restored later
        Position bonusPos = game.getBonusRewards().get(0).getPosition();

        assertTrue(board.getCell(bonusPos.getX(), bonusPos.getY()) instanceof RewardCell,
                "Board cell at spawn position should be a RewardCell immediately after spawn.");

        // ── Phase 2: drive to tick 44 (one tick before expiry) ───────────────
        // 29 decrements: lifetime goes from 30 → 1, spawn still active
        for (int i = 0; i < 29; i++) {
            game.updateTick();
        }

        assertFalse(game.getBonusRewards().isEmpty(),
                "Bonus reward should still be active one tick before expiry (tick 44).");

        // ── Phase 3: one final tick to tick 45 — expiry occurs ───────────────
        // 30th decrement: lifetime → 0, isExpired() = true
        // clearBonusRewardFromBoard() restores bonusPos to FloorCell
        game.updateTick();

        // Assert board cell restored — reliable even if a new bonus spawns
        // elsewhere on this same tick (tick 45 = 15 * 3)
        assertTrue(board.getCell(bonusPos.getX(), bonusPos.getY()) instanceof FloorCell,
                "Board cell at expired bonus position should be restored to FloorCell after expiry.");

        assertEquals(0, player.getTotalScore(),
                "Player score should remain 0 — no reward was collected.");
    }
}