package edu.imtl.bluekare.Fragments.Download;
import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserAccessToken;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.imtl.bluekare.Main.MainActivity;
import edu.imtl.bluekare.R;

public class Async_fetch_mongodb_survey extends AsyncTask<Void, Void, String>{
    private final WeakReference<Context> contextRef;
    String name, date,sex,dob,phone;

    Calendar cal=new GregorianCalendar();


    public Async_fetch_mongodb_survey(Context context, String[] info) {
        contextRef = new WeakReference<>(context);
        name=info[0];
        date=info[1];
        sex=info[2];
        phone=info[3];

    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("Async", "Fetch MongoDB Survey"+s);
        Toast.makeText(contextRef.get(), "다운로드 완료", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL(contextRef.get().getResources().getString(R.string.hippo_abs_server)+contextRef.get().getResources().getString(R.string.fetch_mongo));
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            String json;
            JSONObject jsonObject = new JSONObject();
            JSONObject payload=new JSONObject();

            jsonObject.put("service_id", 14);
            jsonObject.put("service_type_cd", 10);
            jsonObject.put("from_ts", Long.parseLong(date));
            jsonObject.put("to_ts", cal.getTimeInMillis());
            jsonObject.put("filter", payload);
            jsonObject.put("skip", 0);
            jsonObject.put("limit", 1000000000);
            jsonObject.put("order", "asc");
            payload.put("payload.content", "survey");
            if(!name.isEmpty()) payload.put("payload.patient_name",name);
            if(!phone.isEmpty()) payload.put("payload.patient_phone",phone);

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

