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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Displays the UI for the blocks game, the game board
 * 
 * @author Tom
 */
public class BlocksActivity extends Activity implements OnClickListener, GameObserver {
    /** Saves game scores to a database */
    private GameData mGameData;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * get the game instance first, if necessary; must be done before
         * calling setContentView; if the instance doesn't exist, create one
         * using the game-board data in the resources; currently there is only
         * one
         */
        Game game = Game.getInstance();

        if (game == null) {
            /* create a game instance use the game-board resource */
            GameParser parser = new GameParser(getResources(), R.xml.game1);
            game = Game.getInstance(parser);
        }

        /* create database */
        mGameData = new GameData(this);

        setContentView(R.layout.main);

        /* register button click handlers */
        View button = findViewById(R.id.undo);
        button.setOnClickListener(this);
        button = findViewById(R.id.restart);
        button.setOnClickListener(this);

        /* connect various UI elements as needed */
        BlocksView blocksView = (BlocksView) findViewById(R.id.board);
        BoardLayout boardLayout = (BoardLayout) findViewById(R.id.boardLayout);
        boardLayout.setView(blocksView);

        TextView Title = (TextView) findViewById(R.id.title);
        Title.setText(game.getName());
    }

    /** Called when the system is about to start resuming a previous activity. */
    @Override
    public void onPause() {
        super.onPause();

        /*
         * clear all game observers they will be re-added in OnResume
         */
        Game game = Game.getInstance();
        game.clearObservers();
    }

    /** Called when the activity will start interacting with the user. */
    @Override
    public void onResume() {
        super.onResume();

        /* add game observers */
        BoardLayout boardLayout = (BoardLayout) findViewById(R.id.boardLayout);
        Game game = Game.getInstance();
        game.addObserver(this);
        game.addObserver(boardLayout);
    }

    /**
     * Handles button clicks for the undo and restart buttons in the activity
     * view
     */
    public void onClick(View view) {
        Game game = Game.getInstance();

        if (view == findViewById(R.id.undo)) {
            if (!game.isFinished())
                game.undoMove();
        } else if (view == findViewById(R.id.restart)) {
            game.restartGame();
        }
    }

    /*********************************************************************************************
     * GameObserver
     */

    /** Called when a game piece has been moved */
    public void onMovePiece() {
        /* update the move count output */
        Game game = Game.getInstance();
        String str = String.format("%03d", game.getMoveCount());
        TextView current = (TextView) findViewById(R.id.current);
        current.setText(str);
    }

    /** Called when a game piece has been un-moved */
    public void onUndoMove(Piece piece) {
        onMovePiece();
    }

    /** Called when a game has started */
    public void onGameStart() {
        Game game = Game.getInstance();

        AlphaTextView view = (AlphaTextView) findViewById(R.id.boardMessage);
        view.hide();

        onMovePiece();

        /* look up the high score in the database */
        int bestScore = mGameData.getBestScore(game.getID());

        TextView scoreView = (TextView) findViewById(R.id.best);

        if (bestScore != GameData.INVALID_SCORE)
            scoreView.setText(String.format("%03d", bestScore));
        else
            scoreView.setText("000");
    }

    /** Called when a game has finished */
    public void onGameFinish() {
        /* display the game finish message */
        Game game = Game.getInstance();
        AlphaTextView view = (AlphaTextView) findViewById(R.id.boardMessage);
        view.setText(game.getFinishMessage());
        view.show();

        /* look up the high score in the database */
        int bestScore = mGameData.getBestScore(game.getID());

        /* get current game score */
        int currentScore = game.getMoveCount();

        if (currentScore < bestScore || bestScore == GameData.INVALID_SCORE) {
            mGameData.setBestScore(game.getID(), currentScore);

            TextView scoreView = (TextView) findViewById(R.id.best);
            scoreView.setText(String.format("%03d", currentScore));
        }
    }
}
