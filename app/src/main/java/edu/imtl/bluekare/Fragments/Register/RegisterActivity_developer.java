package edu.imtl.bluekare.Fragments.Register;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import edu.imtl.bluekare.R;

public class RegisterActivity_developer extends AppCompatActivity {
    private Button register;
    private RadioButton doctor, researcher, developer;
    private EditText editText;

    private int type=3; //developer=0 researcher=1 doctor=2 general user=3
    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_developer);
        register=findViewById(R.id.enterBtn);
        doctor=findViewById(R.id.radioD);
        researcher=findViewById(R.id.radioR);
        developer=findViewById(R.id.radioDv);
        editText=findViewById(R.id.verificationCode);

        researcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doctor.setEnabled(false);
                developer.setEnabled(false);
                type=1;
            }
        });
        doctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                researcher.setEnabled(false);
                developer.setEnabled(false);
                type=2;
            }
        });
        developer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                researcher.setEnabled(false);
                doctor.setEnabled(false);
                type=0;
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code=editText.getText().toString();
                if(type==3) Toast.makeText(getApplicationContext(),"회원 타입을 선택해주세요.",Toast.LENGTH_SHORT).show();
                else{
                    if(code.equals("12345678imtl")==true){
                        Intent it = new Intent(RegisterActivity_developer.this,RegisterActivity.class);
                        it.putExtra("type", type);
                        startActivity(it);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"인증 코드가 일치하지 않습니다.",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
    }
}