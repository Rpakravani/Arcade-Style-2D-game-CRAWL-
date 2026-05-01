package ca.sfu.cmpt276.team7.reward;
import ca.sfu.cmpt276.team7.core.Position;

/**
 * Tracks a temporary bonus reward currently spawned on the board.
 *
 * <p>This class stores the reward's position and the number of ticks
 * remaining before it expires.</p>
 */
public class BonusRewardSpawn {
    /** Board position where the temporary bonus reward is spawned. */
    private final Position position;
    /** Remaining lifetime in ticks before the reward expires. */
    private int lifetime;   // how many ticks it stays on the board

    /**
     * Creates a tracker for a spawned bonus reward.
     *
     * @param position board position of the spawned reward
     * @param lifetime number of ticks the reward should remain active
     */
    public BonusRewardSpawn(Position position, int lifetime) {
        this.position = position;
        this.lifetime = lifetime;
    }

    /**
     * Returns the board position of this spawned bonus reward.
     *
     * @return reward position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns the remaining lifetime of this spawned bonus reward.
     *
     * @return remaining lifetime in ticks
     */
    public int getLifetime() {
        return lifetime;
    }

    /**
     * Decreases the remaining lifetime by one tick, if it is still positive.
     */
    public void tick() {
        if (lifetime > 0) {
            lifetime--;
        }
    }

    /**
     * Returns whether this spawned bonus reward has expired.
     *
     * @return {@code true} if the reward should disappear, otherwise {@code false}
     */
    public boolean isExpired() {
        return lifetime <= 0;
    }
   }





