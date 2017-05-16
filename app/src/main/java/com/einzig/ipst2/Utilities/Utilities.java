package com.einzig.ipst2.Utilities;

import com.einzig.ipst2.Constants;

/**
 * Created by steve on 5/15/2017.
 */

public class Utilities {

    public static void print_debug(String message)
    {
        if(Constants.debug)
            System.out.println(message);
    }
}
