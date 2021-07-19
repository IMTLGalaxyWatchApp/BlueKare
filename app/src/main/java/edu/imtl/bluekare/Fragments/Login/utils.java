package edu.imtl.bluekare.Fragments.Login;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class utils {
    public static void saveToken(String acc, String renewal, Context context){
        SharedPreferences userPref = context.getSharedPreferences("User_token", MODE_PRIVATE); //특정 이름의 SP 생성
        SharedPreferences.Editor editor = userPref.edit();
        editor.putString("access_token", acc+""); //키, 값으로 저장
        editor.putString("renewal_token", renewal+"");
        editor.apply();
    }
}