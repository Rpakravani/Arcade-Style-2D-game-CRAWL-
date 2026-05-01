package ca.sfu.cmpt276.team7.core;

import java.util.Objects; 

/**
 * immutable position in the board grid (cell coordinates)
 * x = coloumn index (0 based, increases to the right)
 * y = row index (0 based, increased downards)
 */
public final class Position { // final class means nothing can extend Position, behaviour of Position will not change
    private final int x;
    private final int y;

    /**
     * creates a new grid position 
     * 
     * @param x (coloumn index, 0 based, increases to the right)
     * @param y (y = row index, 0 based, increased downards)
     * @throws IllegalArgumentException if x or y is negative
     */
    public Position(int x, int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Position coordinates must be non-negative"); // stops construction and reports error 
        }
        this.x = x;
        this.y = y;
    }

    /**
     * @return coloumn index, x
     */
    public int getX() {
        return x;
    }

    /**
     * @return row index, y
     */
    public int getY() {
        return y; 
    }

    /**
     * checks whether this position is "equal" to another object 
     * @param o any object to compare agasint 
     * @return true if o is a Position with the same x and y, otherwise return false
     */
    @Override 
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false; 
        Position other = (Position)o; 
        return x == other.x && y == other.y; // equal if coordinates match 
    }

    /**
     * returns an integer hash code for this position 
     * @return hash code based on x and y 
     */
    @Override 
    public int hashCode() {
        return Objects.hash(x,y);

        // int result = 17;
        // result = 31 * result + x;
        // result = 31 * result + y;
        // return result;
    }

    /**
     * @return a human readable string representation of this position 
     */
    @Override 
    public String toString() {
        return "Position(" + x + ", " + y + ")";
    }
}

