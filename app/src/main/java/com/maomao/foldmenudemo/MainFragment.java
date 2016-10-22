package com.maomao.foldmenudemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.ProfilePictureView;
import com.parse.Parse;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainFragment extends Fragment implements SensorEventListener {

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }*/

    final static String TAG="gw3";

    private static final String PARSE_APPLICATION_ID = "6XoDR3RpvpUXljoY1oitG8HBvRSOgL4oxxtEMxrp";
    private static final String PARSE_CLIENT_KEY = "QY0XA5buE93SyZSmmRBaVkgk0s5V1M23PZa6CTd9";

    private TextView stepsView, totalView, averageView;


    private int todayOffset, total_start, goal, since_boot, total_days;
    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());


    private ProfileTracker profileTracker;
    private ProfilePictureView profilePictureView;
    private TextView greeting;
    private TextView addressTextView;

    private CurrentWeather mCurrentWeather;


    private Bitmap mBitmap;

    private View v;

    @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.iconImageView) ImageView mIconImageView;
    @InjectView(R.id.refreshImageView) ImageView mRefreshImageView;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUI();
                Log.v(TAG, "MARK1");
            }
        };
    }

    public void onStart(){
        super.onStart();
        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);
        if(prefs.contains("color")){
            v.setBackgroundColor(prefs.getInt("color",0));
        }
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_main, null);
        stepsView = (TextView) v.findViewById(R.id.steps);
        totalView = (TextView) v.findViewById(R.id.total);
        //averageView = (TextView) v.findViewById(R.id.average);
        addressTextView = (TextView) v.findViewById(R.id.addressTextView);

        profilePictureView = (ProfilePictureView) v.findViewById(R.id.profilePicture);
        greeting = (TextView) v.findViewById(R.id.greeting);

        updateUI();
        Log.v(TAG, "MARK2");


        mTemperatureLabel = (TextView) v.findViewById(R.id.temperatureLabel);
        mHumidityValue = (TextView) v.findViewById(R.id.humidityValue);
        mPrecipValue = (TextView) v.findViewById(R.id.precipValue);
        mIconImageView = (ImageView) v.findViewById(R.id.iconImageView);
        mRefreshImageView = (ImageView) v.findViewById(R.id.refreshImageView);
        mProgressBar =(ProgressBar) v.findViewById(R.id.progressBar);

        ButterKnife.inject(getActivity());

        mProgressBar.setVisibility(View.INVISIBLE);



        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final double latitude = ((MainActivity) getActivity()).getLatitude();
                final double longitude = ((MainActivity) getActivity()).getLongitude();
                getForecast(latitude, longitude);
                final String address = (((MainActivity) getActivity()).getAddress());
                //Log.v("address: ", address);
                addressTextView.setText(address);
                try {
                    updateImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //getForecast(latitude, longitude);

        Log.d(TAG, "Main UI code is running!");

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        //getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);

        Database db = Database.getInstance(getActivity());

        if (BuildConfig.DEBUG) db.logState();
        // read todays offset
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);

       // goal = prefs.getInt("goal", Fragment_Settings.DEFAULT_GOAL);
        since_boot = db.getCurrentSteps(); // do not use the value from the sharedPreferences
        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        if (!prefs.contains("pauseCount")) {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_UI, 0);
        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();

    }


    @Override
    public void onPause() {
        super.onPause();
        try {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Database db = Database.getInstance(getActivity());
        db.saveCurrentSteps(since_boot);
        db.close();
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // won't happen
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (BuildConfig.DEBUG)
            Logger.log("UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                    event.values[0]);
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) event.values[0];
            Database db = Database.getInstance(getActivity());
            db.insertNewDay(Util.getToday(), (int) event.values[0]);
            db.close();
        }
        since_boot = (int) event.values[0];
        updatePie();
        updateUI();
        int steps_today = Math.max(todayOffset + since_boot, 0);
        //Log.v("Facebook", "FacebookID = " + ApplicationManager.getFacebookUserID());

        if(AccessToken.getCurrentAccessToken() != null) {
            //Log.v("Facebook", "AccessToken = null" + (AccessToken.getCurrentAccessToken() == null));
            ((MainActivity)getActivity()).getUserDataThroughGraphAPI();
        }

        Date.setDate();

        if (((MainActivity)getActivity()).getID() == true) {
            if (ParseManager.retrieve(ApplicationManager.getFacebookUserID(), Date.getYear(), Date.getMonth(), Date.getDay()) == false)
                ParseManager.update(ApplicationManager.getFacebookUserID(), steps_today, Date.getYear(), Date.getMonth(), Date.getDay());
            else
                ParseManager.upload(ApplicationManager.getFacebookUserID(), steps_today, Date.getYear(), Date.getMonth(), Date.getDay());
        }

        //uploadScore(String.valueOf(steps_today));
    }

    /**
     * Updates the pie graph to show todays steps/distance as well as the
     * yesterday and total values. Should be called when switching from step
     * count to distance.
     */
    private void updatePie() {
        if (BuildConfig.DEBUG) Logger.log("UI - update steps: " + since_boot);
        // todayOffset might still be Integer.MIN_VALUE on first start
        int steps_today = Math.max(todayOffset + since_boot, 0);

        stepsView.setText(formatter.format(steps_today));
        totalView.setText(formatter.format(total_start + steps_today));
    }


    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;
        Profile profile = Profile.getCurrentProfile();
        if (enableButtons && profile != null) {
            profilePictureView.setProfileId(profile.getId());
            greeting.setText(getString(R.string.hello_user, profile.getFirstName()));
        } else {
            profilePictureView.setProfileId(null);
            greeting.setText(null);
        }
    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = "27974c4bc33201748eaf542a6769c3b7";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(getActivity(), getString(R.string.network_unavailable_message),
                    Toast.LENGTH_LONG).show();
        }
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        //Log.d(TAG, currentWeather.getFormattedTime());

        return currentWeather;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");

        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateImage() throws IOException {

        Bitmap bitmap = ((MainActivity)getActivity()).getBitmap();
        if(bitmap!=null) {
            extractProminentColors(bitmap);
        }
    }

    //extract prominent colors
    /*
    compile 'com.android.support:palette-v7:23.0.1'
    is needed in Gradle dependency
     */
    private void extractProminentColors(Bitmap bitmap){
        int defaultColor = 0xb4b4b4;

        Palette p = Palette.from(bitmap).generate();

        int VibrantColor = p.getVibrantColor(defaultColor);
        //Log.v(TAG,"VibrantColor: " + String.format("#%X", VibrantColor));
        int VibrantColorDark = p.getDarkVibrantColor(defaultColor);
        //Log.v(TAG,"VibrantColorDark: " + String.format("#%X", VibrantColorDark));
        int VibrantColorLight = p.getLightVibrantColor(defaultColor);
        //Log.v(TAG,"VibrantColorLight: " + String.format("#%X", VibrantColorLight));
        int MutedColor = p.getMutedColor(defaultColor);
        //Log.v(TAG,"MutedColor: " + String.format("#%X", MutedColor));
        int MutedColorDark = p.getDarkMutedColor(defaultColor);
        //Log.v(TAG, "MutedColorDark: " + String.format("#%X", MutedColorDark));
        int MutedColorLight = p.getLightMutedColor(defaultColor);
        //Log.v(TAG,"MutedColorLight: " + String.format("#%X", MutedColorLight));
        v.setBackgroundColor(MutedColorLight);
        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);
        prefs.edit().putInt("color",MutedColorLight).apply();
    }
}
