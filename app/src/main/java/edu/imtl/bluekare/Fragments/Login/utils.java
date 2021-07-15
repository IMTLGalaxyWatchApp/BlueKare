package edu.imtl.bluekare.Fragments.Login;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class utils {
    public static void saveToken(String acc, String renewal, Context context){
        SharedPreferences userPref = context.getSharedPreferences("User_token", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPref.edit();
        editor.putString("access_token", acc+"");
        editor.putString("renewal_token", renewal+"");
        editor.apply();
    }
}