package edu.imtl.bluekare.Fragments.Login;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SaveUserData {
    public static String getUserAccessToken(Context context){
        try {
            SharedPreferences pref = context.getSharedPreferences("User_token", MODE_PRIVATE);
            return pref.getString("access_token", "");
        }catch (Exception e){

        }
        return null;
    }

    public static String getUserRenewalToken(Context context){
        SharedPreferences pref = context.getSharedPreferences("User_token", MODE_PRIVATE);
        return pref.getString("renewal_token", "");
    }

}