package ca.sfu.cmpt276.team7.reward;
/**
 * A standard reward that simply gives the player points.
 * It has no special behavior beyond its point value.
 */
public class RegularReward extends Reward{
   /**
     * Creates a regular reward with the given point value.
     * @param value the number of points this reward is worth
     */
   public RegularReward(int value){
      super(value);
   }

   /**
    * Handles collection of this reward.
    *
    * <p>This reward has no special effect beyond its point value.</p>
    */
   @Override
   public void onCollect(){
      // no special behavior.
   }


}
