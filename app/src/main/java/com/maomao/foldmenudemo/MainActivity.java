package com.maomao.foldmenudemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.parse.Parse;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemLongClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements OnMenuItemClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    final static String TAG="gwMain";
    private CallbackManager callbackManager;
    private FragmentManager fragmentManager;
    private ContextMenuDialogFragment mMenuDialogFragment;

    private String ID,ImageUrl;
    private Bitmap mBitmap;
    private ImageLoader imageLoader;

    private File mFile;

    private GoogleApiClient mGoogleApiClient;
    private final static int RC_RESOLVE = 1;

    /*address*/

    private Location mLastLocation = null;
    private Location mCurrentLocation;

    protected String mAddressOutput;
    protected boolean mAddressRequested;
    private AddressResultReceiver mResultReceiver;

    private LocationRequest mLocationRequest;

    private double latitude = 40.5209;
    private double longitude =-74.4560;

    public boolean isIdGet;
    //image
    private Bitmap image;

    private static final String PARSE_APPLICATION_ID = "6XoDR3RpvpUXljoY1oitG8HBvRSOgL4oxxtEMxrp";
    private static final String PARSE_CLIENT_KEY = "QY0XA5buE93SyZSmmRBaVkgk0s5V1M23PZa6CTd9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);


        callbackManager = CallbackManager.Factory.create();

        startService(new Intent(this, SensorListener.class));

        fragmentManager = getSupportFragmentManager();
        initToolbar();
        initMenuFragment();
        addFragment(new MainFragment(), true, R.id.container);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();

        if (!getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS).contains("timezone")) {
            getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS).edit()
                    .putString("timezone", TimeZone.getDefault().getID()).commit();
        }

        mAddressRequested = false;
        mResultReceiver = new AddressResultReceiver(new Handler());
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(5 * 1000); // 1 second, in milliseconds
        Date.setDate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        if (BuildConfig.DEBUG) Logger.log("Main::onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AccessToken.getCurrentAccessToken() != null) {
            getUserDataThroughGraphAPI();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (BuildConfig.DEBUG) Logger.log("Main::onStop");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public GoogleApiClient getGC() {
        return mGoogleApiClient;
    }

    public void beginSignIn() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.e("Facebook-Login-Success", loginResult.toString());

                /*Only use it under Facebook authority
                if (!AccessToken.getCurrentAccessToken().getPermissions().contains("publish_actions")) {
                    LoginManager.getInstance().logInWithPublishPermissions(MainActivity.this, Arrays.asList("publish_actions"));
                }*/


                if (!AccessToken.getCurrentAccessToken().getPermissions().contains("user_friends")) {
                    LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("user_friends"));
                }
                getUserDataThroughGraphAPI();
                friendsList();

            }

            @Override
            public void onCancel() {
                Log.e("Facebook-Login-Success", "dd");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("Facebook-Login-Success", exception.toString());
            }
        });
    }

    @Override
    public void onConnected(final Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //Log.v(TAG,mLastLocation.getLongitude()+"");
        // Gets the best and most recent location currently available,
        // which may be null in rare cases when a location is not available.

        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                return;
            }
            if (mAddressRequested) {
                startIntentService();
            }
        } else {
            startLocationUpdates();
        }
        startLocationUpdates();
        if(mLastLocation != null){
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            // This problem can be fixed. So let's try to fix it.
            try {
                // launch appropriate UI flow (which might, for example, be the
                // sign-in flow)
                connectionResult.startResolutionForResult(this, RC_RESOLVE);
            } catch (IntentSender.SendIntentException e) {
                // Try connecting again
                mGoogleApiClient.connect();
            }
        } else {
            if (!isFinishing() && !isDestroyed()) {
                GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0)
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void getUserDataThroughGraphAPI() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object,GraphResponse response) {
                        //Log.e(TAG,object.toString() + "-"+ response.toString());
                        try {
                            if (object!= null) {
                                ID = object.getString("id");
                                Log.i("getUserData()", "id = " + ID);
                                isIdGet = true;
                                ApplicationManager.setFacebookUserID(ID);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.v(TAG,ID);

                        if (response != null) {
                            try {
                                JSONObject data = response.getJSONObject();
                                if (data.has("picture")) {
                                    ImageUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");
                                    //Log.v(TAG,ImageUrl);
                                    //new getImage().execute(ImageUrl);
                                    // set profilePic bitmap to imageview

                                    getFacebookProfilePicture(ImageUrl);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if(mBitmap!=null){
                            Log.v(TAG,"LLL");
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields","id,name,link,email,gender,birthday,location,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }


    private void friendsList() {
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/friends", null, HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        //get Friend name, id
                        //Log.e("Friends List: 1", response.toString());
                    }
                }).executeAsync();

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        startIntentService();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        this.startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
            }
            mAddressRequested = false;
        }

    }

    public boolean getID() {
        return isIdGet;
    }

    public String getAddress(){
        return mAddressOutput;
    }

    public double getLatitude(){
        //Log.e(TAG, "EE" + mLastLocation.getLatitude());
        return latitude;

    }

    public double getLongitude(){
        return longitude;
    }

    public Bitmap getFacebookProfilePicture(String ImageUrl) throws IOException {
        //url = new URL(ImageUrl);
        //Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        imageLoader.loadImage(ImageUrl, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String ImageUrl, View view, Bitmap loadedImage) {
                // Do whatever you want with Bitmap
                mBitmap = loadedImage;
                storeBitmap(mBitmap);
            }
        });
        return mBitmap;
    }

    public Bitmap getBitmap() throws IOException {
        if(ImageUrl==null) {
            saveProfile();
            mBitmap = BitmapFactory.decodeFile(mFile.toString());
        }
        return mBitmap;
    }

    public File saveProfile(){
        mFile = new File(this.getExternalFilesDir(null),"pic.jpg");
        return mFile;
    }

    public void storeBitmap(Bitmap image){
        File pictureFile = saveProfile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
    }

    private List<MenuObject> getMenuObjects() {

        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.ic_close);

        MenuObject signinic = new MenuObject("Sign in");
        signinic.setResource(R.drawable.ic_signin);

        MenuObject leaderboardic = new MenuObject("Leaderboard");
        leaderboardic.setResource(R.drawable.ic_leaderboard);

        MenuObject achievementic = new MenuObject("Achievements");
        achievementic.setResource(R.drawable.ic_achievement);

        MenuObject goalic = new MenuObject("Goal");
        goalic.setResource(R.drawable.ic_goal);

        MenuObject shareic = new MenuObject("Share");
        shareic.setResource(R.drawable.ic_share);



        menuObjects.add(close);
        menuObjects.add(signinic);
        menuObjects.add(leaderboardic);
        menuObjects.add(achievementic);
        menuObjects.add(goalic);
        menuObjects.add(shareic);
        return menuObjects;
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mToolBarTextView = (TextView) findViewById(R.id.text_view_toolbar_title);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolBarTextView.setText("RUPacer");
    }

    protected void addFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        invalidateOptionsMenu();
        String backStackName = fragment.getClass().getName();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(containerId, fragment, backStackName)
                    .setTransition(FragmentTransaction.TRANSIT_NONE);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_menu:
                if (fragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
                    mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mMenuDialogFragment != null && mMenuDialogFragment.isAdded()) {
            mMenuDialogFragment.dismiss();
        } else{
            finish();
        }
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        //Toast.makeText(this, "Clicked on position: " + position, Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder;
        final SharedPreferences prefs =
                this.getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);

        if(clickedView.getParent()!=null){
            ((ViewGroup)clickedView.getParent()).removeView(clickedView);
        }
        switch (position){
            case 1:{
                builder = new AlertDialog.Builder(this);

                clickedView = this.getLayoutInflater().inflate(R.layout.signin, null);
                builder.setView(clickedView);
                clickedView.findViewById(R.id.sign_in_button);
                final Dialog d = builder.create();
                Log.v(TAG, "OK2");

                clickedView.findViewById(R.id.sign_in_button)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                // start the asynchronous sign in flow
                                beginSignIn();
                                d.dismiss();

                            }
                        });
                d.show();
                Log.v(TAG, "OK6");
                break;
            }
            case 2:{
                startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
                break;
            }
            case 3:{
                startActivity(new Intent(MainActivity.this, AchievementActivity.class));
                break;
            }

            case 4:{
                builder = new AlertDialog.Builder(this);
                final NumberPicker np = new NumberPicker(this);
                np.setMinValue(1);
                np.setMaxValue(100000);
                np.setValue(prefs.getInt("goal", 10000));
                builder.setView(np);
                builder.setTitle(R.string.set_goal);
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                np.clearFocus();
                                prefs.edit().putInt("goal", np.getValue()).apply();
                                dialog.dismiss();
                                startService(
                                        new Intent(getApplication(), SensorListener.class)
                                                .putExtra("updateNotificationState", true));
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                Dialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
                break;
            }
            case 5:{
                postPicture();
                break;
            }
        }
    }

    public void postPicture() {
            //save the screenshot
            View rootView = findViewById(android.R.id.content).getRootView();
            rootView.setDrawingCacheEnabled(true);
            // creates immutable clone of image
            image = Bitmap.createBitmap(rootView.getDrawingCache());
            // destroy
            rootView.destroyDrawingCache();

            //share dialog
            AlertDialog.Builder shareDialog = new AlertDialog.Builder(this);
            shareDialog.setTitle("Share Screen Shot");
            shareDialog.setMessage("Share your step record to Facebook?");
            shareDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //share the image to Facebook
                    SharePhoto photo = new SharePhoto.Builder().setBitmap(image).build();
                    SharePhotoContent content = new SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .build();
                    ShareApi.share(content, null);
                }
            });
            shareDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            shareDialog.show();
    }

}
