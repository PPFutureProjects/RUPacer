package com.maomao.foldmenudemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

public class ParseManager extends Activity{

    private static final String TAG = ParseManager.class.getSimpleName();

    private static ParseQuery<ParseObject> mParseQuery;

    private static final String PARSE_APPLICATION_ID = "6XoDR3RpvpUXljoY1oitG8HBvRSOgL4oxxtEMxrp";
    private static final String PARSE_CLIENT_KEY = "QY0XA5buE93SyZSmmRBaVkgk0s5V1M23PZa6CTd9";

    public static boolean isNew = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int step = 100000;
        int step2 = 12345;
        String id = "1104640126226748";

        int year = 2015;
        int month = 12;
        int day = 2;

        /*
        upload(id, step, year, month, day);
        retrieve(id, year, month, day);
        update(id, step2, year, month, day);
        */
    }

    public static void upload(String id, int step, int year, int month, int day) {

        final ParseObject mParseObject = new ParseObject("RUPacer");
        mParseObject.put("userId", id);
        mParseObject.put("userStep", step);
        mParseObject.put("year", year);
        mParseObject.put("month", month);
        mParseObject.put("day", day);

        Log.i(TAG, "saveInBackground");
        mParseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    String objectId = mParseObject.getObjectId();
                    if (objectId != null) {
                        Log.i(TAG, "objectId = null: " + objectId);
                    }
                } else
                    Log.i(TAG, e.toString());
            }
        });
    }

    public static boolean retrieve(String id, int year, int month, int day) {

        ParseQuery<ParseObject> mParseQuery = ParseQuery.getQuery("RUPacer");
        mParseQuery.whereExists("userStep");
        mParseQuery.whereEqualTo("userId", id);
        mParseQuery.whereEqualTo("year", year);
        mParseQuery.whereEqualTo("month", month);
        mParseQuery.whereEqualTo("day", day);
        mParseQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.i(TAG, "no error");
                    Log.i(TAG, "size = " + object.size());
                    isNew = true;
                    if (object.size() > 0) {
                        isNew = false;
                        for (int i = 0; i < object.size(); i++) {
                            ParseObject p = object.get(i);
                            if (!(p.getInt("userStep") + "").equals("")) {
                                Log.i(TAG, String.valueOf(p.getInt("userStep")));
                            } else {

                            }
                        }
                    }
                } else {
                    Log.i(TAG, e.toString());
                    isNew = true;
                }
            }
        });

        return isNew;
    }

    public static void update(String id, int step, int year, int month, int day) {

        final int currentStep = step;

        ParseQuery<ParseObject> mParseQuery = ParseQuery.getQuery("RUPacer");
        mParseQuery.whereExists("userStep");
        mParseQuery.whereEqualTo("userId", id);
        mParseQuery.whereEqualTo("year", year);
        mParseQuery.whereEqualTo("month", month);
        mParseQuery.whereEqualTo("day", day);
        mParseQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    if (object.size() > 0) {
                        for (int i = 0; i < object.size(); i++) {
                            ParseObject p = object.get(i);
                            if (!(p.getInt("userStep") + "").equals("")) {
                                p.put("userStep", currentStep);
                                p.saveInBackground();
                            } else {

                            }
                        }
                    }
                }
            }
        });
    }

}
