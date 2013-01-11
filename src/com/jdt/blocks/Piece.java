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

import android.util.Log;

import java.util.HashSet;
import java.util.Iterator;
import java.text.MessageFormat;
import java.util.Set;

/**
 * Represents a single game piece on the game board
 * 
 * @author Tom
 */
public class Piece implements Cloneable {
    
    /*
     * the collection of cells in the game board grid that together define the
     * piece
     */
    public Set<Cell> mCells;
    /* the directions that this piece can move */
    public Mobility mMobility;
    /* the piece type */
    public int mState = Game.CELL_EMPTY;

    public Piece() {
        
        mCells = new HashSet<Cell>();
        mMobility = new Mobility();
    }

    /**
     * Tests if this piece can move, according to game rules
     * 
     * @return true if the piece is movable
     */
    public boolean canMove() {
        
        return !mCells.isEmpty() && mMobility.canMove();
    }

    /**
     * Moves the piece. A piece will move only if it can move in one direction
     * and then it will move as far as it can. This is the gameplay.
     */
    public void move() {
        
        /* get the number of rows and columns to move */
        int moveRows = mMobility.getMoveRows();
        int moveCols = mMobility.getMoveColumns();

        /*
         * clone the set, clear contents, rebuild set with modified cells have
         * to do it this way because set hash based on cell row/column values
         */
        HashSet<Cell> temp = new HashSet<Cell>();

        for (Iterator<Cell> iter = mCells.iterator(); iter.hasNext();) {
            Cell cell = iter.next();
            temp.add(new Cell(cell.mRow + moveRows, cell.mColumn + moveCols));
        }

        /* now the piece is defined by the new moved cells */
        mCells = temp;
    }

    /**
     * For debugging, logs the cells that define the piece
     * 
     * @param prefix
     */
    public void dump(String prefix) {
        
        StringBuffer str = new StringBuffer();
        str.append(prefix);
        for (Iterator<Cell> iter = mCells.iterator(); iter.hasNext();) {
            Cell cell = iter.next();

            String cellStr = MessageFormat.format("({0,number,integer},{1,number,integer})",
                    cell.mRow, cell.mColumn);
            str.append(cellStr);

        }
        Log.d("piece", str.toString());
    }

    /**
     * Returns the first cell that is part of this piece
     * 
     * @return
     */
    public Cell getFirstCell() {
        
        Iterator<Cell> iter = mCells.iterator();
        Cell cell = new Cell(iter.next());
        return cell;
    }

    /**
     * Create a copy of this object
     */
    protected Object clone() throws CloneNotSupportedException {
        
        Piece clone = new Piece();

        clone.mState = this.mState;

        for (Cell cell : this.mCells)
            clone.mCells.add(new Cell(cell));

        clone.mMobility = (Mobility) this.mMobility.clone();

        return clone;
    }

    /**
     * Tests for this piece being the same as the input piece
     * 
     * @param piece the other piece to compare
     * @return
     */
    public boolean equals(Piece piece) {
        
        /* self-test */
        if (this == piece)
            return true;

        /* same type ? */
        if (mState != piece.mState)
            return false;

        /* same cells ? */
        if (!mCells.equals(piece.mCells))
            return false;

        return true;
    }
}
