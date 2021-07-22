package edu.imtl.bluekare.Fragments.Register;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

import edu.imtl.bluekare.Fragments.Login.LoginActivity;
import edu.imtl.bluekare.R;

import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserAccessToken;
import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserRenewalToken;
import static edu.imtl.bluekare.Fragments.Login.utils.saveToken;

public class Async_register_user_task extends AsyncTask<Void, Void, String> {

    private final WeakReference<Context> contextRef;
    String name;
    String email;
    String pw;
    String pwc;
    int type;
    String phone;
    int gender;
    String dob;

    public Async_register_user_task(Context context, String name, String email, String pw, String pwc, int type, String phone, int gender, String dob)
    {
        contextRef = new WeakReference<>(context);
        this.name = name;
        this.email = email;
        this.pw = pw;
        this.pwc = pwc;
        this.type = type;
        this.phone = phone;
        this.gender = gender;
        this.dob = dob;


    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("Async", "LOGINTASK: "+s);

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
                Intent intent_main = new Intent(contextRef.get(), LoginActivity.class);
                intent_main.putExtra("email", email);
                intent_main.putExtra("pw", pw);
                intent_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                contextRef.get().startActivity(intent_main);

                ((Activity) contextRef.get()).finish();

            }catch (JSONException e){
                e.printStackTrace();
            }
        }else if(s.contains("Invalid id or password") || s.contains("401")){
            Toast.makeText(contextRef.get(), "아이디, 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected String doInBackground(Void... voids) {

        String result ="";
        InputStream inputStream = null;

        try {
            URL url = new URL(contextRef.get().getResources().getString(R.string.hippo_abs_server)+contextRef.get().getResources().getString(R.string.registration));
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


            String json;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("uid", email);
            jsonObject.put("password", pw);
            jsonObject.put("password_confirmation", pwc);
            jsonObject.put("type", type);
            jsonObject.put("phonenum", phone);
            jsonObject.put("gender", gender);
            jsonObject.put("birth", dob);



            JSONObject parent_js = new JSONObject();
            parent_js.put("user", jsonObject);

            json = parent_js.toString();

            Log.e("asdf", json);

            httpURLConnection.setRequestMethod("POST");
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
