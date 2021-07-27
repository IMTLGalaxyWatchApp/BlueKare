package edu.imtl.bluekare.Main;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

import edu.imtl.bluekare.Fragments.Login.LoginActivity;
import edu.imtl.bluekare.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserAccessToken;

public class Async_get_registration extends AsyncTask<Void, Void, String> {

    private final WeakReference<Context> contextRef;
    String name;
    String email;
    String dob;
    String gender;

    public Async_get_registration(Context context) {
        contextRef = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("Async", "GETUSERINFO: "+s);
        try {
            JSONObject obj = new JSONObject(s);
            JSONObject obj2 = obj.getJSONObject("data");
            JSONObject arr = obj2.getJSONObject("user");

            name=arr.getString("name");
            email=arr.getString("uid");
            dob=arr.getString("birth");
            gender=arr.getString("gender");

            Log.e("async_get_username", name);
            Log.e("async_get_username", email);
            Log.e("async_get_username", dob);
            Log.e("async_get_username", gender);

            Intent intent_main = new Intent(contextRef.get(), MainActivity.class);
            intent_main.putExtra("name", name);
            intent_main.putExtra("uid", email);
            intent_main.putExtra("dob", dob);
            intent_main.putExtra("gender", gender);
            intent_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            contextRef.get().startActivity(intent_main);

            ((Activity) contextRef.get()).finish();


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected String doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        String url_path = contextRef.get().getResources().getString(R.string.hippo_abs_server)+contextRef.get().getResources().getString(R.string.registration);

        Request request = new Request.Builder()
                .url(url_path)
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization",  Objects.requireNonNull(getUserAccessToken(contextRef.get().getApplicationContext())))
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.e("Async_get_registration", response.message());

            String res = response.body().string();
            Log.e("Async_get_registration", res);
            return res;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";

    }
}
