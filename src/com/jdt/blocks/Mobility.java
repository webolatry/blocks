/*
 * Copyright (C) 2010 Tom Bruns
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.jdt.blocks;

import com.jdt.blocks.Game.Direction;

/**
 * Defines the possible movement of a board cell or game piece by maintaining
 * available movement in each of the 4 possible directions, combining them
 * according to the defining game rule that movement is allowed only if movement
 * is possible in one direction only.
 * 
 * @author Tom
 */
public class Mobility implements Cloneable {
    private static final int INVALID_MOBILITY = Integer.MIN_VALUE;

    /* number of contiguous empty rows above */
    private int mUp;
    /* number of contiguous empty rows below */
    private int mDown;
    /* number of contiguous empty columns right */
    private int mRight;
    /* number of contiguous empty columns left */
    private int mLeft;

    public Mobility() {
        mUp = INVALID_MOBILITY;
        mDown = INVALID_MOBILITY;
        mRight = INVALID_MOBILITY;
        mLeft = INVALID_MOBILITY;
    }

    /**
     * Tests for legal movement, according to game rules The fundamental rule of
     * this game is that a piece can move, but only if it is free to move in one
     * direction and one direction only. A piece must be blocked in all
     * directions except one, in which case it is allowed to move in that one
     * direction.
     * 
     * @return true if movement, in any direction, is possible
     */
    public boolean canMove() {
        
        if (mUp > 0 && mDown == 0 && mLeft == 0 && mRight == 0)
            return true;
        if (mUp == 0 && mDown > 0 && mLeft == 0 && mRight == 0)
            return true;
        if (mUp == 0 && mDown == 0 && mLeft > 0 && mRight == 0)
            return true;
        if (mUp == 0 && mDown == 0 && mLeft == 0 && mRight > 0)
            return true;

        return false;
    }

    /**
     * Returns the net movement in rows to be applied to a piece that is moving
     * 
     * @return rows to move a piece, positive for down, negative for up
     */
    public int getMoveRows() {

        if (mUp > 0 && mDown == 0)
            return -mUp;
        if (mUp == 0 && mDown > 0)
            return mDown;
        return 0;
    }

    /**
     * Returns the net movement in columns to be applied to a piece that is
     * moving
     * 
     * @return columns to move a piece, positive for right, negative for left
     */
    public int getMoveColumns() {

        if (mLeft > 0 && mRight == 0)
            return -mLeft;
        if (mLeft == 0 && mRight > 0)
            return mRight;
        return 0;
    }

    public void reverse() {

        int temp = mUp;
        mUp = mDown;
        mDown = temp;

        temp = mLeft;
        mLeft = mRight;
        mRight = temp;
    }

    public void setMobility(int mobility, Direction direction) {

        switch (direction) {

            case UP:
                if (mUp == INVALID_MOBILITY)
                    mUp = mobility;
                else
                    mUp = Math.min(mUp, mobility);
                break;
            case DOWN:
                if (mDown == INVALID_MOBILITY)
                    mDown = mobility;
                else
                    mDown = Math.min(mDown, mobility);
                break;
            case LEFT:
                if (mLeft == INVALID_MOBILITY)
                    mLeft = mobility;
                else
                    mLeft = Math.min(mLeft, mobility);
                break;
            case RIGHT:
                if (mRight == INVALID_MOBILITY)
                    mRight = mobility;
                else
                    mRight = Math.min(mRight, mobility);
                break;
        }
    }

    protected Object clone() throws CloneNotSupportedException {

        Mobility clone = new Mobility();

        clone.mLeft = this.mLeft;
        clone.mRight = this.mRight;
        clone.mUp = this.mUp;
        clone.mDown = this.mDown;

        return clone;
    }
}
