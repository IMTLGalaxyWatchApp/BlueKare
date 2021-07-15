package edu.imtl.bluekare.Fragments.Login;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.Socket;
import java.nio.channels.Channel;

import edu.imtl.bluekare.MainActivity;
import edu.imtl.bluekare.R;

public class LoginActivity extends AppCompatActivity {
    EditText id, password;
    Button loginbtn;
    TextView register;
    DeviceInfo deviceInfo;
    RadioButton rembtn;

    String device_id;
    String device_name;
    int remember_flag;

    String uid, upw;

    final String tag="asdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        id = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginbtn = findViewById(R.id.btn_login);
        register = findViewById(R.id.toRegister);
        rembtn=findViewById(R.id.rememBtn);

        deviceInfo = new DeviceInfo(Build.BOARD, Build.BRAND, Build.CPU_ABI, Build.DEVICE, Build.DISPLAY,
                Build.FINGERPRINT, Build.HOST, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), Build.MANUFACTURER, Build.MODEL, Build.PRODUCT,
                Build.TAGS, Build.TYPE, Build.USER, Build.VERSION.RELEASE);
        deviceInfo.logging();

        device_id = deviceInfo.getId();
        device_name = deviceInfo.fingerprint;

        remember_flag=1; // remember login

        rembtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remember_flag*=-1;
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uid=id.getText().toString();
                upw=password.getText().toString();
                Log.e(tag, uid);
                Log.e(tag, upw);
                Async_login_task async_login_task = new Async_login_task(LoginActivity.this, uid, upw, device_id, device_name, remember_flag);
                async_login_task.execute();

            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();

        loginbtn.setEnabled(true);
    }

}