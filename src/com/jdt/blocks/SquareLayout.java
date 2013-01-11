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
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Layout derivative to enforce a square layout for the game board
 * 
 * @author Tom
 */
public class SquareLayout extends LinearLayout {
    
    boolean landscape;

    public SquareLayout(Context context, AttributeSet attributes) {
        
        super(context, attributes);
        landscape = true;

        String orientation = attributes.getAttributeValue(
                "http://schemas.android.com/apk/res/android", "orientation");
        landscape = Integer.parseInt(orientation) == 0;
    }

    public SquareLayout(Context context) {
        super(context);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        
        if (landscape == true) {
            
            int height = b - t;
            ViewGroup.LayoutParams params = this.getLayoutParams();
            params.width = height;
            this.setLayoutParams(params);
            this.setMeasuredDimension(height, height);
            super.onLayout(changed, l, t, l + height, b);
            
        } else {
            
            int width = r - l;
            ViewGroup.LayoutParams params = this.getLayoutParams();
            params.height = width;
            this.setLayoutParams(params);
            this.setMeasuredDimension(width, width);
            super.onLayout(changed, l, t, r, t + width);
        }
    }
}
