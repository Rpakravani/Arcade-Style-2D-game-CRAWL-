package ca.sfu.cmpt276.team7.reward;
/**
 * Base class for all punishment types in the game.
 * Each punishment has a penalty value and defines behavior when triggered.
 */
public abstract class Punishment{

   /** Number of points removed when this punishment is applied. */
   protected int penaltyValue;

   /** 
    * Creates a punishment with the given penalty value. 
    * @param penaltyValue the number of points this punishment removes
   */
   public Punishment(int penaltyValue){
      this.penaltyValue = penaltyValue;
   }
   
   /**
    * Returns the penalty value of this punishment.
    *
    * @return penalty value
    */
   public int getPenaltyValue(){
      return penaltyValue;
   }
   
   /**
     * Defines what happens when the player triggers this punishment.
     * Player.applyPunishment() handles score reduction; subclasses may add extra effects.
     */
   public abstract void onTrigger();

}

