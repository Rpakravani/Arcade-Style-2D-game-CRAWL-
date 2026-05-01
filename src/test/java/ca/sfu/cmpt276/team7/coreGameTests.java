package ca.sfu.cmpt276.team7;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.board.BoardLoader;
import ca.sfu.cmpt276.team7.cells.FloorCell;
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
import ca.sfu.cmpt276.team7.ScreenState;
import ca.sfu.cmpt276.team7.EndReason;
import ca.sfu.cmpt276.team7.PopupReason;

/**
 * Test suite for core game logic
 */
public class coreGameTests 
{

    private Game game;
    private Player player;
    private Board board;
    private List<Enemy> enemies;
    /**
     * Sets up game application for testing
     * 
     * <p>This method loads the map data from the resource file, initializes all
     * core game objects. Marker positions produced by {@link BoardLoader} 
     * are used to spawn enemies and place special cells such as rewards and punishments
     * 
     * <p>If the map file cannot be loaded, the resulting {@link IOException}
     * is caught and printed to the console
     * 
     * <p>Method is called before every test using {@BeforeEach}
     * 
     */
    @BeforeEach
    public void setUp() throws IOException 
    {
        BoardLoader.Result result = BoardLoader.load(Path.of("src/test/resources/maps/valid/map1.txt"));

        board = result.getBoard();
        List<Position> goblin_spawns = result.getGoblinSpawns();
        List<Position> ogre_spawns = result.getOgreSpawns();
        List<Position> key_spawns = result.getKeyPositions();
        List<Position> trap_spawns = result.getTrapPositions();

        player = new Player(board, board.getStartPosition());

        // === Spawn enemies ===
        enemies = new ArrayList<>();

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
        game = new Game(board, player, enemies, key_spawns.size(), 0, key_spawns, trap_spawns);
    }

    /**
     * set up but takes path as a parameter
     * @param mapPath
     * @throws IOException
     */
    public void setUpWithPath(String mapPath) throws IOException 
    {
        BoardLoader.Result result = BoardLoader.load(Path.of(mapPath));

        board = result.getBoard();
        List<Position> goblin_spawns = result.getGoblinSpawns();
        List<Position> ogre_spawns = result.getOgreSpawns();
        List<Position> key_spawns = result.getKeyPositions();
        List<Position> trap_spawns = result.getTrapPositions();

        player = new Player(board, board.getStartPosition());

        // === Spawn enemies ===
        enemies = new ArrayList<>();

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
        game = new Game(board, player, enemies, key_spawns.size(), 0, key_spawns, trap_spawns);
    }

    /**
     * UNIT TESTS
     */

    /**
     * Tests that game initializes screen state to START
     */
    @Test
    public void initialScreenStateTest()
    {
        assertEquals(ScreenState.START, game.getScreenState(), "Screen State not initialized to start screen");
    }

    /**
     * Tests that game tick system increases after player gives valid input and 
     * player position changes
     */
    @Test
    public void validInputTest()
    {
        game.startGame();
        Position initPlayerPos = player.getPosition();
        game.handleInput(68); //W
        game.updateTick();
        assertEquals(1, game.getTimeElapsed(), "Tick not increasing after valid input");
        assertNotEquals(initPlayerPos, player.getPosition(), "Player position did not change after valid input");
    }

    /**
     * Tests that player position does not change after an invalid input
     */
    @Test
    public void invalidInputTest()
    {
        game.startGame();
        Position initPlayerPos = player.getPosition();
        game.handleInput(999);
        game.updateTick();
        assertEquals(initPlayerPos, player.getPosition(), "Player position changed after invalid input");
    }

    /**
     * Tests that player is able to collect keys, score increases after collecting regular reward
     *  and popup for collection appears
     */
    @Test
    public void scoreIncreaseOnRegularRewardCollectTest()
    {
        game.startGame();
        player.setPosition(new Position(3, 1)); //left of key 1
        game.handleInput(68); //D
        game.updateTick();
        assertEquals(1, game.getCollectedRegularRewards(), "K1 not collected");
        assertEquals(PopupReason.KEY_COLLECTED, game.getPopupReason(), "Key Popup did not appear");
        assertEquals(10, game.getDisplayedScore(), "Score did not increase after K1 was collected");
    }

    /**
     * Tests that player is able to win game by reaching exit with all keys collected
     */
    @Test
    public void winConditionTest()
    {
        game.startGame();
        player.setPosition(new Position(3, 1)); //left of key 1
        game.handleInput(68); //D
        game.updateTick();
        assertEquals(1, game.getCollectedRegularRewards(), "K1 not collected");
        assertEquals(PopupReason.KEY_COLLECTED, game.getPopupReason(), "Key Popup did not appear");

        if (game.getScreenState() == ScreenState.PAUSE)
        {
            game.handleInput(32); //Space
        }

        player.setPosition(new Position(4, 3)); //left of key 2
        game.handleInput(68); //D
        game.updateTick();
        assertEquals(2, game.getCollectedRegularRewards(), "K2 not collected");
        assertEquals(PopupReason.KEY_COLLECTED, game.getPopupReason(), "Key Popup did not appear");

        if (game.getScreenState() == ScreenState.PAUSE)
        {
            game.handleInput(32); //Space
        }

        player.setPosition(board.getEndPosition());
        game.updateTick();

        assertEquals(ScreenState.END, game.getScreenState(), "Screen state did not change to end");
        assertEquals(EndReason.WIN, game.getEndReason(), "End Reason is not win");
    }

    /**
     * Tests that player isnt able to exit without all keys
     */
    @Test
    public void exitWithoutKeysTest()
    {
        game.startGame();
        player.setPosition(new Position(8, 4)); //exit
        game.handleInput(68);
        game.updateTick();

        assertEquals(ScreenState.PLAYING, game.getScreenState(), "Screen state changed when walked to exit");
    }

    /**
     * Tests that getting caught by goblin causes a loss in score/game over and end reason is
     * lose by goblin
     */
    @Test
    public void loseByGoblinTest()
    {
        game.startGame();
        player.setPosition(new Position(6, 2)); //above goblin
        game.updateTick();
        assertTrue(game.getDisplayedScore() <= 0, "Score did not decrease after being caught by goblin");
        assertEquals(ScreenState.END, game.getScreenState(), "Screen state did not change to end when caught by goblin");
        assertEquals(EndReason.LOSE_BY_GOBLIN, game.getEndReason(), "End reason did not change to lose by goblin");
    }

    /**
     * Tests that walking over trap cell causes loss in score/game over and 
     * end reason is lose by trap
     */
    @Test
    public void loseByTrapsTest()
    {
        game.startGame();
        player.setPosition(new Position(6, 2)); //left of trap
        game.handleInput(68); //D
        game.updateTick();
        assertTrue(game.getDisplayedScore() <= 0, "Score did not decrease after walking over trap");
        assertEquals(ScreenState.END, game.getScreenState(), "Screen state did not change to end when caught in trap");
        assertEquals(EndReason.LOSE_BY_TRAP, game.getEndReason(), "End reason did not change to lose by trap");
    }

    /**
     * Tests that being caught by ogre causes loss in score/game over and
     * end reason is lose by ogre
     */
    @Test
    public void loseByOgreTest()
    {
        game.startGame();
        player.setPosition(new Position(6, 4)); //left of ogre
        game.updateTick();
        assertTrue(game.getDisplayedScore() <= 0, "Score did not decrease after being caught by ogre");
        assertEquals(ScreenState.END, game.getScreenState(), "Screen state did not change to end when caught by ogre");
        assertEquals(EndReason.LOSE_BY_OGRE, game.getEndReason(), "End reason did not change to lose by ogre");
    }

    /**
     * Tests that game is able to be paused and ticks do not increase when 
     * game is paused
     */
    @Test
    public void pauseFreezeTickTest()throws InterruptedException
    {
        game.startGame();
        game.handleInput(80); //P
        Thread.sleep(500);
        assertEquals(ScreenState.PAUSE, game.getScreenState(), "Screen state did not change to pause when P key was pressed");
        assertEquals(0, game.getTimeElapsed(), "Game time continued to elapse after pause");
    }

    /**
     * Tests that game is able to resume from pause state
     */
    @Test
    public void resumeFromPauseTest()
    {
        game.startGame();
        game.handleInput(80); //P
        game.handleInput(32); //Space
        assertEquals(ScreenState.PLAYING, game.getScreenState(), "Game did not un-pause after space key pressed");
    }

    /**
     * Tests that seconds do not elaspe while game is paused
     */
    @Test
    public void pauseFreezeAccumulatedTimeTest() throws InterruptedException
    {
        game.startGame();
        game.handleInput(80); //P
        Thread.sleep(500);
        game.handleInput(32); //P
        assertTrue(game.getSeconds() < 1, "Game time continued to elapse after pause");
    }

    /**
     * Tests that popupReason starting value is null
     */
    @Test
    public void noPopupOnStartTest()
    {
        game.startGame();
        assertNull(game.getPopupReason(), "PopupReason did not initialize as null");
    }

    /**
     * Tests that list of bonus reward spawn positions is not empty
     */
    @Test
    public void bonusSpawnPositionsNotEmptyTest()
    {
        assertFalse(game.getBonusSpawnPositions().isEmpty(), "Bonus spawn positions initialized empty");
    }

    /**
     * Tests that bouns spawn positions are not on start position or end position
     */
    @Test
    public void bonusSpawnPositionsNotStartOrEndTest()
    {
        List<Position> spawns = game.getBonusSpawnPositions();
        assertFalse(spawns.contains(board.getStartPosition()), "Bonus spawn positions contains starting position");
        assertFalse(spawns.contains(board.getEndPosition()), "Bonus spawn positions contains end position");
    }

    /**
     * Tests that bonus reward spawns after 15 ticks, then despawns and respawns after
     * 30 ticks
     * @throws IOException
     */
    @Test
    public void bonusSpawnLifetimeTest() throws IOException
    {
        setUpWithPath("src/test/resources/maps/valid/map_bonusTest.txt");
        game.startGame();

        for(int i = 0;i<15;i++)
        {
            game.updateTick();
        }

        assertFalse(game.getBonusRewards().isEmpty(), "Bonus should have spawned at tick 15");
        
        for(int j = 0;j<30;j++)
        {
            game.updateTick();
        }
        
        assertEquals(2, game.getTotalBonusRewards(), "Old bonus should have despawned, replaced with new one");
    }

    /**
     * Tests that bonus reward is able to be collected, score increases after 
     * bonus reward is collected and bonus reward popup is shown after collected
     * @throws IOException
     */
    @Test
    public void scoreIncreaseOnBonusRewardCollectTest() throws IOException
    {
        setUpWithPath("src/test/resources/maps/valid/map_bonusTest.txt");
        game.startGame();

        for(int i = 0;i<15;i++)
        {
            game.updateTick();
        }

        Position bonusPos = game.getBonusRewards().get(0).getPosition();
        int bx = bonusPos.getX();
        int by = bonusPos.getY();

        player.setPosition(new Position(bx - 1, by));
        game.handleInput(68);
        game.updateTick();

        assertTrue(game.getDisplayedScore() > 0);
        assertEquals(1, game.getCollectedBonusRewards(), "B1 not collected");
        assertEquals(PopupReason.BONUS_COLLECTED, game.getPopupReason(), "Bonus Popup did not appear");
    }

    /**
     * Tests that ticks does not increase after game has ended
     */
    @Test
    public void noTickAfterGameEndTest()
    {
        game.startGame();
        player.setPosition(new Position(6, 2)); //above goblin
        game.updateTick();
        int ticksAtEnd = game.getTimeElapsed();
        game.updateTick();
        assertEquals(ticksAtEnd, game.getTimeElapsed(), "Ticks continued to increase after game end");
    }

    /**
     * INTERGRATION TESTS
     */

    /**
     * Tests that game is able to pause and un-pause, then continue to 
     * tick afterwards and have enemies positions change after tick
     */
    @Test
    public void pauseAndResumeMidGameTest()
    {
        game.startGame();
        Position enemyPosBefore = enemies.get(0).getPosition();

        game.handleInput(80);
        assertEquals(ScreenState.PAUSE, game.getScreenState(), "Game did not pause after P key was pressed");

        game.handleInput(32);
        assertEquals(ScreenState.PLAYING, game.getScreenState(), "Game did not un-pause after P key");

        game.updateTick();
        assertEquals(1, game.getTimeElapsed(), "Game did not continue to tick after un-paused");

        Position enemyPosAfter = enemies.get(0).getPosition();
        assertNotEquals(enemyPosAfter, enemyPosBefore, "enemies did not move after tick increase");
    }

    /**
     * Tests that game is able to reset all rewards/reward count, tick count, 
     * seconds elapsed, player postion and enemies postions
     */
    @Test
    public void restartGameTest()
    {
        game.startGame();

        Position enemyInitPos = enemies.get(0).getPosition();

        player.setPosition(new Position(6, 2)); //above goblin
        game.updateTick();
        game.handleInput(32); //Space

        assertEquals(ScreenState.PLAYING, game.getScreenState(), "Game did not enter playing screen state after pressing space key on end screen");
        assertEquals(0, game.getTimeElapsed(), "Time elapsed did not reset");
        assertEquals(0, game.getSeconds());
        assertEquals(0, game.getCollectedRegularRewards(), "collected rewards did not reset");
        assertEquals(0, game.getCollectedBonusRewards(), "collected bonus rewards did not reset");
        assertEquals(board.getStartPosition(), player.getPosition(), "Player position did not reset to start");
        assertTrue(board.getCell(4, 1) instanceof RewardCell, "Reward cells did not reset");
        assertEquals(enemyInitPos, enemies.get(0).getPosition(), "Enemies positions did not reset");
    }
    
    /**
     * Tests that enemies still change position after player enters invalid input
     */
    @Test
    public void invalidInputEnemyStillMovesTest()
    {
        game.startGame();

        Position enemyInitPos = enemies.get(0).getPosition();
        Position playerInitPos = player.getPosition();

        game.handleInput(999);
        game.updateTick();
    
        assertEquals(player.getPosition(), playerInitPos, "Player position changed after invalid input");
        assertNotEquals(enemies.get(0).getPosition(), enemyInitPos, "Enemies positions did not change after tick increase");
    }

    /**
     * Tests that reward cell changes to floor cell after reward is collected
     */
    @Test
    public void rewardCellChangesAfterCollectTest()
    {
        game.startGame();
        player.setPosition(new Position(3, 1)); //left of key 1
        game.handleInput(68); //D
        game.updateTick();

        if (game.getScreenState() == ScreenState.PAUSE)
        {
            game.handleInput(32); //Space
        }

        assertTrue(board.getCell(4, 1) instanceof FloorCell, "Reward cell did not change to floor cell after reward collected");
    }

    /**
     * Tests that ogre popup appears when player is caught by ogre and score
     * does not drop to 0 or lower
     */
    @Test
    public void popupAfterOgreHitTest()
    {
        game.startGame();
        player.setScore(100);
        player.setPosition(new Position(6, 4)); //left of ogre
        game.updateTick();

        assertEquals(PopupReason.OGRE_HIT, game.getPopupReason(), "PopupReason did not change to ogre hit");
        assertEquals(ScreenState.PAUSE, game.getScreenState(), "Game did not pause for popup after player got hit by ogre");
    }
}
