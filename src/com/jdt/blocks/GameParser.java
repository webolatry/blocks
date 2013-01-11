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

import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Parses game definition contained in XML
 * 
 * @author Tom
 */
public class GameParser {
    /* the game id, used to save scores to the database */
    private String mID;
    /* the game name, for the UI */
    private String mName;
    /* number of rows and columns in the game board */
    private int mRows;
    private int mColumns;
    /*
     * the game board matrix, each cell contains an integer that defines a piece
     * type (corresponds to color)
     */
    private int[][] mState;
    /*
     * lookup from board matrix integer to the color used to render the board
     * cell
     */
    private HashMap mColors;
    /* message to display when game level successfully completed */
    private String mFinishMessage;
    /*
     * defines the combination of board cells that constitute the successful
     * finish state
     */
    private Piece mFinishPiece;

    private static final String TAG_GAME = "game";
    private static final String TAG_FINISH = "finish";
    private static final String TAG_PIECE = "piece";
    private static final String TAG_TYPE = "type";
    private static final String TAG_BOARD = "board";
    private static final String TAG_SIZE = "size";
    private static final String TAG_CELL = "cell";
    private static final String TAG_NAME = "name";
    private static final String TAG_ID = "id";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_COLORS = "colors";
    private static final String TAG_COLOR = "color";
    private static final String TAG_VALUE = "value";

    /**
     * Parses the game definition using an application resource
     * 
     * @param resources the application resources
     * @param id the id of the game board resource (XML)
     */
    public GameParser(Resources resources, int id) {
        LinkedList<String> ParseStack = new LinkedList<String>();
        LinkedList<String> BoardCells = new LinkedList<String>();
        LinkedList<String> FinishPieceCells = new LinkedList<String>();

        mFinishPiece = new Piece();

        int rows = 0;
        int columns = 0;
        boolean valid = true;
        Cell cell = new Cell();
        mColors = new HashMap();

        try {
            XmlResourceParser parser = resources.getXml(id);
            parser.next();
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT: {
                        /* nothing to do here */
                        break;
                    }
                    case XmlPullParser.START_TAG: {
                        /* add tag to stack */
                        ParseStack.addLast(parser.getName());

                        /* process attributes */
                        int attributeCount = parser.getAttributeCount();
                        if (attributeCount > 0) {
                            /* make a local copy of the stack */
                            LinkedList<String> LocalParseStack = (LinkedList<String>) ParseStack
                                    .clone();

                            String tag = LocalParseStack.removeFirst();
                            if (tag.equalsIgnoreCase(TAG_GAME)) {
                                tag = LocalParseStack.removeFirst();

                                if (tag.equalsIgnoreCase(TAG_FINISH)) {
                                    tag = LocalParseStack.removeFirst();
                                    if (tag.equalsIgnoreCase(TAG_PIECE)) {
                                        for (int i = 0; i < parser.getAttributeCount(); ++i) {
                                            if (parser.getAttributeName(i).equalsIgnoreCase(
                                                    TAG_TYPE)) {
                                                mFinishPiece.mState = Integer.parseInt(parser
                                                        .getAttributeValue(i));
                                            }
                                        }
                                    }
                                }

                                if (tag.equalsIgnoreCase(TAG_COLORS)) {
                                    tag = LocalParseStack.removeFirst();
                                    if (tag.equalsIgnoreCase(TAG_COLOR)) {
                                        int value = 0;
                                        int rgb = 0;
                                        boolean value_valid = false;
                                        boolean rgb_valid = false;
                                        for (int i = 0; i < parser.getAttributeCount(); ++i) {
                                            if (parser.getAttributeName(i).equalsIgnoreCase(
                                                    TAG_TYPE)) {
                                                value = Integer.parseInt(parser
                                                        .getAttributeValue(i));
                                                value_valid = true;
                                            } else if (parser.getAttributeName(i).equalsIgnoreCase(
                                                    TAG_VALUE)) {
                                                String color_value = parser.getAttributeValue(i);
                                                if (color_value.length() == 7) {
                                                    if (color_value.startsWith("#")) {
                                                        color_value = color_value.substring(1);
                                                        color_value = color_value.toLowerCase();
                                                        rgb = Integer.parseInt(color_value, 16);
                                                        rgb_valid = true;
                                                    }
                                                }
                                            }
                                        }

                                        if (value_valid && rgb_valid) {
                                            mColors.put(value, rgb);
                                        }
                                    }
                                }

                            }
                        }

                        break;
                    }
                    case XmlPullParser.END_TAG: {
                        /* remove tag from stack */
                        String tag = parser.getName();

                        if (!tag.equals(ParseStack.peekLast()))
                            throw new Exception("Bad XML");

                        ParseStack.removeLast();

                        break;
                    }
                    case XmlPullParser.TEXT: {
                        String value = parser.getText();

                        /* make a local copy of the stack */
                        LinkedList<String> LocalParseStack = (LinkedList<String>) ParseStack
                                .clone();// new Stack<String>(ParseStack);

                        String tag = LocalParseStack.removeFirst();

                        if (tag.equalsIgnoreCase(TAG_GAME)) {
                            tag = LocalParseStack.removeFirst();
                            if (tag.equalsIgnoreCase(TAG_BOARD)) {
                                tag = LocalParseStack.removeFirst();
                                if (tag.equalsIgnoreCase(TAG_SIZE)) {
                                    String[] results = value.split(",");
                                    if (results.length == 2) {
                                        rows = Integer.parseInt(results[0]);
                                        columns = Integer.parseInt(results[1]);
                                    }
                                } else if (tag.equalsIgnoreCase(TAG_CELL)) {
                                    BoardCells.addLast(value);
                                }
                            } else if (tag.equalsIgnoreCase(TAG_NAME)) {
                                mName = value.trim();
                            } else if (tag.equalsIgnoreCase(TAG_ID)) {
                                mID = value.trim();
                            } else if (tag.equalsIgnoreCase(TAG_FINISH)) {
                                tag = LocalParseStack.removeFirst();
                                if (tag.equalsIgnoreCase(TAG_MESSAGE)) {
                                    mFinishMessage = value.trim();
                                } else if (tag.equalsIgnoreCase(TAG_PIECE)) {
                                    tag = LocalParseStack.removeFirst();
                                    if (tag.equalsIgnoreCase(TAG_CELL)) {
                                        FinishPieceCells.addLast(value);
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                parser.next();
                eventType = parser.getEventType();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            valid = false;
        } catch (IOException e) {
            e.printStackTrace();
            valid = false;
        } catch (Exception e) {
            e.printStackTrace();
            valid = false;
        }

        if (valid) {
            if (rows != 0 && columns != 0 && !mName.isEmpty()) {
                initialize(rows, columns);

                Iterator iter = BoardCells.iterator();
                while (iter.hasNext() && valid) {
                    String cellValue = (String) iter.next();

                    String[] cellValues = cellValue.split(",");
                    valid = (cellValues.length == 3);

                    if (valid) {
                        cell.mRow = Integer.parseInt(cellValues[0]);
                        cell.mColumn = Integer.parseInt(cellValues[1]);
                        int state = Integer.parseInt(cellValues[2]);
                        setCellState(cell, state);
                    }
                }

                iter = FinishPieceCells.iterator();
                while (iter.hasNext() && valid) {
                    String cellValue = (String) iter.next();

                    String[] cellValues = cellValue.split(",");
                    valid = (cellValues.length == 2);

                    if (valid) {
                        int row = Integer.parseInt(cellValues[0]);
                        int column = Integer.parseInt(cellValues[1]);

                        mFinishPiece.mCells.add(new Cell(row, column));
                    }
                }
            }
        }

        if (!valid)
            mState = null;
    }

    /*
     * Sets the integer value associated with the cell contiguous cells with the
     * value value constitute a game piece
     */
    private void setCellState(Cell cell, int value) {
        if (cell.mRow < mRows && cell.mColumn < mColumns)
            mState[cell.mRow][cell.mColumn] = value;
    }

    /*
     * Fills the game board with the empty value, establishing a board with no
     * pieces
     */
    private void initialize(int rows, int columns) {
        mRows = rows;
        mColumns = columns;
        mState = new int[rows][columns];
        for (int row = 0; row < mRows; ++row)
            for (int col = 0; col < mColumns; ++col)
                mState[row][col] = Game.CELL_EMPTY;
    }

    /** Returns the name of the game, for display in the UI */
    public String getName() {
        return mName;
    }

    /** Returns the game Id, used to store game scores in the database */
    public String getID() {
        return mID;
    }

    /** Returns true if the game was successfully parsed from XML */
    public boolean valid() {
        return mState != null;
    }

    /** Returns the number of rows in the game board */
    public int getRows() {
        return mRows;
    }

    /** Returns the number of columns in the game board */
    public int getColumns() {
        return mColumns;
    }

    /** Returns the intial board state as defined in the game resource */
    public int[][] getState() {
        return mState;
    }

    /** Returns the game piece colors defined in the game resource */
    public HashMap getColors() {
        return mColors;
    }

    /** Returns the finish piece definition from the game resource */
    public Piece getFinishPiece() {
        return mFinishPiece;
    }

    /** Returns the game finish message from the game resource */
    public String getFinishMessage() {
        return mFinishMessage;
    }
}
