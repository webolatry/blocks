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
 * Represents a single location on the game board
 * @author Tom
 *
 */
public class Cell {
    
    /** the cell row on the game board */
    public int mRow;
    /** the cell column on the game board */
    public int mColumn;

    Cell() {
        mRow = 0;
        mColumn = 0;
    }

    Cell(int row, int column) {
        mRow = row;
        mColumn = column;
    }

    Cell(Cell cell) {
        mRow = cell.mRow;
        mColumn = cell.mColumn;
    }

    public Cell(Cell cell, Direction direction) {
        mRow = cell.mRow;
        mColumn = cell.mColumn;

        switch (direction) {
            case UP:
                --mRow;
                break;
            case DOWN:
                ++mRow;
                break;
            case LEFT:
                --mColumn;
                break;
            case RIGHT:
                ++mColumn;
                break;
        }
    }

    public int hashCode() {
        return (mRow & 0x0000FFFF) | ((mColumn << 16) & 0xFFFF0000);
    }

    public boolean equals(Object that) {
        if (this == that)
            return true;

        if (!(that instanceof Cell))
            return false;

        Cell thatCell = (Cell) that;
        return (mRow == thatCell.mRow && mColumn == thatCell.mColumn);
    }
}
