package ca.sfu.cmpt276.team7.reward;

/**
 * A basic trap punishment that only removes points.
 */
public class TrapPunishment extends Punishment {

    /**
     * Creates a trap punishment with the given penalty value.
     *
     * @param penaltyValue the number of points removed when triggered
     */
    public TrapPunishment(int penaltyValue) {
        super(penaltyValue);
    }

    /**
     * Handles activation of this trap punishment.
     *
     * <p>This punishment has no extra behavior beyond the standard
     * score reduction handled by {@link Player#applyPunishment(Punishment)}.</p>
     */
    @Override
    public void onTrigger() {
        // No extra behavior for now
        // Player.applyPunishment() already subtracts the score 
    }
}
