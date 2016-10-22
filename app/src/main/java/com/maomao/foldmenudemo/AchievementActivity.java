package com.maomao.foldmenudemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AchievementActivity extends Activity{

    private static final String TAG = AchievementActivity.class.getSimpleName();

    private static int mDailySteps;
    private static int mTotalSteps;
    private static int mDaysOver10000;

    private ArrayList<UserEntry> mUserEntriesList;

    private ListView mAchievementList;
    private ImageView mProfilePicture;
    private TextView mStepsInLastFiveDays;

    private GraphRequest mGraphRequest;
    private CallbackManager mCallbackManager;

    private ArrayList<String> mAchievementTitle;
    private ArrayList<String> mAchievementDescription;
    private ArrayList<Boolean> isAchievementFinished;
    private ArrayList<Integer> mUserScore;

    private LinearLayout BG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.fragment_achievement);
        BG = (LinearLayout)findViewById(R.id.Achievement);

        mAchievementList = (ListView)findViewById(R.id.achievement_list);
         mStepsInLastFiveDays = (TextView)findViewById(R.id.steps_in_last_five_days);

        mAchievementTitle = new ArrayList<String>();
        mAchievementDescription = new ArrayList<String>();
        isAchievementFinished = new ArrayList<Boolean>();
        mUserScore = new ArrayList<Integer>();

        Date.setDate();

        for (int i = 0; i < 21; i++) {
            isAchievementFinished.add(false);
        }

        ApplicationManager.setFacebookAppID(getResources().getString(R.string.facebook_app_id));
        query();

        new CountDownTimer(2000, 100){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
               Log.i(TAG, "mUserScore.size() = " + mUserScore.size());
               if (mUserScore.size() > 0) {
                   initialize();
                   calculateAchievement();
                   showAchievement();
               }
            }
        }.start();
    }

    public void onStart(){
        super.onStart();
        SharedPreferences prefs =
                this.getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);
        if(prefs.contains("color")){
            BG.setBackgroundColor(prefs.getInt("color", 0));
        }
    }

    public void query() {
        ParseQuery<ParseObject> mParseQuery = ParseQuery.getQuery("RUPacer");
        mParseQuery.whereExists("userStep");
        mParseQuery.whereEqualTo("userId", ApplicationManager.getFacebookUserID());
        mParseQuery.orderByDescending("createdAt");
        mParseQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.i(TAG, "no error");
                    Log.i(TAG, "size = " + object.size());
                    if (object.size() > 0) {
                        for (int i = 0; i < object.size(); i++) {
                            ParseObject p = object.get(i);
                            if (!(p.getInt("userStep") + "").equals("")) {
                                mUserScore.add(p.getInt("userStep"));
                                Log.i(TAG, String.valueOf(p.getInt("userStep")));
                            } else {

                            }
                        }
                        Log.i(TAG, "mUserScore.size() = " + mUserScore.size());
                    }
                } else {
                    Log.i(TAG, e.toString());
                }
            }
        });
    }

    public void initialize() {

        mAchievementTitle.add(getResources().getString(R.string.boots_i));
        mAchievementTitle.add(getResources().getString(R.string.boots_ii));
        mAchievementTitle.add(getResources().getString(R.string.boots_iii));
        mAchievementTitle.add(getResources().getString(R.string.boots_iv));
        mAchievementTitle.add(getResources().getString(R.string.boots_v));
        mAchievementTitle.add(getResources().getString(R.string.stamina_i));
        mAchievementTitle.add(getResources().getString(R.string.stamina_ii));
        mAchievementTitle.add(getResources().getString(R.string.stamina_iii));
        mAchievementTitle.add(getResources().getString(R.string.stamina_iv));
        mAchievementTitle.add(getResources().getString(R.string.stamina_v));
        mAchievementTitle.add(getResources().getString(R.string.stamina_vi));
        mAchievementTitle.add(getResources().getString(R.string.marathon_i));
        mAchievementTitle.add(getResources().getString(R.string.marathon_ii));
        mAchievementTitle.add(getResources().getString(R.string.marathon_iii));
        mAchievementTitle.add(getResources().getString(R.string.marathon_iv));
        mAchievementTitle.add(getResources().getString(R.string.marathon_v));
        mAchievementTitle.add(getResources().getString(R.string.continual_i));
        mAchievementTitle.add(getResources().getString(R.string.continual_ii));
        mAchievementTitle.add(getResources().getString(R.string.continual_iii));
        mAchievementTitle.add(getResources().getString(R.string.continual_iv));
        mAchievementTitle.add(getResources().getString(R.string.continual_v));

        mAchievementDescription.add(getResources().getString(R.string.boots_i_description));
        mAchievementDescription.add(getResources().getString(R.string.boots_ii_description));
        mAchievementDescription.add(getResources().getString(R.string.boots_iii_description));
        mAchievementDescription.add(getResources().getString(R.string.boots_iv_description));
        mAchievementDescription.add(getResources().getString(R.string.boots_v_description));
        mAchievementDescription.add(getResources().getString(R.string.stamina_i_description));
        mAchievementDescription.add(getResources().getString(R.string.stamina_ii_description));
        mAchievementDescription.add(getResources().getString(R.string.stamina_iii_description));
        mAchievementDescription.add(getResources().getString(R.string.stamina_iv_description));
        mAchievementDescription.add(getResources().getString(R.string.stamina_v_description));
        mAchievementDescription.add(getResources().getString(R.string.stamina_vi_description));
        mAchievementDescription.add(getResources().getString(R.string.marathon_i_description));
        mAchievementDescription.add(getResources().getString(R.string.marathon_ii_description));
        mAchievementDescription.add(getResources().getString(R.string.marathon_iii_description));
        mAchievementDescription.add(getResources().getString(R.string.marathon_iv_description));
        mAchievementDescription.add(getResources().getString(R.string.marathon_v_description));
        mAchievementDescription.add(getResources().getString(R.string.continual_i_description));
        mAchievementDescription.add(getResources().getString(R.string.continual_ii_description));
        mAchievementDescription.add(getResources().getString(R.string.continual_iii_description));
        mAchievementDescription.add(getResources().getString(R.string.continual_iv_description));
        mAchievementDescription.add(getResources().getString(R.string.continual_v_description));
    }

    public void showAchievement(){

        String s = "Steps of last 5 days: ";

        for (int i = 0; i < 5; i++) {
            if(i != 4) {
                if (i < mUserScore.size())
                    s += mUserScore.get(i) + ",";
                else
                    s += 0 + ",";
            }
            else {
                if (i < mUserScore.size())
                    s += mUserScore.get(i);
                else
                    s += 0;
            }
        }

        mStepsInLastFiveDays.setText(s);

        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < mAchievementTitle.size(); i++) {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("Title", mAchievementTitle.get(i));
            hashMap.put("Description", mAchievementDescription.get(i));
            if(isAchievementFinished.get(i))
                hashMap.put("Icon", R.drawable.achievement_finished);
            else
                hashMap.put("Icon", null);
            arrayList.add(hashMap);
        }
        Log.i("showAchievement()", "Success");
        SimpleAdapter simpleadapter = new SimpleAdapter(this,
                arrayList, R.layout.fragment_achievement_listitem,
                new String[]{"Title", "Description", "Icon"},
                new int[]{R.id.achievement_listitem_title, R.id.achievement_listitem_description, R.id.achievement_listitem_picture});
        mAchievementList.setAdapter(simpleadapter);
    }

    public void calculateAchievement() {

        /*Initialization*/

        mDailySteps = 0; //for "Boots made for walking"
        mTotalSteps = 0; //for "Marathon"
        mDaysOver10000 = 0; //for "Stamina"

        mDailySteps = mUserScore.get(0);

        for(int i = 0; i < mUserScore.size(); i++){
            mTotalSteps += mUserScore.get(i);
            if(mUserScore.get(i) > 10000)
                mDaysOver10000++;
        }

        /*Calculation for "Boots made for walking"*/

        if(mDailySteps > 25000)
            isAchievementFinished.set(4, true);
        if(mDailySteps > 20000)
            isAchievementFinished.set(3, true);
        if(mDailySteps > 15000)
            isAchievementFinished.set(2, true);
        if(mDailySteps > 10000)
            isAchievementFinished.set(1, true);
        if(mDailySteps > 5000)
            isAchievementFinished.set(0, true);

        /*Calculation for "Marathon"*/

        if(mTotalSteps > 1000000)
            isAchievementFinished.set(15, true);
        if(mTotalSteps > 750000)
            isAchievementFinished.set(14, true);
        if(mTotalSteps > 500000)
            isAchievementFinished.set(13, true);
        if(mTotalSteps > 200000)
            isAchievementFinished.set(12, true);
        if(mTotalSteps > 100000)
            isAchievementFinished.set(11, true);

        /*Calculation for "Stamina"*/

        if(mDaysOver10000 > 100)
            isAchievementFinished.set(10, true);
        if(mDaysOver10000 > 60)
            isAchievementFinished.set(9, true);
        if(mDaysOver10000 > 30)
            isAchievementFinished.set(8, true);
        if(mDaysOver10000 > 15)
            isAchievementFinished.set(7, true);
        if(mDaysOver10000 > 10)
            isAchievementFinished.set(6, true);
        if(mDaysOver10000 > 5)
            isAchievementFinished.set(5, true);

        /*Calculation for "Continual"*/

        if(mUserScore.size() >= 30)
            isAchievementFinished.set(20, true);
        if(mUserScore.size() >= 15)
            isAchievementFinished.set(19, true);
        if(mUserScore.size() >= 7)
            isAchievementFinished.set(18, true);
        if(mUserScore.size() >= 3)
            isAchievementFinished.set(17, true);
        if(mUserScore.size() >= 1)
            isAchievementFinished.set(16, true);
    }
}