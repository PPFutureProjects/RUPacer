package com.maomao.foldmenudemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class LeaderboardActivity extends Activity{

    private static final String TAG = LeaderboardActivity.class.getSimpleName();
    private static final String ONE_DAY = "one_day";
    private static final String THREE_DAYS = "three_days";
    private static final String SEVEN_DAYS = "seven_days";
    private static final String ONE_MONTH = "one_month";
    private static final String TOTAL = "total";
    private static final String AVERAGE = "average";

    private static String mTimeMode, mQuantityMode;

    private static boolean isInitialized = false;

    private ArrayList<UserEntry> mUserEntriesList;

    private ArrayList<String> mUserName;
    private ArrayList<String> mUserID;
    private ArrayList<String> mUserProfilePictureURL;
    private ArrayList<Bitmap> mUserProfilePicture;

    private ArrayList<Integer> mTempUserScore;

    private ListView mLeaderboardList;
    private Spinner mModeSpinner;
    private ArrayAdapter mArrayAdapter;
    private Switch mModeSwitch;
    private ImageView mProfilePicture;
    private Bitmap mBitmap;

    private StepComparator mStepComparator;

    private GraphRequest mGraphRequest;
    private CallbackManager mCallbackManager;

    private RelativeLayout BG;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.fragment_leaderboard);
        BG = (RelativeLayout)findViewById(R.id.Leaderboard);

        mLeaderboardList = (ListView)findViewById(R.id.leaderboard_list);
        mModeSpinner = (Spinner)findViewById(R.id.mode_spinner);
        mModeSwitch = (Switch)findViewById(R.id.mode_switch);

        mArrayAdapter = ArrayAdapter.createFromResource(this, R.array.modes, android.R.layout.simple_spinner_item);
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mModeSpinner.setAdapter(mArrayAdapter);
        mModeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (mArrayAdapter.getItem(i).toString()) {
                    case "Today":
                        mTimeMode = ONE_DAY;
                        showLeaderboard();
                        break;
                    case "Last 3 days":
                        mTimeMode = THREE_DAYS;
                        showLeaderboard();
                        break;
                    case "Last 1 week":
                        mTimeMode = SEVEN_DAYS;
                        showLeaderboard();
                        break;
                    case "Last 1 month":
                        mTimeMode = ONE_MONTH;
                        showLeaderboard();
                        break;
                    default:
                        mTimeMode = ONE_DAY;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mModeSpinner.setVisibility(View.VISIBLE);

        mModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(isInitialized == true) {
                    if (b) {
                        mQuantityMode = AVERAGE;
                        showLeaderboard();
                    } else {
                        mQuantityMode = TOTAL;
                        showLeaderboard();
                    }
                }
            }
        });

        mTimeMode = ONE_DAY;
        mQuantityMode = TOTAL;

        mStepComparator = new StepComparator();

        if(AccessToken.getCurrentAccessToken() != null) {

            mGraphRequest = new GraphRequest(AccessToken.getCurrentAccessToken(), "me", null, HttpMethod.GET, new GraphRequest.Callback() {

                @Override
                public void onCompleted(GraphResponse response) {
                    try {
                        JSONObject jsonObject = response.getJSONObject();
                        String name = jsonObject.getString("name");
                        String id = jsonObject.getString("id");
                        ApplicationManager.setFacebookUserName(name);
                        ApplicationManager.setFacebookUserID(id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            mGraphRequest.executeAsync();
        }
    }

    public class StepComparator implements Comparator<UserEntry> {
        @Override
        public int compare(UserEntry user1, UserEntry user2) {
            double step1 = user1.getStepAverage();
            double step2 = user2.getStepAverage();
            if (step1 < step2)
                return 1;
            else
                return -1;
        }
    }

    public void getUserFriends() {
        mGraphRequest = new GraphRequest(AccessToken.getCurrentAccessToken(), "me/friends", null,HttpMethod.GET, new GraphRequest.Callback() {
            public void onCompleted(GraphResponse response) {
                Log.i("getUserFriends()", response.toString());
                try {
                    JSONObject jsonObject = response.getJSONObject();
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject object = jsonArray.getJSONObject(i);
                        String userName = object.getString("name");
                        String userID = object.getString("id");
                        mUserName.add(userName);
                        mUserID.add(userID);
                    }
                    for(int j = 0; j < mUserName.size(); j++) {
                        Log.i("getUserFriends()", mUserName.get(j) + ", " + mUserID.get(j));
                    }
                    mUserName.add(ApplicationManager.getFacebookUserName());
                    mUserID.add(ApplicationManager.getFacebookUserID());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mGraphRequest.executeAsync();
    }

    public void getUserProfilePicture() {
        Bundle params = new Bundle();
        params.putBoolean("redirect", false);
        params.putInt("height", 50);
        params.putInt("width", 50);
        for(int i = 0; i < mUserID.size(); i++) {
            Log.i("getUserProfilePicture()", String.valueOf(i));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mGraphRequest = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + mUserID.get(i) + "/picture", params, HttpMethod.GET, new GraphRequest.Callback() {
                public void onCompleted(GraphResponse response) {
                    Log.i("getUserProfilePicture()", response.toString());
                    try {
                        JSONObject jsonObject = response.getJSONObject().getJSONObject("data");
                        String url = jsonObject.getString("url");
                        Log.i("getUserProfilePicture()", url);
                        mUserProfilePictureURL.add(url);
                        new getImage().execute(url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            mGraphRequest.executeAsync();
        }
    }

    public class getImage extends AsyncTask<String, String, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                mBitmap = getProfilePicture(params[0]);
                Log.i("doInBackground ", "site=" + params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mUserProfilePicture.add(bitmap);
            Log.i("onPostExecute", "bitmap == null: " + (bitmap == null));
        }
    }

    public Bitmap getProfilePicture(String imageURL) {

        URL url;
        Bitmap bitmap = null;

        try {
            url = new URL(imageURL);
            InputStream inputStream = url.openConnection().getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            bitmap = BitmapFactory.decodeStream(bufferedInputStream);
            bufferedInputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public void initialize() {
        Log.i("initialize()", "Start");
        if(AccessToken.getCurrentAccessToken() != null) {

            getUserFriends();

            CountDownTimer timer1 = new CountDownTimer(1000, 100) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    getUserProfilePicture();
                }
            };
            timer1.start();

            CountDownTimer timer2 = new CountDownTimer(3000, 100) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    Log.i("onFinish()", "size = " + mUserID.size());
                    for (int i = 0; i < mUserID.size(); i++)
                        mUserEntriesList.add(new UserEntry(mUserName.get(i), 0, mUserProfilePicture.get(i)));
                    Log.i("onFinish()", "mUserEntriesList.size() = " + mUserEntriesList.size());
                    ApplicationManager.setUserEntriesList(mUserEntriesList);
                    for (int i = 0; i < mUserID.size(); i++) {
                        Log.i("onFinish()", "i = " + i);
                        Log.i("onFinish()", "mUserID.get(" + i + ") = " + mUserID.get(i));
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getUserSteps(mUserID.get(i), i);
                        Log.i("onFinish()", i + ".mUserTempScore.size() = " + mTempUserScore.size());
                    }
                }
            };
            timer2.start();

            CountDownTimer timer3 = new CountDownTimer(7000, 100) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "user size = " + mUserEntriesList.size());
                    showLeaderboard();
                }
            };
            timer3.start();
        }
    }

    public void onStart(){
        super.onStart();
        SharedPreferences prefs =
                this.getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);
        if(prefs.contains("color")){
             BG.setBackgroundColor(prefs.getInt("color", 0));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AccessToken.getCurrentAccessToken() != null) {
            mUserEntriesList = new ArrayList<UserEntry>();
            mUserName = new ArrayList<String>();
            mUserID = new ArrayList<String>();
            mUserProfilePictureURL = new ArrayList<String>();
            mUserProfilePicture = new ArrayList<Bitmap>();
            initialize();
        }
    }

    public void getUserSteps(String id, final int i) {

        if(mTempUserScore != null)
            mTempUserScore = null;
        Log.i(TAG, "mTempUserScore.isEmpty()" + (mTempUserScore == null));
        mTempUserScore = new ArrayList<Integer>();

        ParseQuery<ParseObject> mParseQuery = ParseQuery.getQuery("RUPacer");
        mParseQuery.whereExists("userStep");
        mParseQuery.whereEqualTo("userId", id);
        mParseQuery.orderByDescending("createdAt");
        mParseQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.i(TAG, "no error " + i);
                    Log.i(TAG, "size = " + object.size());
                    if (object.size() > 0) {
                        for (int i = 0; i < object.size(); i++) {
                            ParseObject p = object.get(i);
                            if (!(p.getInt("userStep") + "").equals("")) {
                                Log.i(TAG, String.valueOf(p.getInt("userStep")));
                                mTempUserScore.add(p.getInt("userStep"));

                            } else {

                            }
                        }
                        Log.i(TAG, "mTempUserScore.size() = " + mTempUserScore.size());
                        ApplicationManager.getUserEntries(i).setStepDaily(mTempUserScore);
                        mTempUserScore = null;
                        mTempUserScore = new ArrayList<Integer>();
                    }
                } else {
                    Log.i(TAG, e.toString());
                }
            }
        });
    }

    public void showLeaderboard(){

        String timeMode = mTimeMode;
        String quantityMode = mQuantityMode;
        calculate(timeMode);

        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < ApplicationManager.getUserEntriesList().size(); i++) {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("UserID", ApplicationManager.getUserEntries(i).getUserID());
            if(quantityMode == "total")
                hashMap.put("Step", "Total steps: " + ApplicationManager.getUserEntries(i).getStepTotal());
            else
                hashMap.put("Step", "Average steps: " + new DecimalFormat("0.00").format(ApplicationManager.getUserEntries(i).getStepAverage()));
            hashMap.put("Picture", ApplicationManager.getUserEntries(i).getProfilePicture());
            arrayList.add(hashMap);
        }
        SimpleAdapter simpleadapter = new SimpleAdapter(this,
                arrayList, R.layout.fragment_leaderboard_listitem,
                new String[]{"UserID", "Step", "Picture"},
                new int[]{R.id.leaderboard_listitem_userid, R.id.leaderboard_listitem_step, R.id.leaderboard_listitem_profilepicture});
        simpleadapter.setViewBinder(new ListViewBinder());
        mLeaderboardList.setAdapter(simpleadapter);
        isInitialized = true;
    }

    private class ListViewBinder implements SimpleAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Object o, String s) {
            if ((view instanceof ImageView) && (o instanceof Bitmap)) {
                ImageView imageView = (ImageView) view;
                Bitmap bmp = (Bitmap) o;
                imageView.setImageBitmap(bmp);
                return true;
            }
            return false;
        }
    }


    public void calculate(String timeMode) {

        int period;
        int temp;

        switch(timeMode) {
            case "one-day":
                period = 1;
                break;
            case "three_days":
                period = 3;
                break;
            case "seven_days":
                period = 7;
                break;
            case "one_month":
                period = 30;
                break;
            default:
                period = 1;
        }

        for(int i = 0; i < ApplicationManager.getUserEntriesList().size(); i++) {
            temp = 0;
            ApplicationManager.getUserEntries(i).setStepTotal(0);
            for(int j = 0; j < Math.min(ApplicationManager.getUserEntries(i).getStepDailySize(), period); j++){
                temp += ApplicationManager.getUserEntries(i).getStepDaily(j);
            }
            Log.i("calculate()", "temp = " + temp);
            Log.i("calculate()", "period = " + period);
            ApplicationManager.getUserEntries(i).setStepTotal(temp);
            ApplicationManager.getUserEntries(i).setStepAverage((double) temp / period);
        }

        Collections.sort(ApplicationManager.getUserEntriesList(), mStepComparator);
    }

}
