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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Iterator;

/**
 * Renders the game board
 * 
 * @author Tom
 */
public class BlocksView extends View {

    /* size of the game board */
    private int mViewWidth = 0;
    private int mViewHeight = 0;

    /* layout that contains this view */
    private BoardLayout mBoardLayout;

    public BlocksView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Called when the size of this view has changed. */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewWidth = w;
        mViewHeight = h;
    }

    /** Called when the view should render its content. */
    @Override
    protected void onDraw(Canvas canvas) {

        Game game = Game.getInstance();
        if (game == null)
            return;
        if (mBoardLayout == null)
            return;

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(mBoardLayout.getBoardOffsetX(), mBoardLayout.getBoardOffsetY());
        canvas.scale(mBoardLayout.getBoardScaleFactor(), mBoardLayout.getBoardScaleFactor());

        float cell_width = getCellWidth();
        float cell_height = getCellHeight();

        Paint paint = new Paint();
        paint.setColor(R.color.background);
        paint.setStyle(Paint.Style.FILL);

        int rows = game.getRows();
        int columns = game.getColumns();

        Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        Bitmap mBitmap = Bitmap.createScaledBitmap(myBitmap, mViewWidth, mViewHeight, true);

        canvas.drawBitmap(mBitmap, 0, 0, null);

        RectF cell_rect = new RectF();
        Cell cell = new Cell();

        /* for each game board row... */
        for (cell.mRow = 0; cell.mRow < rows; ++cell.mRow) {

            /* for each game board column */
            for (cell.mColumn = 0; cell.mColumn < columns; ++cell.mColumn) {

                if (!game.isCellPiece(cell))
                    continue;

                boolean draw_cell = true;
                int color = game.getCellColor(cell);
                int cellState = game.getCellState(cell);

                if (draw_cell) {

                    paint.setColor(color);
                    paint.setAlpha(150);

                    cell_rect.top = cell.mRow * cell_height + 1;
                    cell_rect.bottom = cell_rect.top + cell_height - 2;
                    cell_rect.left = cell.mColumn * cell_width + 1;
                    cell_rect.right = cell_rect.left + cell_width - 2;

                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRect(cell_rect, paint);

                    paint.setStrokeWidth(2);

                    /* grout, right */
                    if (cell.mColumn < columns - 1) {

                        cell.mColumn++;
                        if (game.getCellState(cell) == cellState)
                            canvas.drawLine(cell_rect.right + 1, cell_rect.top,
                                    cell_rect.right + 1, cell_rect.bottom, paint);
                        cell.mColumn--;
                    }
                    /* grout, bottom */
                    if (cell.mRow < rows - 1) {

                        cell.mRow++;
                        if (game.getCellState(cell) == cellState)
                            canvas.drawLine(cell_rect.left, cell_rect.bottom + 1, cell_rect.right,
                                    cell_rect.bottom + 1, paint);
                        cell.mRow--;
                    }
                }
            }
        }

        canvas.restore();
    }

    /**
     * Sets the layout that contains this view, used to get current size
     * 
     * @param layout
     */
    public void setLayout(BoardLayout layout) {
        mBoardLayout = layout;
    }

    /**
     * Returns the width of a single cell
     * 
     * @return
     */
    private float getCellWidth() {
        Game game = Game.getInstance();
        return mViewWidth / (float) game.getColumns();
    }

    /**
     * Returns the height of a single cell
     * 
     * @return
     */
    private float getCellHeight() {
        Game game = Game.getInstance();
        return mViewHeight / (float) game.getRows();
    }

    /**
     * Draws a moving piece, positions a specified percent between its current
     * position and its destination position
     * 
     * @param canvas drawn here
     * @param paint colors, alpha
     * @param piece the piece to render
     * @param percentMoved positions the piece for rendering, changed via
     *            animation
     */
    public void renderMovingPiece(Canvas canvas, Paint paint, Piece piece, float percentMoved) {

        /*
         * assume input canvas already adjusted for current board scale and
         * offset
         */
        float cell_width = getCellWidth();
        float cell_height = getCellHeight();
        RectF cell_rect = new RectF();

        /* get the color of the piece */
        Game game = Game.getInstance();
        int color = game.getValueColor(piece.mState);

        /* determine how far the piece has moved */
        float moveRows = piece.mMobility.getMoveRows();
        float moveCols = piece.mMobility.getMoveColumns();
        float movedX = moveCols * percentMoved * cell_width;
        float movedY = moveRows * percentMoved * cell_height;

        canvas.save(Canvas.MATRIX_SAVE_FLAG);

        /* add movement offset to canvas */
        canvas.translate(movedX, movedY);

        paint.setColor(color);
        paint.setAlpha(150);
        paint.setStyle(Paint.Style.FILL);

        /* draw each cell */
        for (Iterator<Cell> iter = piece.mCells.iterator(); iter.hasNext();) {

            Cell cell = iter.next();

            cell_rect.top = cell.mRow * cell_height + 1;
            cell_rect.bottom = cell_rect.top + cell_height - 2;
            cell_rect.left = cell.mColumn * cell_width + 1;
            cell_rect.right = cell_rect.left + cell_width - 2;

            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(cell_rect, paint);

            /* grout, right */
            Cell testCell = new Cell(cell, Direction.RIGHT);
            if (piece.mCells.contains(testCell)) {

                canvas.drawLine(cell_rect.right + 1, cell_rect.top, cell_rect.right + 1,
                        cell_rect.bottom, paint);
            }

            /* grout, bottom */
            testCell = new Cell(cell, Direction.DOWN);
            if (piece.mCells.contains(testCell)) {

                canvas.drawLine(cell_rect.left, cell_rect.bottom + 1, cell_rect.right,
                        cell_rect.bottom + 1, paint);
            }
        }

        canvas.restore();
    }

    /**
     * Draws a glowing piece, applying the specified alpha
     * 
     * @param canvas drawn here
     * @param paint colors, alpha
     * @param piece the piece to render
     * @param alpha the alpha with which to draw, changed via animation
     */
    public void renderGlowingPiece(Canvas canvas, Paint paint, Piece piece, int alpha) {

        float cell_width = getCellWidth();
        float cell_height = getCellHeight();
        RectF cell_rect = new RectF();

        /* get the color of the piece */
        Game game = Game.getInstance();
        int color = game.getValueColor(piece.mState);

        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setStyle(Paint.Style.FILL);

        /* draw each cell */
        for (Iterator<Cell> iter = piece.mCells.iterator(); iter.hasNext();) {

            Cell cell = iter.next();

            cell_rect.top = cell.mRow * cell_height + 1;
            cell_rect.bottom = cell_rect.top + cell_height - 2;
            cell_rect.left = cell.mColumn * cell_width + 1;
            cell_rect.right = cell_rect.left + cell_width - 2;

            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(cell_rect, paint);

            /* grout, right */
            Cell testCell = new Cell(cell, Direction.RIGHT);
            if (piece.mCells.contains(testCell)) {

                canvas.drawLine(cell_rect.right + 1, cell_rect.top, cell_rect.right + 1,
                        cell_rect.bottom, paint);
            }

            /* grout, bottom */
            testCell = new Cell(cell, Direction.DOWN);
            if (piece.mCells.contains(testCell)) {

                canvas.drawLine(cell_rect.left, cell_rect.bottom + 1, cell_rect.right,
                        cell_rect.bottom + 1, paint);
            }
        }
    }

    /**
     * Returns the current width of the game board
     * 
     * @return
     */
    public int getBoardWidth() {
        return mViewWidth;
    }

    /**
     * Returns the current height of the game board
     * 
     * @return
     */
    public int getBoardHeight() {
        return mViewHeight;
    }
}
