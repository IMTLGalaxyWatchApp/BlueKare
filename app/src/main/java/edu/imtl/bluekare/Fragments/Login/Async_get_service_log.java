package edu.imtl.bluekare.Fragments.Login;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import edu.imtl.bluekare.R;

import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserAccessToken;

public class Async_get_service_log extends AsyncTask<Void, Void, String> {

    private final WeakReference<Context> contextRef;


    public Async_get_service_log(Context context)
    {
        contextRef = new WeakReference<>(context);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("Async", "Async_get_service_log: "+s);
        JSONObject obj;
        try {
            obj = new JSONObject(s);
            JSONObject obj2 = obj.getJSONObject("data");
            Log.e("asdf", String.valueOf(obj2));

            JSONArray arr = obj2.getJSONArray("services");
            Log.e("asdf", String.valueOf(arr));

            StringBuilder services= new StringBuilder();
            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = contextRef.get().getApplicationContext().getSharedPreferences("current_device_info", 0);

            int device_id = pref.getInt("cur_device_id", -1);
            editor = pref.edit();
            int counter=0;
            for(int i=0; i<arr.length(); i++)
            {
                int d_id = arr.getJSONObject(i).getInt("device_id");
                int id = arr.getJSONObject(i).getInt("id");
                int farm_id=arr.getJSONObject(i).getInt("farm_id");
                int service_cd=arr.getJSONObject(i).getInt("service_type_cd");

                Log.e("asdf", String.valueOf(d_id));
                Log.e("asdf", String.valueOf(id));
                Log.e("asdf", String.valueOf(farm_id));
                Log.e("asdf", String.valueOf(service_cd));

                services.append(id).append(",");

                if(device_id == d_id && farm_id==14 && service_cd==10){
                    Log.e("asdf", "BREAK");
                    editor.putInt("cur_service_id", id);
                    editor.apply();
                }
                counter++;

            }
            editor.putString("all_service_list", services.toString());
            editor.putInt("len_service_list", counter);
            editor.apply();

        } catch (JSONException e) {                e.printStackTrace();            }

    }

    @Override
    protected String doInBackground(Void... voids) {

        try {
            URL url = new URL(contextRef.get().getResources().getString(R.string.hippo_abs_server)+contextRef.get().getResources().getString(R.string.register_service));
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", getUserAccessToken(contextRef.get().getApplicationContext()));
            httpURLConnection.setRequestProperty("content-type", "application/json");

            httpURLConnection.setDoInput(true);
            int status_code = httpURLConnection.getResponseCode();
            String status = httpURLConnection.getResponseMessage();

            Log.d("Status", status_code+": "+status+"");
            String line;
            String page = "";
            BufferedReader reader;
            if(status_code == HttpURLConnection.HTTP_OK){                    reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));                }
            else{                                                           reader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream(), "UTF-8"));                }
            while((line = reader.readLine()) != null){                        page += line+"\n";                    }
            Log.d("page", page);
            return page;

        } catch (IOException e){
            e.printStackTrace();
        } catch (Exception e){
            Log.e("Error", e.getMessage());
        }
        return null;
    }
}