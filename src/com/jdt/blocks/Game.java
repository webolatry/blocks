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

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import android.content.res.Resources;

/**
 * Maintains current game state, processes game moves
 * 
 * @author Tom
 */
public class Game {

    public static final int CELL_OUT_OF_BOUNDS = -1;
    public static final int CELL_EMPTY = 0;
    public static final int CELL_STATE_MIN = 1;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    /*********************************************************************************************
     * Private Game data
     */

    /* the game id, used to save scores to the database */
    private String mID;
    /* the game name, for the UI */
    private String mName;
    /* number of rows and columns in the game board */
    private int mRows;
    private int mColumns;
    /* the game board matrix, each cell contains an integer that defines a piece */
    private int[][] mState;
    /*
     * lookup from board matrix integer to the color used to render the board
     * cell
     */
    private HashMap mColors;
    /* saves previous game piece states for move-undo */
    private Stack<Piece> mUndoStack;
    /* message to display when game level successfully completed */
    private String mFinishMessage;
    /*
     * defines the combination of board cells that constitute the successful
     * finish state
     */
    private Piece mFinishPiece;
    private boolean mFinished;
    private LinkedList<GameObserver> mObservers;

    /*********************************************************************************************
     * Game creation and access
     */

    /* the current game instance */
    //private static Game sInstance = null;

    /** Returns the current game instance */
    //public static Game getInstance() {
     //   return sInstance;
   // }

    /**
     * Create a new game instance using a game parser
     * 
     * @param parser contains the game board definition
     * @return a new Game instance based on the board definition in the parser
     */
    public static Game createFromResource(Resources res, int id) {
    	
    	GameParser parser = new GameParser(res, id);

        if (parser == null || !parser.valid())
            throw new IllegalArgumentException("illegal game resource");

        Game game = new Game();

        game.mRows = 0;
        game.mColumns = 0;
        game.mState = null;
        game.mID = new String(parser.getID());
        game.mName = new String(parser.getName());
        game.mUndoStack = new Stack<Piece>();
        game.mFinished = false;
        game.mObservers = new LinkedList<GameObserver>();
        game.mColors = new HashMap();

        String finishMessage = parser.getFinishMessage();
        if (finishMessage != null)
        	game.mFinishMessage = new String(finishMessage);

        if (parser.valid()) {
        	game.mRows = parser.getRows();
        	game.mColumns = parser.getColumns();
        	game.mState = parser.getState().clone();
        	game.mColors = (HashMap) parser.getColors().clone();

            try {
                game.mFinishPiece = (Piece) parser.getFinishPiece().clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException();
            }
        }

        return game;
    }

    /* Default constructor is private, games only created with a resource */
    private Game() {
    }

    /*********************************************************************************************
     * Game Observers
     */

    /**
     * Adds an object that will receive events from this game object
     * 
     * @param observer the observer to add
     */
    public void addObserver(GameObserver observer) {

        mObservers.add(observer);

        observer.onGameStart();
    }

    /**
     * Clears all objects from receiving events from this game objecgt
     */
    public void clearObservers() {
        mObservers.clear();
    }

    /*********************************************************************************************
     * Public Interface
     */

    /**
     * Returns the number of moves that have occurred in the current game
     * 
     * @return number of moves that may be un-done
     */
    public int getMoveCount() {
        return getUndoSize();
    }

    /**
     * Returns the name of the game, for display in the UI
     * 
     * @return game name
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns the game id, used to store game scores in the database
     * 
     * @return game id
     */
    public String getID() {
        return mID;
    }

    /**
     * returns the number of rows on the game board
     * 
     * @return rows
     */
    public int getRows() {
        return mRows;
    }

    /**
     * returns the number of columns on the game board
     * 
     * @return columns
     */
    public int getColumns() {
        return mColumns;
    }

    /**
     * Returns the state of the cell, a value that defines the type or color of
     * the game piece at the cell location, or empty if there is no piece at the
     * location
     * 
     * @param cell the cell for which the state is obtained
     * @return the cell state, the piece color
     */
    public int getCellState(Cell cell) {
        if (cell.mRow < 0 || cell.mRow >= mRows)
            return CELL_OUT_OF_BOUNDS;
        if (cell.mColumn < 0 || cell.mColumn >= mColumns)
            return CELL_OUT_OF_BOUNDS;

        return mState[cell.mRow][cell.mColumn];
    }

    /**
     * Determines if the provided cell is part of a piece
     * 
     * @param cell the cell location to test
     * @return true if the cell is part of a game piece
     */
    public boolean isCellPiece(Cell cell) {
        if (cell.mRow < 0 || cell.mRow >= mRows)
            return false;
        if (cell.mColumn < 0 || cell.mColumn >= mColumns)
            return false;

        return mState[cell.mRow][cell.mColumn] != CELL_EMPTY;
    }

    /**
     * Returns the color that the game board cell should be colored Different
     * game pieces have different colors, but not all pieces have unique colors.
     * Pieces with the same color may join together to form a larger piece, as
     * part of game play. Each board cell has a value that is used to look up
     * the actual color.
     * 
     * @param cell the board cell for which to look up the color
     * @return the color
     */
    public int getCellColor(Cell cell) {
        if (cell.mRow < 0 || cell.mRow >= mRows)
            return 0;
        if (cell.mColumn < 0 || cell.mColumn >= mColumns)
            return 0;

        int value = mState[cell.mRow][cell.mColumn];

        return getValueColor(value);
    }

    /**
     * Returns the color associated with the game piece value Game pieces are
     * assigned a single value. Each cell of a piece has that value in the game
     * board matrix. Contiguous cells with the same value constitute a piece.
     * After a piece has moved, it will merge with newly contiguous cells of the
     * same value to form a new, larger piece. The values of cells in the board
     * matrix are not colors themselves, but instead are used to lookup colors.
     * This is because there is a special value used to indicate empty cell
     * locations.
     * 
     * @param value the piece value for which to look up the color
     * @return the color
     */
    public int getValueColor(int value) {
        if (!mColors.containsKey(value))
            return 0;
        int color = (Integer) mColors.get(value);
        return color;
    }

    /**
     * Tests for the game being in a finished state
     * 
     * @return true if the game is finished
     */
    public boolean isFinished() {
        return mFinished;
    }

    /**
     * Tests for any moves that can be un-done
     * 
     * @return true if there are moves that can be un-done
     */
    public boolean canUndoMove() {
        return !mUndoStack.empty();
    }

    /**
     * Returns the game to its start-up state
     */
    public void restartGame() {
        if (mUndoStack.empty())
            return;

        /* undo all moves */
        while (!mUndoStack.empty()) {
            Piece piece = mUndoStack.pop();
            piece.mMobility.reverse();
            removePiece(piece);
            piece.move();
            addPiece(piece);
        }

        mFinished = false;

        /* notify all observers that the game is starting */
        for (GameObserver observer : mObservers) {
            observer.onGameStart();
        }
    }

    /**
     * Un-dones one game move, if the game isn't finished, and there are
     * available moves
     */
    public void undoMove() {
        if (isFinished())
            return;

        if (!canUndoMove())
            return;

        Piece piece = mUndoStack.pop();

        for (GameObserver observer : mObservers) {
            observer.onUndoMove(piece);
        }
    }

    /**
     * Removes a piece from the board. When a piece is moved it is first
     * removed, then the movement is animated, then the piece is added to its
     * new location.
     * 
     * @param piece the piece to remove
     */
    public void removePiece(Piece piece) {
        if (piece == null)
            return;

        /* remove each cell of the piece */
        for (Iterator<Cell> iter = piece.mCells.iterator(); iter.hasNext();) {
            Cell cell = iter.next();
            setCellState(cell, CELL_EMPTY);
        }
    }

    /**
     * Adds a piece to the game board. When a piece is moved it is first
     * removed, then the movement is animated, then the piece is added to its
     * new location.
     * 
     * @param piece the piece to add.
     */
    public void addPiece(Piece piece) {
        if (piece == null)
            return;

        /* add each cell of the piece */
        for (Iterator<Cell> iter = piece.mCells.iterator(); iter.hasNext();) {
            Cell cell = iter.next();
            setCellState(cell, piece.mState);
        }
    }

    /**
     * Adds the piece to the move-undo stack, and fires an event that a move has
     * occurred
     * 
     * @param piece the piece that was moved
     */
    public void addUndoMove(Piece piece) {
        mUndoStack.push(piece);

        for (GameObserver observer : mObservers) {
            observer.onMovePiece();
        }
    }

    /**
     * Constructs a piece object using the provided cell as a seed location,
     * growing in all directions and gathering contiguous cells that have the
     * same state, the value that identifies the type or color of game piece at
     * each cell location
     * 
     * @param cell the seed board location from which to construct the game
     *            piece
     * @return the game piece overlapping the cell location
     */
    public Piece getPiece(Cell cell) {
        Piece piece = new Piece();

        piece.mState = getCellState(cell);

        if (piece.mState < CELL_STATE_MIN)
            return piece;

        Set<Cell> cellsToCheck = new HashSet<Cell>();
        Set<Cell> cellsChecked = new HashSet<Cell>();

        /* push input sell */
        cellsToCheck.add(cell);

        while (!cellsToCheck.isEmpty()) {
            /* get and remove first item */
            Iterator<Cell> iter = cellsToCheck.iterator();
            Cell cellToCheck = iter.next();
            cellsToCheck.remove(cellToCheck);

            /* save as checked */
            cellsChecked.add(new Cell(cellToCheck));

            /* add cell to piece */
            piece.mCells.add(new Cell(cellToCheck));

            /* check cell above */
            if (getRelativeCellState(cellToCheck, Direction.UP) == piece.mState) {
                /* add cell to cells-to-check, if not already checked */
                Cell newCell = new Cell(cellToCheck, Direction.UP);
                if (!cellsChecked.contains(newCell))
                    cellsToCheck.add(newCell);
            } else {
                /*
                 * cell above not part of piece, determine distance to nearest
                 * barrier (board end or another piece)
                 */
                piece.mMobility.setMobility(getCellMobility(cellToCheck, Direction.UP),
                        Direction.UP);
            }

            /* check cell below */
            if (getRelativeCellState(cellToCheck, Direction.DOWN) == piece.mState) {
                /* add cell to cells-to-check, if not already checked */
                Cell newCell = new Cell(cellToCheck, Direction.DOWN);
                if (!cellsChecked.contains(newCell))
                    cellsToCheck.add(newCell);
            } else {
                /*
                 * cell above not part of piece, determine distance to nearest
                 * barrier (board end or another piece)
                 */
                piece.mMobility.setMobility(getCellMobility(cellToCheck, Direction.DOWN),
                        Direction.DOWN);
            }

            /* check cell left */
            if (getRelativeCellState(cellToCheck, Direction.LEFT) == piece.mState) {
                /* add cell to cells-to-check, if not already checked */
                Cell newCell = new Cell(cellToCheck, Direction.LEFT);
                if (!cellsChecked.contains(newCell))
                    cellsToCheck.add(newCell);
            } else {
                /*
                 * cell above not part of piece, determine distance to nearest
                 * barrier (board end or another piece)
                 */
                piece.mMobility.setMobility(getCellMobility(cellToCheck, Direction.LEFT),
                        Direction.LEFT);
            }

            /* check cell right */
            if (getRelativeCellState(cellToCheck, Direction.RIGHT) == piece.mState) {
                /* add cell to cells-to-check, if not already checked */
                Cell newCell = new Cell(cellToCheck, Direction.RIGHT);
                if (!cellsChecked.contains(newCell))
                    cellsToCheck.add(newCell);
            } else {
                /*
                 * cell above not part of piece, determine distance to nearest
                 * barrier (board end or another piece)
                 */
                piece.mMobility.setMobility(getCellMobility(cellToCheck, Direction.RIGHT),
                        Direction.RIGHT);
            }
        }

        return piece;
    }

    /**
     * Sets the game state to finished
     */
    public void setFinished() {
        mFinished = true;

        for (GameObserver observer : mObservers) {
            observer.onGameFinish();
        }
    }

    /**
     * Tests the input piece to see if it matches the definition of the finish
     * piece from the game xml file
     * 
     * @param piece the piece to test
     * @return true if the piece matches the finish piece
     */
    public boolean isFinishPiece(Piece piece) {
        if (piece == null)
            return false;
        if (mFinishPiece == null)
            return false;

        return mFinishPiece.equals(piece);
    }

    /**
     * Get the message to display when the game is successfully finished
     * 
     * @return the display message
     */
    public String getFinishMessage() {
        return mFinishMessage;
    }

    /**
     * Set the value of a specific game board cell
     * 
     * @param cell the cell to change
     * @param value the value for the cell
     * @return
     */
    private boolean setCellState(Cell cell, int value) {
        if (cell.mRow >= mRows && cell.mColumn >= mColumns)
            return false;
        mState[cell.mRow][cell.mColumn] = value;
        return true;
    }

    /**
     * Returns the state of the cell adjacent to the input cell, in the
     * specified direction
     * 
     * @param cell the cell for which to obtain the state of a neighboring cell
     * @param direction the direction from the input cell
     * @return the state of the neighboring cell, or out-of-bounds if no
     *         neighbor
     */
    private int getRelativeCellState(Cell cell, Direction direction) {
        switch (direction) {
            case UP:
                if (cell.mRow == 0)
                    return CELL_OUT_OF_BOUNDS;
                return mState[cell.mRow - 1][cell.mColumn];
            case DOWN:
                if (cell.mRow == (mRows - 1))
                    return CELL_OUT_OF_BOUNDS;
                return mState[cell.mRow + 1][cell.mColumn];
            case LEFT:
                if (cell.mColumn == 0)
                    return CELL_OUT_OF_BOUNDS;
                return mState[cell.mRow][cell.mColumn - 1];
            case RIGHT:
                if (cell.mColumn == (mColumns - 1))
                    return CELL_OUT_OF_BOUNDS;
                return mState[cell.mRow][cell.mColumn + 1];
            default:
                return CELL_OUT_OF_BOUNDS;
        }
    }

    /**
     * Determines the number of empty board spaces from the provided cell, in
     * the provided direction
     * 
     * @param cell the starting location from which to count empty spaces
     * @param direction the direction to travel, counting empty spaces
     * @return the number of empty spaces, how far the cell could travel without
     *         hidding another game piece
     */
    private int getCellMobility(Cell cell, Direction direction) {
        int mobility = 0;

        switch (direction) {
            case UP:
                for (int row = cell.mRow - 1; row >= 0; --row) {
                    if (mState[row][cell.mColumn] != CELL_EMPTY)
                        break;
                    ++mobility;
                }
                return mobility;
            case DOWN:
                for (int row = cell.mRow + 1; row < mRows; ++row) {
                    if (mState[row][cell.mColumn] != CELL_EMPTY)
                        break;
                    ++mobility;
                }
                return mobility;
            case LEFT:
                for (int col = cell.mColumn - 1; col >= 0; --col) {
                    if (mState[cell.mRow][col] != CELL_EMPTY)
                        break;
                    ++mobility;
                }
                return mobility;
            case RIGHT:
                for (int col = cell.mColumn + 1; col < mColumns; ++col) {
                    if (mState[cell.mRow][col] != CELL_EMPTY)
                        break;
                    ++mobility;
                }
                return mobility;
            default:
                return 0;
        }
    }

    /**
     * Returns the number of moves on the undo stack
     * 
     * @return number of moves that may be un-done
     */
    private int getUndoSize() {
        return mUndoStack.size();
    }
}
