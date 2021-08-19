package edu.imtl.bluekare.Fragments.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.imtl.bluekare.Fragments.Register.RegisterActivity;
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

        remember_flag=0; // remember login

        rembtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rembtn.isChecked()) remember_flag=1;
                else remember_flag=0;

                Log.d("asdflogin",Integer.toString(remember_flag));
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
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup_intent = new Intent(getBaseContext(), RegisterActivity.class);
                startActivity(signup_intent);
                finish();
            }
        });

        String temp_email = getIntent().getStringExtra("email");
        String temp_pw = getIntent().getStringExtra("pw");

        if(temp_email!=null && temp_email.length() > 2 && temp_pw.length() > 2)
        {   Log.e("asdf","async_login");
            Async_login_task async_login_task = new Async_login_task(LoginActivity.this, temp_email, temp_pw, device_id, device_name, remember_flag);
            async_login_task.execute();
        }


        SharedPreferences remember_pref = getApplicationContext().getSharedPreferences("renewal_token_pref", 0);
        String temp_renewal_token = remember_pref.getString("auto_renew_token", "none");
        if(!temp_renewal_token.equals("none"))
        {   Log.e("asdf","async_renew");
            Async_renew_session_task async_renew_session_task = new Async_renew_session_task(LoginActivity.this, temp_renewal_token, true);
            async_renew_session_task.execute();
        }


    }


    @Override
    protected void onResume() {
        super.onResume();

        loginbtn.setEnabled(true);
    }

}