package org.mqstack.mvideo.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by USER on 2016/1/16.
 */
public class TextResourceReader {

    private static final String TAG = "TextResourceReader";

    public static String readTextFileFromResource(Context context, int resourceId) {
        StringBuilder body = new StringBuilder();

        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            Log.d(TAG, "Could not open resource" + resourceId);
        } catch (Resources.NotFoundException nfe){
            Log.d(TAG, "Resource not found:" + resourceId + nfe);
        }

        return body.toString();
    }
}
