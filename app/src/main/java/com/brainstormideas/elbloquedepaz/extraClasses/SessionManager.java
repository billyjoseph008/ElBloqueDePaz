package com.brainstormideas.elbloquedepaz.extraClasses;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.brainstormideas.elbloquedepaz.Login;
import com.brainstormideas.elbloquedepaz.configuracion;

import java.util.HashMap;

public class SessionManager {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "Pref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    public static final String KEY_NUMERO = "numero";


    public static final String KEY_CONFIGURED = "configured";

    public static final String KEY_UID = "uid";

    public static final String KEY_LLAMAR = "llamar";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String uid, String name, String numero,  String email) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        editor.putBoolean(KEY_CONFIGURED, false);
        editor.putBoolean(KEY_LLAMAR, false);
        // Storing name in pref
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_NUMERO, numero);
        // Storing email in pref
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_UID, uid);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, Login.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }
    public void checkConfigured() {
        // Check login status
        if (!this.isConfigured() && this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, configuracion.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }


    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, Login.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() { return pref.getBoolean(IS_LOGIN, false); }
    public boolean isConfigured(){ return pref.getBoolean(KEY_CONFIGURED, false); }
    public boolean isLlamar(){return pref.getBoolean(KEY_LLAMAR, false);}
    public String getNumero(){return pref.getString(KEY_NUMERO, "3327257746");}

    public void setConfigured(){
        editor.putBoolean(KEY_CONFIGURED, true);
        editor.commit();
    }
    public void setNumero(String num){
        editor.putString(KEY_NUMERO, num);
        editor.commit();
    }

    public void setLlamar(Boolean set){
        editor.putBoolean(KEY_LLAMAR, set);
        editor.commit();
    }


}