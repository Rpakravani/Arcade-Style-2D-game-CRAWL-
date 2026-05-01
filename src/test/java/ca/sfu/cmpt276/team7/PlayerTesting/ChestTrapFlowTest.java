package ca.sfu.cmpt276.team7.PlayerTesting;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.reward.RegularReward;
import ca.sfu.cmpt276.team7.reward.PunishmentCell;
import ca.sfu.cmpt276.team7.reward.TrapPunishment;
import ca.sfu.cmpt276.team7.Game;
import ca.sfu.cmpt276.team7.EndReason;
import ca.sfu.cmpt276.team7.ScreenState;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Integration test: key → chest → trap → game over (LOSE_BY_TRAP).
 *
 * <p>Engine behaviour exercised:
 * <ul>
 *   <li>Collecting a {@link RegularReward} increments
 *       {@code collectedRegularRewards} and triggers a KEY_COLLECTED popup.</li>
 *   <li>Collecting a chest ({@link RegularReward} with value &gt; 0) applies
 *       score normally — the engine does not distinguish chest from key.</li>
 *   <li>Stepping on a {@link PunishmentCell} with a penalty large enough to
 *       push the player's score below 0 triggers {@code checkLoss()}, which
 *       sets {@link EndReason#LOSE_BY_TRAP} and calls {@code endGame()}
 *       immediately — no trap popup is shown.</li>
 *   <li>Clearing a reward popup requires SPACE + one {@code updateTick()}.
 *       No extra dead tick is needed — {@code resumeFromPopup()} returns
 *       the game directly to {@link ScreenState#PLAYING}.</li>
 * </ul>
 * </p>
 */
public class ChestTrapFlowTest {

    /**
     * Builds a blank 5×5 {@link Board} filled entirely with {@link FloorCell} instances.
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
     * Full flow: collect key → collect chest → step on trap → game ends.
     *
     * <p>Movement path:
     * <pre>
     *   Start (2,2) →[D]→ Key (3,2) →[popup]→ [S]→ Chest (3,3)
     *   →[popup]→ [A]→ Trap (2,3) → END / LOSE_BY_TRAP
     * </pre>
     * </p>
     *
     * <p>Trap penalty is 999, which drops the player well below 0 after
     * collecting only 10 + 20 = 30 points, guaranteeing {@code checkLoss()}
     * fires immediately on the trap tick with no popup.</p>
     */
    @Test
    void testChestThenTrapFlow() {
        Board board = makeBoard();

        Position start    = new Position(2, 2);
        Position keyPos   = new Position(3, 2);
        Position chestPos = new Position(3, 3);
        Position trapPos  = new Position(2, 3);

        board.setStartPosition(start);
        // Key gives 10 points (standard RegularReward value)
        board.setCell(3, 2, new RewardCell(keyPos,   new RegularReward(10)));
        // Chest gives 20 points — engine applies score normally
        board.setCell(3, 3, new RewardCell(chestPos, new RegularReward(20)));
        // Trap penalty of 999 — guaranteed to drop score below 0
        board.setCell(2, 3, new PunishmentCell(trapPos, new TrapPunishment(999)));

        Player player = new Player(board, start);

        Game game = new Game(
                board,
                player,
                List.of(),                    // no enemies
                2,                            // key + chest = 2 regular rewards
                0,
                List.of(keyPos, chestPos),    // reset positions
                List.of(trapPos)
        );

        game.startGame();

        // ── Step 1: collect key at (3,2) ─────────────────────────────────────
        game.handleInput(68); // D → east
        game.updateTick();

        assertEquals(1, game.getCollectedRegularRewards(),
                "Key collection should increment collectedRegularRewards to 1.");
        assertEquals(10, player.getTotalScore(),
                "Key worth 10 points should be applied to score.");

        // Clear KEY_COLLECTED popup: SPACE + one tick
        game.handleInput(32);
        game.updateTick();

        assertEquals(ScreenState.PLAYING, game.getScreenState(),
                "Game should be PLAYING again after clearing the key popup.");

        // ── Step 2: collect chest at (3,3) ───────────────────────────────────
        game.handleInput(83); // S → south
        game.updateTick();

        assertEquals(2, game.getCollectedRegularRewards(),
                "Chest collection should increment collectedRegularRewards to 2.");
        assertEquals(30, player.getTotalScore(),
                "Chest worth 20 points should be applied — total is now 30.");

        // Clear KEY_COLLECTED popup for chest: SPACE + one tick
        game.handleInput(32);
        game.updateTick();

        assertEquals(ScreenState.PLAYING, game.getScreenState(),
                "Game should be PLAYING again after clearing the chest popup.");

        // ── Step 3: step on trap at (2,3) ────────────────────────────────────
        // Trap penalty 999 >> 30 points, so score goes negative → checkLoss fires
        game.handleInput(65); // A → west
        game.updateTick();

        // No popup for trap — engine calls endGame() directly via checkLoss()
        assertEquals(ScreenState.END, game.getScreenState(),
                "Game should be in END state immediately after the trap drops score below 0.");
        assertEquals(EndReason.LOSE_BY_TRAP, game.getEndReason(),
                "End reason should be LOSE_BY_TRAP.");
    }
}