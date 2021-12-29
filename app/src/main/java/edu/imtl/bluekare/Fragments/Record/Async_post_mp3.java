package edu.imtl.bluekare.Fragments.Record;

import android.os.AsyncTask;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Async_post_mp3 extends AsyncTask<Void, Void, String> {
    private final WeakReference<Context> contextRef;
    String uid, patient_name, d_name,filepath, filename;
    int type;

    public Async_post_mp3(Context context, String[] patient_info, String filepath, String filename) {
        contextRef = new WeakReference<>(context);

        this.patient_name=patient_info[0];
        this.d_name =patient_info[1];

        this.filepath=filepath;
        this.filename=filename;


    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("Async", "Post MP3 "+s);
    }
    @Override
    protected String doInBackground(Void... voids) {

        try {
            String page = "";
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("multipart/form-data");
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("mp3_file",filename,
                            RequestBody.create(MediaType.parse("audio/mpeg"),
                                    new File(filepath)))
                    .addFormDataPart("d_name",d_name)
                    .addFormDataPart("p_name",patient_name)
                    .build();
            Request request = new Request.Builder()
                    .url("http://jdi.bitzflex.com:4005/voice/send_voice_data")
                    .method("POST", body)
                    .addHeader("Content-Type", "multipart/form-data")

                    .build();
            Response response = client.newCall(request).execute();

            return response.message();

        } catch (Exception e){
            Log.e("Error", e.getMessage());
        }



        return null;
    }
}
