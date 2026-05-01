package ca.sfu.cmpt276.team7.reward;

import ca.sfu.cmpt276.team7.cells.Cell; 
import ca.sfu.cmpt276.team7.core.Position; 

/**
 * A cell that contains a punishment. When the player steps on this cell,
 * the punishment is triggered and applied to the player's score.
 */
public class PunishmentCell extends Cell{

   /** Punishment stored in this cell. */
   private Punishment punishment;
   
   /**
    * create a PunishmentCel at the given position with the specified punishment.
    * @param position the location of the cell on the board 
    * @param punishment the punishment contained in this cell
    */
   public PunishmentCell(Position position, Punishment punishment){
      super(position);
      this.punishment = punishment;
   }

   /**
    * Returns the punishment stored in this cell.
    *
    * @return stored punishment
    */
   public Punishment getPunishment(){
      return punishment;
   }

   /**
    * Returns whether this cell can be walked on.
    *
    * @return {@code true}, since players can step on punishment cells
    */
   public boolean isWalkable(){
      return true;
   }
}