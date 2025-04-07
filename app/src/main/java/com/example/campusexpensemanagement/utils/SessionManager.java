package com.example.campusexpensemanagement.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    // Shared preferences file name
    private static final String PREF_NAME = "CampusExpenseManagementPref";

    // All Shared Preferences Keys
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String KEY_REMEMBERED_USERNAME = "rememberedUsername";
    private static final String KEY_REMEMBERED_PASSWORD = "rememberedPassword";

    // Shared Preferences
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(int userId, String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get stored session data
     */
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    /**
     * Save remember me credentials
     */
    public void setRememberMe(String username, String password) {
        editor.putBoolean(KEY_REMEMBER_ME, true);
        editor.putString(KEY_REMEMBERED_USERNAME, username);
        editor.putString(KEY_REMEMBERED_PASSWORD, password);
        editor.commit();
    }
    /**
     * Clear remember me credentials
     */
    public void clearRememberMe() {
        editor.putBoolean(KEY_REMEMBER_ME, false);
        editor.remove(KEY_REMEMBERED_USERNAME);
        editor.remove(KEY_REMEMBERED_PASSWORD);
        editor.commit();
    }

    /**
     * Check if remember me is enabled
     */
    public boolean isRememberMeEnabled() {
        return pref.getBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Get remembered username
     */
    public String getRememberedUsername() {
        return pref.getString(KEY_REMEMBERED_USERNAME, "");
    }

    /**
     * Get remembered password
     */
    public String getRememberedPassword() {
        return pref.getString(KEY_REMEMBERED_PASSWORD, "");
    }

    /**
     * Clear session details
     */
    public void clearSession() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }
}