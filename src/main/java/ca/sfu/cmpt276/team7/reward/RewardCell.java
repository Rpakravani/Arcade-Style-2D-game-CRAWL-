package ca.sfu.cmpt276.team7.reward;

import ca.sfu.cmpt276.team7.cells.Cell; 
import ca.sfu.cmpt276.team7.core.Position; 

/**
 * A cell that contains a reward. When the player steps on this cell,
 * the reward is collected and applied to the player's score.
 */
public class RewardCell extends Cell {
   /** Reward currently stored in this cell. */
   private Reward reward;

   /**
   * create a RewardCell at the given position with the specified reward.
   * @param position the location of this cell on the board
   * @param reward the reward contained in this cell
   */
   public RewardCell(Position position, Reward reward ){
   super(position);
   this.reward = reward;
   }

   /**
    * Returns the reward stored in this cell.
    *
    * @return stored reward, or {@code null} if the reward has been cleared
    */
   public Reward getReward(){
      return reward;
   }

   /**
    * Returns whether this cell can be walked on.
    *
    * @return {@code true}, since players can step on reward cells
    */
   public boolean isWalkable(){
      return true;
   }

   /**
    * Removes the reward from this cell.
    */
   public void clearReward() {
    this.reward = null;
   }

}
