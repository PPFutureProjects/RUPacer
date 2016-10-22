package com.maomao.foldmenudemo;

import java.util.Calendar;

public class Date {

    public static int year;
    public static int month;
    public static int day;

    public static void setDate() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DATE);
    }

    public static int getYear() {
        return year;
    }

    public static int getMonth() {
        return month;
    }

    public static int getDay() {
        return day;
    }
}
