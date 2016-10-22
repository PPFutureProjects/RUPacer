package com.maomao.foldmenudemo;

import java.util.ArrayList;

public class ApplicationManager {

    private static final String TAG = "GoddessWalker";

    private static String facebookUserName = null;
    private static String facebookUserID = null;
    private static String facebookAppID = null;
    private static UserEntry currentFacebookUser;
    private static ArrayList<UserEntry> mUserEntriesList;

    public static String getFacebookUserName() {
        return facebookUserName;
    }

    public static void setFacebookUserName(String userName) {
        facebookUserName = userName;
    }

    public static String getFacebookUserID() {
        return facebookUserID;
    }

    public static void setFacebookUserID(String userID) {
        facebookUserID = userID;
    }

    public static String getFacebookAppID() {
        return facebookAppID;
    }

    public static void setFacebookAppID(String appID) {
        facebookAppID = appID;
    }

    public static UserEntry getCurrentFacebookUser() {
        return currentFacebookUser;
    }

    public static void setCurrentFacebookUser(UserEntry facebookUser) {
        currentFacebookUser = facebookUser;
    }

    public static ArrayList<UserEntry> getUserEntriesList() {
        return mUserEntriesList;
    }

    public static UserEntry getUserEntries(int index) {
        if (mUserEntriesList != null && mUserEntriesList.size() > index) {
            return mUserEntriesList.get(index);
        } else {
            return null;
        }
    }

    public static void setUserEntriesList(ArrayList<UserEntry> userEntriesList) {
        mUserEntriesList = userEntriesList;
    }
}
