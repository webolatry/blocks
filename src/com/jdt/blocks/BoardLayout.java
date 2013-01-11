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

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.FrameLayout;

/* nineoldandroids used for animation classes on older platforms */
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Contains the game view (BlocksView), receives and processes gestures, and
 * controls game piece animation
 * 
 * @author Tom
 */
public class BoardLayout extends FrameLayout implements GameObserver {

    /* current board width and height */
    private int mViewWidth = 0;
    private int mViewHeight = 0;

    /* scale and offset for when the board is zoomed & panned */
    private float mBoardOffsetX = 0.0f;
    private float mBoardOffsetY = 0.0f;
    private float mBoardScaleFactor = 1.0f;

    /* helper for detecting scale gestures (pinch zoom) */
    private ScaleGestureDetector mScaleDetector;
    /* helper for detecting tap and scroll gestures */
    private GestureDetector mGestureDetector;

    /* for animating a moving piece */
    private ObjectAnimator mPieceAnimator;
    private Piece mMovingPiece;

    /*
     * true when animating an undo-move so that after the animation the move
     * isn't pushed to the move undo-stack
     */
    private boolean mUndoingMove = false;

    /* for animating a glowing piece, after a piece has been moved */
    private AnimatorSet mGlowingAnimator;
    private Piece mGlowingPiece;

    /* receives animation events from this object */
    private BoardDrawable mDrawable;

    public BoardLayout(Context context, AttributeSet attrs) {

        super(context, attrs);

        final Game game = Game.getInstance();

        /* configure the gesture helpers */
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(context, new GestureListener());

        mDrawable = new BoardDrawable();
        mDrawable.setCallback(this);
        setForeground(mDrawable);

        /*
         * Configure the animation of a piece moving. The drawable function
         * setMovingPiecePercent will be called with values between 0 and 1
         */
        mPieceAnimator = ObjectAnimator.ofFloat(mDrawable, "MovingPiecePercent", 0.0f, 1.0f);
        mPieceAnimator.addListener(new AnimatorListenerAdapter() {

            /** Notifies the start of the animation. */
            public void onAnimationStart(Animator animation) {

                /*
                 * remove the piece from the board, it will be drawn by the
                 * animation
                 */
                game.removePiece(mMovingPiece);
            }

            /** Notifies the end of the animation. */
            public void onAnimationEnd(Animator animation) {

                /* stop the drawable from drawing the piece */
                mDrawable.clearMovingPiece();

                /*
                 * move the piece (according to game rules, it will only move in
                 * one direction)
                 */
                mMovingPiece.move();

                /* add the piece in its moved location */
                game.addPiece(mMovingPiece);

                /*
                 * get the first cell of the piece that was moved, will be used
                 * to find the destination piece, which may have merged with
                 * other pieces
                 */
                Cell cell = mMovingPiece.getFirstCell();

                /*
                 * put the moved piece on the undo stack, but not if the current
                 * animation is the undo of a previous move
                 */
                boolean checkPiece = false;
                if (!mUndoingMove) {
                    game.addUndoMove(mMovingPiece);
                    checkPiece = true;
                }

                /* clear state, not needed anymore */
                mMovingPiece = null;
                mUndoingMove = false;

                if (checkPiece) {

                    /* find piece from cell */
                    mGlowingPiece = game.getPiece(cell);

                    /* animate the piece at the destination, glowing */
                    mGlowingAnimator.start();

                    /* is the piece the finish piece ? */
                    if (game.isFinishPiece(mGlowingPiece)) {
                        game.setFinished();
                    }
                }
            }
        });
        mPieceAnimator.setDuration(250);

        /*
         * Configure the animation of a piece glowing The drawable function
         * setGlowAlpha will be called with alpha values
         */
        ObjectAnimator GlowingAnimator1 = ObjectAnimator.ofInt(mDrawable, "GlowAlpha", 5, 75);
        ObjectAnimator GlowingAnimator2 = ObjectAnimator.ofInt(mDrawable, "GlowAlpha", 75, 5);
        mGlowingAnimator = new AnimatorSet();
        mGlowingAnimator.addListener(new AnimatorListenerAdapter() {

            /** Notifies the start of the animation. */
            public void onAnimationStart(Animator animation) {
                mDrawable.setGlowingPiece(mGlowingPiece);
            }

            /** Notifies the end of the animation. */
            public void onAnimationEnd(Animator animation) {
                mDrawable.clearGlowingPiece();
                /* clear state, not needed anymore */
                mGlowingPiece = null;
            }
        });
        GlowingAnimator1.setDuration(350);
        GlowingAnimator2.setDuration(350);
        mGlowingAnimator.play(GlowingAnimator1).before(GlowingAnimator2);
        mUndoingMove = false;
    }

    /** Connects various rendering elements together */
    public void setView(BlocksView view) {
        mDrawable.setView(view);
        mDrawable.setLayout(this);
        view.setLayout(this);
    }

    /**
     * get the current board x-offset (pinch & zoom)
     * 
     * @return
     */
    public float getBoardOffsetX() {
        return mBoardOffsetX;
    }

    /**
     * get the current board y-offset (pinch & zoom)
     * 
     * @return
     */
    public float getBoardOffsetY() {
        return mBoardOffsetY;
    }

    /**
     * get the current board scale (pinch & zoom)
     * 
     * @return
     */
    public float getBoardScaleFactor() {
        return mBoardScaleFactor;
    }

    /** Returns a board cell from the provided x y */
    private Cell getCellFromXY(float X, float Y) {

        final Game game = Game.getInstance();
        float cellWidth = mViewWidth / game.getColumns();
        float cellHeight = mViewHeight / game.getRows();
        return new Cell((int) Math.floor(Y / cellWidth), (int) Math.floor(X / cellHeight));
    }

    /**
     * This is called during layout when the size of this view has changed.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
    }

    /**
     * Implement this method to handle touch screen motion events.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mGestureDetector.onTouchEvent(event))
            return true;
        if (mScaleDetector.onTouchEvent(event))
            return true;

        return false;
    }

    /**
     * Receives events from the ScaleGestureDetector For pinch-zoom
     * 
     * @author Tom
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress. Reported by
         * pointer motion. For pinch-zoom
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mBoardScaleFactor *= detector.getScaleFactor();
            mBoardScaleFactor = Math.max(1.0f, Math.min(mBoardScaleFactor, 5.0f));
            invalidate();
            return true;
        }
    }

    /**
     * Receives events from the GestureDetector For tapping on a piece to move
     * it, and panning the board
     * 
     * @author Tom
     */
    class GestureListener extends SimpleOnGestureListener {

        /**
         * Notified when a tap occurs with the up MotionEvent that triggered it.
         */
        @Override
        public boolean onSingleTapUp(MotionEvent event) {

            final Game game = Game.getInstance();

            if (game.isFinished())
                return true;

            /* convert x,y to board coordinates */
            float pts[] = {
                    event.getX(), event.getY()
            };
            Matrix m = new Matrix();
            m.preScale(1.0f / mBoardScaleFactor, 1.0f / mBoardScaleFactor);
            m.preTranslate(-mBoardOffsetX, -mBoardOffsetY);
            m.mapPoints(pts);

            /* get the tapped cell */
            Cell cell = getCellFromXY(pts[0], pts[1]);

            /* get the piece at the cell, if any */
            Piece piece = game.getPiece(cell);

            if (piece == null)
                return true;

            /* if the piece can't move, exit */
            if (!piece.canMove())
                return true;

            /* move the piece */
            mUndoingMove = false;
            mMovingPiece = piece;
            mDrawable.setMovingPiece(mMovingPiece);
            mPieceAnimator.start();

            return true;
        }

        /**
         * Notified when a scroll occurs with the initial on down MotionEvent
         * and the current move MotionEvent. For panning the game board
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            /* only scroll if board is zoomed in */
            if (mBoardScaleFactor > 1.0f) {
                mBoardOffsetX -= distanceX;
                mBoardOffsetY -= distanceY;

                invalidate();
            } else {
                /* make sure offset is zero */
                mBoardOffsetX = 0.0f;
                mBoardOffsetY = 0.0f;
                invalidate();
            }
            return true;
        }
    }

    /*********************************************************************************************
     * GameObserver
     */

    /** Called when a game piece has been moved */
    public void onMovePiece() {
    }

    /** Called when a game piece has been un-moved */
    public void onUndoMove(Piece piece) {

        try {
            mUndoingMove = true;
            mMovingPiece = (Piece) piece.clone();
            mMovingPiece.mMobility.reverse();
            mDrawable.setMovingPiece(mMovingPiece);
            mPieceAnimator.start();
        } catch (Exception e) {
            mUndoingMove = false;
            mMovingPiece = null;
            mDrawable.clearMovingPiece();
        }
    }

    /** Called when a game has started */
    public void onGameStart() {

        /* forward to drawable */
        mDrawable.onGameStart();
        invalidate();
    }

    /** Called when a game has finished */
    public void onGameFinish() {
    }
}
