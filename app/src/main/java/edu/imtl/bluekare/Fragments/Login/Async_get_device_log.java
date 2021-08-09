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
import java.util.Objects;

import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserAccessToken;


public class Async_get_device_log extends AsyncTask<Void, Void, String> {

    int register_device=1;
    private final WeakReference<Context> contextRef;
    String device_id;
    String device_name;


    public Async_get_device_log(Context context, String device_id, String device_name)
    {
        contextRef = new WeakReference<>(context);
        this.device_id = device_id;
        this.device_name = device_name;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("Async", "Async_get_device_log: "+s);
        JSONObject obj;
        try {
            obj = new JSONObject(s);
            JSONObject obj2 = obj.getJSONObject("data");
            Log.e("asdf", String.valueOf(obj2));

            JSONArray arr = obj2.getJSONArray("devices");
            Log.e("asdf", String.valueOf(arr));

            for(int i=0; i<arr.length(); i++)
            {
                String d_name = arr.getJSONObject(i).getString("name");
                String d_id = arr.getJSONObject(i).getString("device_id");
                int id = arr.getJSONObject(i).getInt("id");

                Log.e("asdf", d_name);
                Log.e("asdf", d_id);
                Log.e("asdf", String.valueOf(id));

                if(device_id.equals(d_id)){
                    register_device=0;
                    Log.e("asdf", "BREAK");

                    SharedPreferences pref;
                    SharedPreferences.Editor editor;

                    pref = contextRef.get().getApplicationContext().getSharedPreferences("current_device_info", 0);
                    editor = pref.edit();
                    editor.putInt("cur_device_id", id);
                    editor.apply();

                    Async_get_service_log async_get_service_log = new Async_get_service_log(contextRef.get());
                    async_get_service_log.execute();

                }

            }
        } catch (JSONException e) {                e.printStackTrace();            }

        if(register_device==1){
            Async_register_device_task async_register_device_task = new Async_register_device_task(contextRef.get(), device_id, device_name);
            async_register_device_task.execute();
        }
    }

    @Override
    protected String doInBackground(Void... voids) {

        try {
            URL url = new URL(contextRef.get().getResources().getString(edu.imtl.bluekare.R.string.hippo_abs_server)+contextRef.get().getResources().getString(edu.imtl.bluekare.R.string.create_device));
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
            Log.e("Error", Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }
}
