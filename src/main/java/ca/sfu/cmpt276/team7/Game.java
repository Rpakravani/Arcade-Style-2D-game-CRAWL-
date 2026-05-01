package ca.sfu.cmpt276.team7;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.function.LongSupplier;
import ca.sfu.cmpt276.team7.board.Board;
import ca.sfu.cmpt276.team7.cells.Cell;
import ca.sfu.cmpt276.team7.cells.FloorCell;
import ca.sfu.cmpt276.team7.core.Direction;
import ca.sfu.cmpt276.team7.core.GameCharacter;
import ca.sfu.cmpt276.team7.core.Position;
import ca.sfu.cmpt276.team7.enemies.Enemy;
import ca.sfu.cmpt276.team7.enemies.Goblin;
import ca.sfu.cmpt276.team7.enemies.Ogre;
import ca.sfu.cmpt276.team7.reward.BonusReward;
import ca.sfu.cmpt276.team7.reward.BonusRewardSpawn;
import ca.sfu.cmpt276.team7.reward.Player;
import ca.sfu.cmpt276.team7.reward.PunishmentCell;
import ca.sfu.cmpt276.team7.reward.RegularReward;
import ca.sfu.cmpt276.team7.reward.Reward;
import ca.sfu.cmpt276.team7.reward.RewardCell;
import ca.sfu.cmpt276.team7.reward.TrapPunishment;

/**
 * Core controller for the game.
 *
 * <p>Coordinates the main gameplay loop, player input, enemy updates,
 * reward and punishment interactions, timed bonus reward spawning,
 * pause and popup transitions, and win/loss detection</p>
 *
 * <p>It also manages score-related counters, elapsed game time,
 * reset/start behaviour, and end-of-game state transitions</p>
 *
 * <p>Collaborates with:
 * <ul>
 *  <li>{@link Board} - supplies the dungeon layout and important positions</li>
 *  <li>{@link Player} - handles movement, score changes, and reward collection</li>
 *  <li>{@link Enemy} - updates enemy movement each tick</li>
 *  <li>{@link BonusRewardSpawn} - tracks temporary bonus reward lifetime on the board</li>
 * </ul>
 */
public class Game 
{
    /** How often the game tries to spawn a bonus reward. */
    private static final int BONUS_SPAWN_INTERVAL_TICKS = 15;

    /** How long a spawned bonus reward stays on the board. */
    private static final int BONUS_SPAWN_LIFETIME_TICKS = 30;

    /** Score value of a spawned bonus reward. */
    private static final int BONUS_REWARD_VALUE = 25;

    /** Duration field stored inside BonusReward when collected. */
    private static final int BONUS_EFFECT_DURATION_TICKS = 20;

    /** Score penalty applied when the player is hit by an ogre. */
    private static final int OGRE_HIT_PENALTY = 25;

    private Direction pendingMove = null;

    /** Remaining ticks before another ogre collision can apply damage. */
    private int ogreHitCooldown = 0;

    /**
     * number of ticks since game start
     * incremented once per call to {@link #updateTick()}
     */
    private int timeElapsed;

    /** Total number of regular rewards on the board. */
    private int totalRegularRewards;

    /** Number of regular rewards the player has collected so far. */
    private int collectedRegularRewards;

    /** Total number of bonus rewards spawned during the run. */
    private int totalBonusRewards;

    /** Number of bonus rewards the player has collected so far */
    private int collectedBonusRewards;

    /** The current screen state of the game. */
    private ScreenState screenState;

    /** The reason the game ended, set when screenState becomes END. */
    private EndReason endReason;

    /** The reason a popup is currently being shown. */
    private PopupReason popupReason;

    /**Dungeon board, provides layout and positions */
    private Board board;

    /**Player character controlled with keyboard input */
    private Player player;

    /**All enemy instances present on board */
    private List<Enemy> enemies;

    /**Elapsed time from start */
    private long startTime;

    /**Total time elapsed */
    private long totalTime;

    /**Active bonus rewards currently on board */
    private List<BonusRewardSpawn> bonusRewards;

    /** Cached legal positions where a bonus reward may spawn. */
    private final List<Position> bonusSpawnPositions;

    /** Random generator for choosing spawn tiles. */
    private final Random random;

    /**Initial positions of enemies, keys and traps, used for reset */ 
    private final List<Position> initialEnemyPos;
    private final List<Position> initialKeyPos;
    private final List<Position> initialTrapPos;

    /** Time source used for elapsed-time calculations. */
    private final LongSupplier clock;

    /**
     * Constructs a new game controller with the given board, player, enemies,
     * and initial reward/trap metadata
     *
     * <p>This constructor initializes runtime counters, stores the original
     * positions of enemies, keys, and traps for later reset, and precomputes
     * the legal positions where temporary bonus rewards may spawn</p>
     *
     * @param board dungeon board used for gameplay
     * @param player player controlled during the game
     * @param enemies enemy instances active on the board
     * @param totalRegularRewards total number of regular rewards placed initially
     * @param totalBonusRewards total number of bonus rewards spawned so far at startup
     * @param keyPositions initial regular reward positions used for reset
     * @param trapPositions initial trap positions used for reset
     */
    public Game(Board board, Player player, List<Enemy> enemies, int totalRegularRewards, int totalBonusRewards,
                List<Position> keyPositions, List<Position> trapPositions) {
        this(board, player, enemies, totalRegularRewards, totalBonusRewards,
             keyPositions, trapPositions, System::currentTimeMillis);
    }

    /**
     * Constructs a new game controller with the given board, player, enemies,
     * initial reward/trap metadata, and time source.
     *
     * <p>This constructor initializes runtime counters, stores the original
     * positions of enemies, keys, and traps for later reset, and precomputes
     * the legal positions where temporary bonus rewards may spawn.</p>
     *
     * <p>The supplied {@code clock} is used for elapsed-time calculations,
     * which allows tests to control the flow of time deterministically.</p>
     *
     * @param board dungeon board used for gameplay
     * @param player player controlled during the game
     * @param enemies enemy instances active on the board
     * @param totalRegularRewards total number of regular rewards placed initially
     * @param totalBonusRewards total number of bonus rewards spawned so far at startup
     * @param keyPositions initial regular reward positions used for reset
     * @param trapPositions initial trap positions used for reset
     * @param clock time source used for elapsed-time calculations
     * @throws IllegalArgumentException if {@code clock} is {@code null}
     */
    public Game(Board board, Player player, List<Enemy> enemies, int totalRegularRewards, int totalBonusRewards,
                List<Position> keyPositions, List<Position> trapPositions, LongSupplier clock)
    {
        if (clock == null) {
            throw new IllegalArgumentException("clock cannot be null");
        }

        this.board = board;
        this.player = player;
        this.enemies = enemies;
        this.timeElapsed = 0;
        this.totalRegularRewards = totalRegularRewards;
        this.collectedRegularRewards = 0;
        this.totalBonusRewards = totalBonusRewards;
        this.collectedBonusRewards = 0;
        this.screenState = ScreenState.START;
        this.endReason = null;
        this.popupReason = null;
        this.bonusRewards = new ArrayList<>();
        this.random = new Random();
        this.clock = clock;

        this.initialKeyPos = new ArrayList<>(keyPositions);
        this.initialTrapPos = new ArrayList<>(trapPositions);

        this.initialEnemyPos = new ArrayList<>();
        for (Enemy enemy : enemies) 
        {
            this.initialEnemyPos.add(enemy.getPosition());
        }

        this.bonusSpawnPositions = buildBonusSpawnPositions();
    }

    /**
     * Starts or restarts active gameplay from the current reset state
     *
     * <p>This resets elapsed time counters, clears popup/end information,
     * resets collected reward counters, clears any tracked bonus reward spawns,
     * and places the player at the board's start position</p>
     */
    public void startGame()
    {
        startTime = clock.getAsLong();
        totalTime = 0;

        pendingMove = null;
        ogreHitCooldown = 0;
        collectedRegularRewards = 0;
        collectedBonusRewards = 0;
        totalBonusRewards = 0;

        timeElapsed = 0;
        screenState = ScreenState.PLAYING;
        endReason = null;
        popupReason = null;
        player.setPosition(board.getStartPosition());
        bonusRewards.clear();
    }

    /**
     * Restores the game to its initial pre-play state
     *
     * <p>This clears runtime counters, removes any active reward or punishment
     * cells currently on the board, restores the original regular rewards and traps,
     * resets enemy positions, clears temporary bonus reward tracking, and resets
     * the player state back to the start position.</p>
     */
    public void resetGameState() 
    {
        startTime = 0;
        timeElapsed = 0;
        totalTime = 0;
        pendingMove = null;
        ogreHitCooldown = 0;
        collectedRegularRewards = 0;
        collectedBonusRewards = 0;
        totalBonusRewards = 0;
        bonusRewards.clear();
        screenState = ScreenState.START;
        endReason = null;
        popupReason = null;

        player.resetState(board.getStartPosition());

        for (int y = 0; y < board.getHeight(); y++) 
        {
            for (int x = 0; x < board.getWidth(); x++) 
            {
                Position pos = new Position(x, y);
                Cell cell = board.getCell(x, y);

                if (cell instanceof RewardCell || cell instanceof PunishmentCell) 
                {
                    board.setCell(x, y, new FloorCell(pos));
                }
            }
        }
        
        for (Position pos : initialKeyPos) {
            board.setCell(pos.getX(), pos.getY(),
                new RewardCell(pos, new RegularReward(10)));
        }

        for (Position pos : initialTrapPos) {
            board.setCell(pos.getX(), pos.getY(),
                new PunishmentCell(pos, new TrapPunishment(5)));
        }

        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);

            if (enemy instanceof Ogre ogre) {
                ogre.resetState();
            } else {
                enemy.setPosition(initialEnemyPos.get(i));
            }
        }
    }

    /**
     * Advances the game by one simulation tick.
     *
     * <p>When the game is in {@link ScreenState#PLAYING}, each tick:
     * <ol>
     *  <li>decrements the ogre-hit cooldown if active</li>
     *  <li>increments {@link #timeElapsed}</li>
     *  <li>applies any pending player movement and processes any collected reward</li>
     *  <li>checks immediate ogre and loss conditions after player movement</li>
     *  <li>updates any active player bonus effect</li>
     *  <li>updates enemy movement</li>
     *  <li>checks ogre, loss, and win conditions after enemy movement</li>
     *  <li>updates active temporary bonus reward lifetimes</li>
     *  <li>attempts to spawn a new bonus reward when the spawn interval is reached</li>
     *  <li>pauses for a reward popup if one was triggered this tick</li>
     * </ol>
     *
     * <p>If the game is not currently in {@link ScreenState#PLAYING},
     * this method does nothing.</p>
     */
    public void updateTick()
    {
        if(screenState != ScreenState.PLAYING)
        {
            return;
        }

        if (ogreHitCooldown > 0)
        {
            ogreHitCooldown--;
        }  

        timeElapsed++;

        PopupReason rewardPopup = null;
        Reward collectedReward = null;

        if (pendingMove != null)
        {
            collectedReward = player.move(pendingMove);
            pendingMove = null;

            if (collectedReward instanceof RegularReward)
            {
                collectedRegularRewards++;
                rewardPopup = PopupReason.KEY_COLLECTED;
            }
            else if (collectedReward instanceof BonusReward)
            {
                collectedBonusRewards++;
                bonusRewards.removeIf(b -> b.getPosition().equals(player.getPosition()));
                rewardPopup = PopupReason.BONUS_COLLECTED;
            }

            if (isTouchingOgre() && ogreHitCooldown == 0) {
                handleOgreHit();
                return;
            }

            if (checkLoss()) {
                return;
            }
        }

        player.tickBonus();

		// Setup list of enemy positions
		Set<Position> enemy_positions = enemies.stream()
				.map(Enemy::getPosition)
				.collect(Collectors.toSet());

		for (Enemy enemy : enemies) {
			// Remove the current enemies position (enemies can move into their own tiles)
			enemy_positions.remove(enemy.getPosition());
			// Update movement
			enemy.updateMovement(player.getPosition(), enemy_positions);
			// Add the new position back into the set
			enemy_positions.add(enemy.getPosition());

		}

        if (isTouchingOgre() && ogreHitCooldown == 0) {
            handleOgreHit();
            return;
        }

        if(checkLoss())
        {
            return;
        }

        if(checkWin())
        {
            endReason = EndReason.WIN;
            endGame();
            return;
        }

        updateActiveBonusRewards();

        if (shouldTrySpawnBonusReward())
        {
            trySpawnBonusReward();
        }

        if (rewardPopup != null)
        {
            pauseForPopup(rewardPopup);
        }
    }

    /**
     * Builds the list of board positions that are valid for temporary bonus reward spawning.
     *
     * <p>Valid spawn positions must:
     * <ul>
     *   <li>currently be a {@link FloorCell}</li>
     *   <li>not be the start tile</li>
     *   <li>not be the exit tile</li>
     *   <li>not overlap an initial key position</li>
     *   <li>not overlap an initial trap position</li>
     *   <li>not overlap an initial enemy spawn position</li>
     * </ul>
     *
     * @return list of legal bonus spawn positions
     */
    private List<Position> buildBonusSpawnPositions()
    {
        List<Position> positions = new ArrayList<>();

        for (int y = 0; y < board.getHeight(); y++)
        {
            for (int x = 0; x < board.getWidth(); x++)
            {
                Position pos = new Position(x, y);
                Cell cell = board.getCell(x, y);

                if (!(cell instanceof FloorCell))
                {
                    continue;
                }

                if (isReservedBonusSpawnPosition(pos))
                {
                    continue;
                }

                positions.add(pos);
            }
        }

        return positions;
    }

    /**
     * Checks whether a position is reserved for another gameplay purpose and
     * therefore should not be used for random bonus reward spawning.
     *
     * @param pos position to check
     * @return true if reserved, false otherwise
     */
    private boolean isReservedBonusSpawnPosition(Position pos)
    {
        if (pos.equals(board.getStartPosition()))
        {
            return true;
        }

        if (pos.equals(board.getEndPosition()))
        {
            return true;
        }

        if (initialKeyPos.contains(pos))
        {
            return true;
        }

        if (initialTrapPos.contains(pos))
        {
            return true;
        }

        if (initialEnemyPos.contains(pos))
        {
            return true;
        }

        return false;
    }

    /**
     * Ticks all active bonus reward spawns, removes expired ones from the board,
     * and then removes them from the tracking list.
     */
    private void updateActiveBonusRewards()
    {
        List<BonusRewardSpawn> expiredRewards = new ArrayList<>();

        for (BonusRewardSpawn bonus : bonusRewards)
        {
            bonus.tick();

            if (bonus.isExpired())
            {
                expiredRewards.add(bonus);
            }
        }

        for (BonusRewardSpawn expiredBonus : expiredRewards)
        {
            clearBonusRewardFromBoard(expiredBonus.getPosition());
        }

        bonusRewards.removeAll(expiredRewards);
    }

    /**
     * Returns true when the game should attempt a new bonus reward spawn.
     *
     * @return true if a spawn attempt should happen this tick
     */
    private boolean shouldTrySpawnBonusReward()
    {
        if (!bonusRewards.isEmpty())
        {
            return false;
        }

        return timeElapsed > 0 && timeElapsed % BONUS_SPAWN_INTERVAL_TICKS == 0;
    }

    /**
     * Attempts to spawn a single temporary bonus reward on a random legal tile.
     *
     * <p>The chosen tile must still currently be a plain {@link FloorCell},
     * and cannot be occupied by the player or an enemy.</p>
     */
    private void trySpawnBonusReward()
    {
        List<Position> availablePositions = new ArrayList<>();

        for (Position pos : bonusSpawnPositions)
        {
            if (!isCurrentlyAvailableBonusSpawnPosition(pos))
            {
                continue;
            }

            availablePositions.add(pos);
        }

        if (availablePositions.isEmpty())
        {
            return;
        }

        Position spawnPos = availablePositions.get(random.nextInt(availablePositions.size()));

        BonusReward reward = new BonusReward(BONUS_REWARD_VALUE, BONUS_EFFECT_DURATION_TICKS);
        board.setCell(spawnPos.getX(), spawnPos.getY(), new RewardCell(spawnPos, reward));
        bonusRewards.add(new BonusRewardSpawn(spawnPos, BONUS_SPAWN_LIFETIME_TICKS));
        totalBonusRewards++;
    }

    /**
     * Returns whether a bonus reward may spawn on this tile right now.
     *
     * @param pos position to check
     * @return true if the tile is currently usable for spawning
     */
    private boolean isCurrentlyAvailableBonusSpawnPosition(Position pos)
    {
        if (!(board.getCell(pos.getX(), pos.getY()) instanceof FloorCell))
        {
            return false;
        }

        if (player.getPosition().equals(pos))
        {
            return false;
        }

        for (Enemy enemy : enemies)
        {
            if (enemy.getPosition().equals(pos))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Removes a bonus reward from the board by restoring its tile to floor,
     * but only if the tile still contains a reward cell.
     *
     * @param pos board position to clear
     */
    private void clearBonusRewardFromBoard(Position pos)
    {
        Cell cell = board.getCell(pos.getX(), pos.getY());

        if (cell instanceof RewardCell)
        {
            board.setCell(pos.getX(), pos.getY(), new FloorCell(pos));
        }
    }

    /**
     * Getter for the list of characters
     * @return A list of characters
     */
    public List<GameCharacter> getCharacters() 
    {
	    List<GameCharacter> char_list = new ArrayList<>(enemies);
	    char_list.add(player);
	    return char_list;
    }

    /**
     * Handles one keyboard input event for the current screen state.
     *
     * <p>Depending on the current {@link ScreenState}, this method may:
     * <ul>
     *  <li>start the game from the start screen</li>
     *  <li>resume from a pause or popup state</li>
     *  <li>restart after the game ends</li>
     *  <li>toggle pause while playing</li>
     *  <li>queue the player's next movement direction while playing</li>
     * </ul>
     *
     * <p>During {@link ScreenState#PLAYING}, movement input does not move the player
     * immediately. It stores the requested direction in {@code pendingMove}, and the
     * actual movement and any related reward, popup, or win/loss processing are
     * handled on the next call to {@link #updateTick()}.</p>
     *
     * <p>Supported movement key codes:
     * <ul>
     *  <li>87 (W) or 38 (↑) - move NORTH</li>
     *  <li>83 (S) or 40 (↓) - move SOUTH</li>
     *  <li>65 (A) or 37 (←) - move WEST</li>
     *  <li>68 (D) or 39 (→) - move EAST</li>
     * </ul>
     *
     * <p>Key code 80 (P) toggles pause while playing. Space is used for start,
     * resume, and restart depending on the current screen state.</p>
     *
     * @param keyCode integer key code from a keyboard event
     */
    public void handleInput(int keyCode)
    {
        if (screenState == ScreenState.START) 
        {
            if (keyCode == 32) 
            { // Space
                startGame();
            }
            return;
        }

        if (screenState == ScreenState.PAUSE) 
        {
            if (keyCode == 32) 
            { // Space only
                if (popupReason != null) 
                {
                    resumeFromPopup();
                } else {
                    togglePause();
                }
            }
            return;
        }

        if (screenState == ScreenState.END) 
        {
            if (keyCode == 32) 
            { // Space
                resetGameState();
                startGame();
            }
            return;
        }

        if(screenState != ScreenState.PLAYING)
        {
            return;
        }

        if(keyCode == 80) // P
        {
            togglePause();
            return;
        }

        switch(keyCode)
        {
            case 87: case 38:
                pendingMove = Direction.NORTH;
                break;
            case 83: case 40:
                pendingMove = Direction.SOUTH;
                break;
            case 65: case 37:
                pendingMove = Direction.WEST;
                break;
            case 68: case 39:
                pendingMove = Direction.EAST;
                break;
            default:
                return;
        }
    }

    /**
     * Applies the ogre collision penalty to the player.
     *
     * <p>If the player's score becomes negative after the penalty,
     * the game ends with {@link EndReason#LOSE_BY_OGRE}. Otherwise,
     * an ogre-hit cooldown is started, preventing another ogre penalty
     * on the next tick, and an ogre-hit popup is shown.</p>
     */
    private void handleOgreHit() {
        player.setScore(player.getTotalScore() - OGRE_HIT_PENALTY);

        if (player.getTotalScore() < 0)
        {
            endReason = EndReason.LOSE_BY_OGRE;
            endGame();
            return;
        }

        ogreHitCooldown = 2;
        pauseForPopup(PopupReason.OGRE_HIT);
    }

    /**
     * Checks whether the player is currently on the same tile as an ogre.
     *
     * @return {@code true} if the player is touching an ogre, otherwise {@code false}
     */
    private boolean isTouchingOgre() {
        for (Enemy enemy : enemies) {
            if (enemy instanceof  Ogre && enemy.getPosition().equals(player.getPosition())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Pauses the game and records the popup reason currently being shown
     *
     * <p>This freezes elapsed gameplay time by saving the time accumulated
     * since the last resume/start moment, then switches the game into
     * {@link ScreenState#PAUSE}</p>
     *
     * @param reason reason the popup is being displayed
     */
    private void pauseForPopup(PopupReason reason) 
    {
        pendingMove = null;
        popupReason = reason;
        totalTime += clock.getAsLong() - startTime;
        screenState = ScreenState.PAUSE;
    }

    /**
     * Resumes gameplay from a paused or popup state
     *
     * <p>This clears the current popup reason, resets the running time anchor,
     * and returns the game to {@link ScreenState#PLAYING}</p>
     */
    private void resumeFromPopup() 
    {
        pendingMove = null;
        popupReason = null;
        startTime = clock.getAsLong();
        screenState = ScreenState.PLAYING;
    }

    /**
     * Checks whether player has met win condition
     * 
     * <p>Player wins when all keys have been collected and player
     * is standing on exit cell</p>
     * 
     * @return {@code true} if conditions are met
     */
    public boolean checkWin()
    {
         return collectedRegularRewards >= totalRegularRewards
            && player.getPosition().equals(board.getEndPosition());
    }

    /**
     * Checks whether the player has lost the game.
     *
     * <p>The player loses if:</p>
     * <ul>
     *  <li>their total score drops below 0, or</li>
     *  <li>a {@link Goblin} occupies the same position as the player</li>
     * </ul>
     *
     * <p>Ogre collisions are handled separately by {@link #handleOgreHit()}.</p>
     *
     * <p>When a loss is detected, this method sets the appropriate
     * {@link EndReason}, triggers {@link #endGame()}, and returns {@code true}.</p>
     *
     * @return {@code true} if a loss condition was detected, otherwise {@code false}
     */
    public boolean checkLoss()
    {
        if(player.getTotalScore() < 0)
        {
            
            endReason = EndReason.LOSE_BY_TRAP;
            endGame();
            return true;
        }

        for(Enemy enemy : enemies)
        {
            if(enemy.getPosition().equals(player.getPosition()))
            {
                
                if(enemy instanceof Goblin)
                {
                    endReason = EndReason.LOSE_BY_GOBLIN;  
                    endGame();
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Toggles between paused and playing states
     *
     * <p>If the game is currently playing, elapsed time is frozen and the
     * screen state changes to {@link ScreenState#PAUSE}. If the game is
     * currently paused, the running time anchor is reset and gameplay resumes</p>
     */
    public void togglePause()
    {
        if(screenState == ScreenState.PLAYING)
        {
            pendingMove = null;
            totalTime += clock.getAsLong() - startTime;
            screenState = ScreenState.PAUSE;
        }
        else if(screenState == ScreenState.PAUSE)
        {
            pendingMove = null;
            startTime = clock.getAsLong();
            screenState = ScreenState.PLAYING;
        }
    }

    /**
     * Finalizes the current run and switches the game to the end screen.
     *
     * <p>This stores the final elapsed play time using the configured time source
     * so that {@link #getSeconds()} returns a stable frozen result while the end
     * screen is displayed.</p>
     */
    private void endGame()
    {
        pendingMove = null;
        totalTime += clock.getAsLong() - startTime;
        screenState = ScreenState.END;
    }

    /**
     * Returns number of seconds game has been running, excluding
     * time when paused.
     *
     * <p>The elapsed time is calculated using the configured time source.</p>
     *
     * @return seconds game has been running
     */
    public int getSeconds()
    {
        if(screenState == ScreenState.PLAYING)
        {
            return (int) ((totalTime + clock.getAsLong() - startTime) / 1000);
        }
        return (int) (totalTime / 1000);
    }

    /**
     * Returns the displayed score, clamped to a minimum of 0.
     *
     * @return the current non-negative displayed score
     */
    public int getDisplayedScore() {
        return Math.max(0, player.getTotalScore());
    }

    /**
    * Returns the total number of regular rewards on the board
    *
    * @return totalRegularRewards
    */
    public int getTotalRegularRewards()
    {
        return totalRegularRewards;
    }

    /**
    * Returns the number of regular rewards the player has collected
    *
    * @return collectedRegularRewards
    */
    public int getCollectedRegularRewards()
    {
        return collectedRegularRewards;
    }

    /**
     * Returns the total number of bonus rewards spawned during the current run
     * 
     * @return totalBonusRewards
     */
    public int getTotalBonusRewards()
    {
        return totalBonusRewards;
    }

    /**
     * returns the number of bonus rewards that player has colllected
     * 
     * @return collectedBonusRewards
     */
    public int getCollectedBonusRewards()
    {
        return collectedBonusRewards;
    }

    /**
     * Returns the currently tracked active temporary bonus reward spawns.
     *
     * <p>The returned list is the internal tracking list used by this game.</p>
     *
     * @return active bonus reward spawn trackers
     */
    public List<BonusRewardSpawn> getBonusRewards()
    {
        return bonusRewards;
    }

    /**
     * Returns all legal positions where a bonus reward may spawn.
     *
     * @return bonus spawn positions
     */
    public List<Position> getBonusSpawnPositions()
    {
        return new ArrayList<>(bonusSpawnPositions);
    }

    /**
    * Returns the current screen state of the game
    *
    * @return screenState
    */
    public ScreenState getScreenState()
    {
        return screenState;
    }

    /**
    * Returns the reason the game ended
    *
    * @return endReason, or {@code null} if the game has not ended
    */
    public EndReason getEndReason()
    {
        return endReason;
    }

    /**
    * Returns the reason a popup is currently being shown
    *
    * @return popupReason, or {@code null} if no popup is active
    */
    public PopupReason getPopupReason()
    {
        return popupReason;
    }

    /**
     * Returns the number of ticks elapsed since the game started.
     *
     * @return the current tick count
     */
    public int getTimeElapsed()
    {
        return timeElapsed;
    }

    /**
     * Returns the player instance for this game.
     *
     * @return the current player
     */
    public Player getPlayer() 
    {
        return player;
    }
}
