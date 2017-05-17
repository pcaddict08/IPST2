package com.einzig.ipst2.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.einzig.ipst2.Activities.MainActivity;
import com.einzig.ipst2.Constants;

/**
 * Created by steve on 5/15/2017.
 */

public class Utilities {

    public static void print_debug(String message) {
        if (Constants.debug)
            System.out.println(message);
    }

    public static void show_warning(final String title, final String message, final Context context) {
        try {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle(title);
                    alertDialogBuilder
                            .setMessage(message)
                            .setCancelable(true)
                            .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
