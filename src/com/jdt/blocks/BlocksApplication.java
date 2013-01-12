package com.jdt.blocks;

import android.app.Application;

public class BlocksApplication extends Application {

	private Game mGame;

	@Override
	public void onCreate() {

		super.onCreate();

		mGame = Game.createFromResource(getResources(), R.xml.game1);
	}

	/**
	 * Returns the global instance of the Game object
	 * 
	 * @return the Game object
	 */
	public Game getGame() {
		return mGame;
	}

}
