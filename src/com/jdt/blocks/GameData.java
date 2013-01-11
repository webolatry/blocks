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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Maintains game high score in a database
 * 
 * @author Tom
 */
public class GameData extends SQLiteOpenHelper {
    
    public static final int INVALID_SCORE = -1;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "blocks.best";
    private static final String SCORES_TABLE_NAME = "scores";
    private static final String ID_FIELD_NAME = "_id";
    private static final String GAME_FIELD_NAME = "game";
    private static final String SCORE_FIELD_NAME = "score";
    private static final String SCORES_TABLE_CREATE = "CREATE TABLE " + SCORES_TABLE_NAME + " ("
            + ID_FIELD_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, " + GAME_FIELD_NAME + " TEXT, "
            + SCORE_FIELD_NAME + " INTEGER);";

    GameData(Context context) {
        
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Called when the database is created for the first time. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        
        db.execSQL(SCORES_TABLE_CREATE);
    }

    /** Called when the database needs to be upgraded. */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
        db.execSQL("DROP TABLE IF EXISTS" + SCORES_TABLE_NAME);
        onCreate(db);
    }

    /**
     * Obtains the high score from the database, for the specified game
     * 
     * @param gameID the name of the game for which to obtain the high score
     * @return the high score
     */
    public int getBestScore(String gameID) {
        
        SQLiteDatabase db = getReadableDatabase();

        String columns[] = new String[] {
            SCORE_FIELD_NAME
        };
        String where = new String(GAME_FIELD_NAME + " = '" + gameID + "'");
        int score = INVALID_SCORE;

        Cursor cursor = db.query(SCORES_TABLE_NAME, columns, where, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            score = cursor.getInt(0);
        }

        cursor.close();

        return score;
    }

    /**
     * Saves the high score to the database, for the specified game
     * 
     * @param gameID the name of the game for which to save the high score
     * @param score the score to save
     */
    public void setBestScore(String gameID, int score) {
        
        SQLiteDatabase db = getWritableDatabase();
        String where = new String(GAME_FIELD_NAME + " = '" + gameID + "'");
        ContentValues values = new ContentValues();
        values.put(SCORE_FIELD_NAME, score);
        int rowCount = db.update(SCORES_TABLE_NAME, values, where, null);

        if (rowCount == 0) {
            values.put(GAME_FIELD_NAME, gameID);
            db.insert(SCORES_TABLE_NAME, null, values);
        }
    }
}
