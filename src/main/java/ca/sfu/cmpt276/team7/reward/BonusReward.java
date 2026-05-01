package ca.sfu.cmpt276.team7.reward;
/**
 * A reward that provides a temp bonus eefect for a limited duration.
 */

public class BonusReward extends Reward {
   private int duration;

   /**
    * point value and add duration.
    * @param value the numbe of points this reward is worth
    * @param duration how many ticks the bonus effect lasts 
    */
   public BonusReward(int value,  int duration){
      super(value);
      this.duration = duration;
   }
   public void onCollect(){
      
   }
   /**
    * Decreases the duration and when it hits 0 the effect will go away.
    */
   public void tick(){
      if (duration > 0){
         duration--;
      }
   }
   /**
     * @return remaining duration of the bonus effect
   */
   
   public int getDuration() {
      return duration;
   }


}
