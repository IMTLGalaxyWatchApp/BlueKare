package edu.imtl.bluekare.Fragments.Survey;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import edu.imtl.bluekare.R;
import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserAccessToken;
public class Async_post_mqtt_survey extends AsyncTask<Void, Void, String>{

    private final WeakReference<Context> contextRef;
    String uid, patient_name, patient_phone;
    int type;
    JSONObject survey;

    public Async_post_mqtt_survey(Context context, String[] user_info, JSONObject survey) {
        contextRef = new WeakReference<>(context);
        this.uid = user_info[0];
        this.patient_name=user_info[1];
        this.patient_phone=user_info[2];

        this.survey=survey;
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

            Date date=new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String getTime = sdf.format(date);

            String json;
            String topic="UP."+uid+"|dtx|14|10";
            JSONObject jsonObject = new JSONObject();
            JSONObject payload=new JSONObject();
            jsonObject.put("topic", topic);
            jsonObject.put("payload", payload);
            payload.put("content","survey");

            payload.put("patient_name", patient_name);
            payload.put("patient_phone", patient_phone);

            payload.put("date",getTime);
            payload.put("survey_content", survey);

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
