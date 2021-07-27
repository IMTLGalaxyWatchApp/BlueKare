package edu.imtl.bluekare.Fragments.Register;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import edu.imtl.bluekare.Fragments.Login.DeviceInfo;
import edu.imtl.bluekare.R;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    RadioButton male_button;
    RadioButton female_button;
    EditText name_t;
    EditText email_t;
    EditText pw_t;
    EditText pwc_t;
    EditText phone_t;
    EditText dob_t;
    Button register_button;
    TextView tologin_button;

    String name;
    String email;
    String pw;
    String pwc;
    String phone;
    String dob;
    String device_id;
    String device_name;

    DeviceInfo deviceInfo;

    int mYear,mMonth,mDay;

    int type=3;
    int gender=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        deviceInfo = new DeviceInfo(Build.BOARD, Build.BRAND, Build.CPU_ABI, Build.DEVICE, Build.DISPLAY,
                Build.FINGERPRINT, Build.HOST, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), Build.MANUFACTURER, Build.MODEL, Build.PRODUCT,
                Build.TAGS, Build.TYPE, Build.USER, Build.VERSION.RELEASE);


        deviceInfo.logging();


        device_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        device_name = Settings.Global.getString(getContentResolver(), "device_name");

        Log.e("asdf", device_id+"///"+device_name);
        name_t = findViewById(R.id.rt_name);
        email_t = findViewById(R.id.rt_email);
        pw_t = findViewById(R.id.rt_password);
        pwc_t = findViewById(R.id.rt_repassword);
        phone_t = findViewById(R.id.rt_phoneNum);
        dob_t=findViewById(R.id.rt_birth);

        male_button = findViewById(R.id.male_button);
        female_button = findViewById(R.id.female_button);
        register_button = findViewById(R.id.btn_register);
        tologin_button=findViewById(R.id.toLogin);

        dob_t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RegisterActivity.this, DatePickerActivity.class);
                startActivityForResult(intent,1);
            }
        });


        male_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                female_button.setChecked(false);
                gender=0;
            }
        });

        female_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                male_button.setChecked(false);
                gender=1;
            }
        });


        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ErrorMsg = "";

                name = name_t.getText().toString();
                email = email_t.getText().toString();
                pw = pw_t.getText().toString();
                pwc = pwc_t.getText().toString();
                phone = phone_t.getText().toString();
                dob = dob_t.getText().toString();

                if(email.isEmpty() || email == null){
                    ErrorMsg += "Email";
                    //Toast.makeText(getApplicationContext(), "이메일을 적어주세요", Toast.LENGTH_SHORT).show();
                }
                if(pw.isEmpty()){
                    if(ErrorMsg.isEmpty())  ErrorMsg += "Password";
                    else ErrorMsg += ", Password";
                    //Toast.makeText(getApplicationContext(), "비밀번호를 적어주세요", Toast.LENGTH_SHORT).show();
                }
                if(name.isEmpty()){
                    if(ErrorMsg.isEmpty())  ErrorMsg += "Name";
                    else ErrorMsg += ", Name";
                    //Toast.makeText(getApplicationContext(), "성함을 적어주세요", Toast.LENGTH_SHORT).show();
                }
                if(ErrorMsg.length() > 2)
                    Toast.makeText(getApplicationContext(), "Please Check: "+ErrorMsg, Toast.LENGTH_SHORT).show();
                else
                {
                    Async_register_user_task async_register_user_task = new Async_register_user_task(RegisterActivity.this, name, email, pw, pwc, type, phone, gender, dob);
                    async_register_user_task.execute();


                }

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode != RESULT_OK) {
                Log.e("asdf","intent error");
                return;
            }
            String converted_month=String.valueOf(data.getExtras().getInt("mMonth"));
            String converted_day=String.valueOf(data.getExtras().getInt("mDay"));
            if(data.getExtras().getInt("mMonth")<10) converted_month="0"+converted_month;
            if(data.getExtras().getInt("mDay")<10) converted_day="0"+converted_day;

            String sendText = data.getExtras().getInt("mYear")+"-"+converted_month+"-"+converted_day;
            dob_t.setText(sendText);
        }
    }
}