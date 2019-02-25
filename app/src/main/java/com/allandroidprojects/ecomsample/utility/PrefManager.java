package com.allandroidprojects.ecomsample.utility;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Lincoln on 05/05/16.
 */
public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences constants
    private static final String PREF_NAME = "MyPreference";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    private static final String USER_NAME = "UserName";
    private static final String AUTHENTICATE = "IsAuthenticated";
    private static final String USER_ID = "UserId";


    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setUserName(String userName) {
        editor.putString(USER_NAME, userName);
        editor.commit();
    }

    public void setUserId(String userId) {
        editor.putString(USER_ID, userId);
        editor.commit();
    }

    public String getUserId() {
        return pref.getString(USER_ID, "-1");
    }

    public String getUserName() {
        return pref.getString(USER_NAME, "Hello World!");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    public void setIsLoggedIn(Boolean flag) {
        editor.putBoolean(IS_LOGGED_IN, flag);
        editor.commit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setAuthenticate(boolean authenticate){
        editor.putBoolean(AUTHENTICATE, authenticate);
        editor.commit();
    }

    public boolean isAuthenticated(){
        return pref.getBoolean(AUTHENTICATE, false);
    }
}
