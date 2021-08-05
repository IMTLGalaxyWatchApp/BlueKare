package edu.imtl.bluekare.Fragments.Login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.imtl.bluekare.Main.Async_get_registration;
import edu.imtl.bluekare.Main.MainActivity;
import edu.imtl.bluekare.R;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserAccessToken;
import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserRenewalToken;
import static edu.imtl.bluekare.Fragments.Login.utils.saveToken;

public class Async_renew_session_task extends AsyncTask<Void, Void, String> {

    private final WeakReference<Context> contextRef;
    String renewal_token;
    Boolean scheduled;

    public Async_renew_session_task(Context context, String renewal_token, Boolean scheduled)
    {
        contextRef = new WeakReference<>(context);
        this.renewal_token = renewal_token;
        this.scheduled = scheduled;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("Async", "Async_renew_session_task: "+s);

        if(s.contains("data")){
            String accToken = getUserAccessToken(contextRef.get().getApplicationContext());
            String renewalToken = getUserRenewalToken(contextRef.get().getApplicationContext());
            try {
                JSONObject jsonObject_parent = new JSONObject(s);
                JSONObject jsonObject = jsonObject_parent.getJSONObject("data");

                String acc_token = jsonObject.getString("access_token");
                String renewal_token = jsonObject.getString("renewal_token");

                Log.d("acc token", acc_token);
                Log.d("renewal token", renewal_token);
                if(!acc_token.equals(accToken) || !renewal_token.equals(renewalToken)) {
                    saveToken(acc_token, renewal_token, contextRef.get().getApplicationContext());
                }
                SharedPreferences remember_pref = contextRef.get().getSharedPreferences("renewal_token_pref", 0);
                SharedPreferences.Editor editor = remember_pref.edit();
                editor.putString("auto_renew_token", renewal_token);
                editor.apply();

                if(scheduled){
                    Async_get_registration async_get_registration = new Async_get_registration(contextRef.get());
                    async_get_registration.execute();

//                    Intent intent_main = new Intent(contextRef.get(), MainActivity.class);
//                    intent_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    contextRef.get().startActivity(intent_main);
//
//                    ((Activity) contextRef.get()).finish();
                }


            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    protected String doInBackground(Void... voids) {


        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(contextRef.get().getResources().getString(R.string.hippo_abs_server)+contextRef.get().getResources().getString(R.string.renew))
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", renewal_token)
                .build();

        String return_string = "";
        try {
            Response response = client.newCall(request).execute();
            Log.e("Response Async_renew_session_task", response.message());
            return_string = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }



        return return_string;
    }
}
