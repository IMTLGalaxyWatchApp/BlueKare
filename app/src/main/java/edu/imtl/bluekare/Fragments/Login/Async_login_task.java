package edu.imtl.bluekare.Fragments.Login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import edu.imtl.bluekare.Main.Async_get_registration;
import edu.imtl.bluekare.Main.MainActivity;

import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserAccessToken;
import static edu.imtl.bluekare.Fragments.Login.SaveUserData.getUserRenewalToken;
import static edu.imtl.bluekare.Fragments.Login.utils.saveToken;

public class Async_login_task extends AsyncTask<Void, Void, String> {

    private final WeakReference<Context> contextRef;
    String id;
    String pw;
    String device_id;
    String device_name;
    int remember_flag;

    public Async_login_task(Context context, String id, String pw, String device_id, String device_name, int remember_flag)
    {
        contextRef = new WeakReference<>(context);
        this.id = id;
        this.pw = pw;

        this.device_id = device_id;
        this.device_name = device_name;
        this.remember_flag = remember_flag;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("Async", "LOGINTASK: "+s);

        if(s==null){ Log.d("asdf","login error");}

        else if(s.contains("data")){
            //receving user token&renewal token
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

                //간단한 값으로 DB 는 부담일 때 사용, 초기설정값 or 자동로그인 여부 저장할 때 사용, 어플리케이션 삭제 전 까지 보존
                SharedPreferences remember_pref = contextRef.get().getSharedPreferences("renewal_token_pref", 0);
                SharedPreferences.Editor editor = remember_pref.edit();
                if(remember_flag == 1){
                    editor.putString("renew_token", renewal_token);
                    editor.apply();
                }

                editor.putString("auto_renew_token", renewal_token);
                editor.apply();


                Async_get_device_log async_get_device_log = new Async_get_device_log(contextRef.get(), device_id, device_name);
                async_get_device_log.execute();

                Async_get_registration async_get_registration = new Async_get_registration(contextRef.get());
                async_get_registration.execute();

                Intent intent_main = new Intent(contextRef.get(), MainActivity.class);
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

        try {
            URL url = new URL(contextRef.get().getResources().getString(edu.imtl.bluekare.R.string.hippo_abs_server)+contextRef.get().getResources().getString(edu.imtl.bluekare.R.string.login));
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            String json;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uid", id);
            jsonObject.put("password", pw);

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
            String line;
            String page = "";
            BufferedReader reader;
            if(status_code == HttpURLConnection.HTTP_OK){
                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));

            }
            else reader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream(), "UTF-8"));
            while((line = reader.readLine()) != null) page += line+"\n";
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