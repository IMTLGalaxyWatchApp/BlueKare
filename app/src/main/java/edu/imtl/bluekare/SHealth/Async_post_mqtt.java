package edu.imtl.bluekare.SHealth;
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
import java.net.MalformedURLException;
import java.net.URL;
import edu.imtl.bluekare.R;

import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserAccessToken;

public class Async_post_mqtt extends AsyncTask<Void, Void, String>{
    private final WeakReference<Context> contextRef;
    String uid, name, dob, gender,phone;
    int type;
    JSONObject health;

    public Async_post_mqtt(Context context, String[] user_info,int type, JSONObject health) {
        contextRef = new WeakReference<>(context);
        this.uid = user_info[0];
        this.name=user_info[1];
        this.dob=user_info[2];
        this.gender=user_info[3];
        this.phone=user_info[4];
        this.type=type;
        this.health=health;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("Async", "Post MQTT "+s);
    }
    @Override
    protected String doInBackground(Void... voids) {

        try {
            URL url = new URL(contextRef.get().getResources().getString(R.string.hippo_abs_server)+contextRef.get().getResources().getString(R.string.post_mqtt));
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


            String json;
            String topic="UP."+uid+"|dtx|14|10";
            JSONObject jsonObject = new JSONObject();
            JSONObject payload=new JSONObject();
            jsonObject.put("topic", topic);
            jsonObject.put("payload", payload);

            payload.put("user_name", name);
            payload.put("user_phone", phone);
            payload.put("user_gender", gender);
            payload.put("user_type",type);
            payload.put("health_content", health);

            json = jsonObject.toString();

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
