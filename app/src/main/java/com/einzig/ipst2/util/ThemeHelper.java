/*
 * ****************************************************************************
 * Copyright 2017 Steven Foskett, Jimmy Ho, Ryan Porterfield                  *
 * Permission is hereby granted, free of charge, to any person obtaining a    *
 * copy of this software and associated documentation files (the "Software"), *
 * to deal in the Software without restriction, including without limitation  *
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,   *
 * and/or sell copies of the Software, and to permit persons to whom the      *
 * Software is furnished to do so, subject to the following conditions:       *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included in *
 * all copies or substantial portions of the Software.                        *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE*
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER     *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING    *
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER        *
 * DEALINGS IN THE SOFTWARE.                                                  *
 ******************************************************************************/

package com.einzig.ipst2.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.einzig.ipst2.R;

/*
 * Created by Steven Foskett on 7/8/2017.
 */

public class ThemeHelper {

    public static void setSettingsTheme(Activity activity) {
        PreferencesHelper preferencesHelper = new PreferencesHelper(activity);
        if (preferencesHelper.getBool(preferencesHelper.themeKey())) {
            activity.setTheme(R.style.AppTheme_Dark);
        } else
            activity.setTheme(R.style.AppTheme);
    }

    public static boolean isDarkTheme(Context context) {
        PreferencesHelper preferencesHelper = new PreferencesHelper(context);
        return preferencesHelper.getBool(preferencesHelper.themeKey());
    }

    public static void styleButton(Button button, Context context) {
        if (isDarkTheme(context)) {
            button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_dark));
        } else {
            button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg));
        }
    }

    public static void styleRadioButton(CompoundButton rb, Context context, boolean isChecked) {
        if (isDarkTheme(context)) {
            if (isChecked) {
                rb.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_dark));
                rb.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                rb.setBackground(null);
                rb.setTextColor(ContextCompat.getColor(context, R.color.white));
            }
        } else {
            Logger.d("BUTTON: " + rb.getText().toString() + " " + isChecked);
            if (isChecked) {
                rb.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg));
                rb.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                rb.setBackground(null);
                rb.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            }
        }
    }

    public static int getDialogTheme(Context context) {
        PreferencesHelper preferencesHelper = new PreferencesHelper(context);
        if(preferencesHelper.getBool(preferencesHelper.themeKey()))
            return R.style.AboutDialog_dark;
        else
            return R.style.AboutDialog;
    }
}
