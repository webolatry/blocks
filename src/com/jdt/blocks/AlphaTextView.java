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
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

/**
 * A TextView derivative for fading text
 * 
 * @author Tom
 * 
 */
public class AlphaTextView extends android.widget.TextView
{
	public AlphaTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/** Starts animation of alpha blending of the text view */
	public void show()
	{
		AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
		animation1.setDuration(500);

		animation1.setAnimationListener(new AnimationListener()
		{

			public void onAnimationEnd(Animation arg0)
			{
			}

			public void onAnimationRepeat(Animation arg0)
			{
			}

			public void onAnimationStart(Animation arg0)
			{
				setVisibility(VISIBLE);
			}

		});
		startAnimation(animation1);
	}

	/** Hides the text view */
	public void hide()
	{
		setVisibility(INVISIBLE);
	}
}
