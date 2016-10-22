package com.maomao.foldmenudemo;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class UserEntry
{
    private String mUserID;
    private int mStepTotal;
    private double mStepAverage;
    private ArrayList<Integer> mStepDaily;
    private Bitmap mProfilePicture;

    public UserEntry() {
        this.mStepTotal = 0;
        this.mStepAverage = 0;
        this.mStepDaily = new ArrayList<Integer>();
    }

    public UserEntry(String userID) {
        this.mUserID = userID;
        this.mStepTotal = 0;
        this.mStepAverage = 0;
        this.mStepDaily = new ArrayList<Integer>();
    }

    public UserEntry(String userID, int stepTotal) {
        this.mUserID = userID;
        this.mStepTotal = stepTotal;
        this.mStepAverage = 0;
        this.mStepDaily = new ArrayList<Integer>();
    }

    public UserEntry(String userID, int stepTotal, Bitmap profilePicture) {
        this.mUserID = userID;
        this.mStepTotal = stepTotal;
        this.mStepAverage = 0;
        this.mProfilePicture = profilePicture;
        this.mStepDaily = new ArrayList<Integer>();
    }

    public void setUserID(String userID) {
        this.mUserID = userID;
    }

    public String getUserID() {
        return mUserID;
    }

    public void setStepTotal(int step) {
        this.mStepTotal = step;
    }

    public int getStepTotal() {
        return mStepTotal;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.mProfilePicture = profilePicture;
    }

    public Bitmap getProfilePicture() {
        return mProfilePicture;
    }

    public void setStepDaily(ArrayList<Integer> step) {
        if(mStepDaily != null)
            mStepDaily = new ArrayList<Integer>();
        for(int i = 0; i < step.size(); i++)
            mStepDaily.add(step.get(i));
    }

    public int getStepDaily(int i) {
        return mStepDaily.get(i);
    }

    public int getStepDailySize() {
        return mStepDaily.size();
    }

    public void setStepAverage(double i) {
        this.mStepAverage = i;
    }

    public double getStepAverage() {
        return mStepAverage;
    }
}

