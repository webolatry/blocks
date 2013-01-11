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

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.PixelFormat;

import com.jdt.blocks.Piece;

/**
 * Draws game animation - moving and glowing pieces Receives animation values
 * from BoardLayout
 * 
 * @author Tom
 */
public class BoardDrawable extends Drawable {

    /* the game view renders the board background and pieces */
    private BlocksView mView;
    /* the layout has the current board pan and zoom */
    private BoardLayout mBoardLayout;

    /* for drawing the moving piece */
    private float mMovedPercent;
    private Piece mMovingPiece;

    /* for drawing the glowing piece */
    private Piece mGlowingPiece;
    private int mGlowAlpha;

    /* for drawing */
    private final Paint mPaint;

    public BoardDrawable() {

        mPaint = new Paint();
        mMovedPercent = 0.0f;
        mGlowAlpha = 0;
    }

    /** Sets the board view that renders animating pieces */
    public void setView(BlocksView view) {
        mView = view;
    }

    /** Sets the layout that provides the current pan & zoom */
    public void setLayout(BoardLayout layout) {
        mBoardLayout = layout;
    }

    /**
     * Draw in its bounds (set via setBounds) respecting optional effects such
     * as alpha (set via setAlpha) and color filter (set via setColorFilter).
     */
    @Override
    public void draw(Canvas canvas) {

        if (mMovingPiece == null && mGlowingPiece == null)
            return;

        if (mBoardLayout == null)
            return;

        canvas.save();

        /* apply current offset and scale (from pan and zoom) */
        canvas.translate(mBoardLayout.getBoardOffsetX(), mBoardLayout.getBoardOffsetY());
        canvas.scale(mBoardLayout.getBoardScaleFactor(), mBoardLayout.getBoardScaleFactor());

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2);
        mPaint.setARGB(120, 255, 0, 0);

        /* have the view draw the moving piece */
        if (mMovingPiece != null) {
            mView.renderMovingPiece(canvas, mPaint, mMovingPiece, mMovedPercent);
        }

        /* have the view draw the glowing piece */
        if (mGlowingPiece != null) {
            mView.renderGlowingPiece(canvas, mPaint, mGlowingPiece, mGlowAlpha);
        }

        canvas.restore();
    }

    /**
     * Return the opacity/transparency of this Drawable.
     */
    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    /**
     * Specify an alpha value for the drawable. 0 means fully transparent, and
     * 255 means fully opaque.
     */
    @Override
    public void setAlpha(int arg0) {
    }

    /** set and show the piece for movement animation */
    public void setMovingPiece(Piece piece) {
        mMovingPiece = piece;
        mMovedPercent = 0.0f;
        invalidateSelf();
    }

    /** Hides the moving piece, if any */
    public void clearMovingPiece() {
        if (mMovingPiece != null) {
            mMovingPiece = null;
            invalidateSelf();
        }
    }

    /** set and show the piece for glowing animation */
    public void setGlowingPiece(Piece piece) {
        mGlowingPiece = piece;
        mGlowAlpha = 0;
        invalidateSelf();
    }

    /** hides the glowing piece, if any */
    public void clearGlowingPiece() {
        if (mGlowingPiece != null) {
            mGlowingPiece = null;
            invalidateSelf();
        }
    }

    /**
     * Receives movement percent from the layout animator
     * 
     * @param percent
     */
    public void setMovingPiecePercent(float percent) {
        if (mMovingPiece != null) {
            mMovedPercent = percent;
            invalidateSelf();
        }
    }

    /**
     * Receives glow alpha from the layout animator
     * 
     * @param percent
     */
    public void setGlowAlpha(int alpha) {
        if (mGlowingPiece != null) {
            mGlowAlpha = alpha;
            invalidateSelf();
        }
    }

    /** Called when a game has started */
    public void onGameStart() {
        clearGlowingPiece();
        clearMovingPiece();
    }

    /**
     * Specify an optional colorFilter for the drawable. Pass null to remove any
     * filters.
     */
    @Override
    public void setColorFilter(ColorFilter arg0) {
    }
}
