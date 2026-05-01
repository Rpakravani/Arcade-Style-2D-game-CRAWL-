package ca.sfu.cmpt276.team7.reward;
/**
 * Base class for all reward types in the game.
 * Each reward has a point value and defines behavior when collected by the player.
 */

public abstract class Reward{
   /** Point value granted when this reward is collected. */
   protected int value;

   /**  Creates a reward with the given point value.
    @param value the number of points this reward is worth 
   */
   public Reward (int value){
      this.value = value;
   }

   /**
    * Returns the point value of this reward.
    *
    * @return reward value
    */
   public int getValue(){
      return value;
   }
   
   /** 
    * Defines what happens when the player collects this reward.
    * Player.collectReward() handles score updates,
    * subclasses may add extra effects.
   */
   public abstract void onCollect();

}