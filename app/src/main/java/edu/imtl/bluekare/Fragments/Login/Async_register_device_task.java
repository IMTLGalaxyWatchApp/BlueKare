package edu.imtl.bluekare.Fragments.Login;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.imtl.bluekare.R;

import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserAccessToken;

public class Async_register_device_task extends AsyncTask<Void, Void, String> {

    private final WeakReference<Context> contextRef;
    int device_num_id;
    String device_id;
    String device_name;


    public Async_register_device_task(Context context, String device_id, String device_name)
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
        Log.d("Async", "Async_register_device_task: "+s);

        JSONObject obj;
        try {
            obj = new JSONObject(s);
            JSONObject obj2 = obj.getJSONObject("data");
            Log.e("asdf", String.valueOf(obj2));
            device_num_id = obj2.getInt("id");

            Async_register_service_task async_register_service_task = new Async_register_service_task(contextRef.get(), device_num_id);
            async_register_service_task.execute();


        } catch (JSONException e) {                e.printStackTrace();            }



    }


    @Override
    protected String doInBackground(Void... voids) {

        String result ="";
        InputStream inputStream = null;

        try {
            URL url = new URL(contextRef.get().getResources().getString(R.string.hippo_abs_server)+contextRef.get().getResources().getString(R.string.create_device));
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


            String json;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("device_id", device_id);
            jsonObject.put("name", device_name);



            JSONObject parent_js = new JSONObject();
            parent_js.put("device", jsonObject);

            json = parent_js.toString();

            Log.e("asdf", json);

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Authorization", getUserAccessToken(contextRef.get().getApplicationContext()));
            httpURLConnection.setRequestProperty("content-type", "application/json");

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(json.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int status_code = httpURLConnection.getResponseCode();
            String status = httpURLConnection.getResponseMessage();
            Log.d("Status", status_code+": "+status+"");
            String page = "";       String line;
            if(status_code == HttpURLConnection.HTTP_OK){

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                while((line = reader.readLine()) != null){                page += line+"\n";                   }
            }else{
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream(), "UTF-8"));
                while((line = reader.readLine()) != null){                page += line+"\n";                   }
            }
            Log.d("page", page);
            return page;

        } catch (IOException | JSONException e){
            e.printStackTrace();
        } catch (Exception e){
            Log.e("Error", e.getMessage());
        }
        return null;
    }
}
