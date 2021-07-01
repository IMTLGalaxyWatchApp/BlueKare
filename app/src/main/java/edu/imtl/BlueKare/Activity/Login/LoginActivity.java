package edu.imtl.BlueKare.Activity.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.imtl.BlueKare.Activity.MainActivity;
import edu.imtl.BlueKare.R;

public class LoginActivity extends AppCompatActivity {
    EditText id, password;
    Button loginbtn;
    TextView register;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateLisnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mFirebaseAuth = FirebaseAuth.getInstance();
        id = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginbtn = findViewById(R.id.btn_login);
        register = findViewById(R.id.toRegister);


        mAuthStateLisnter = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser!=null) {
                    if(mFirebaseUser.isEmailVerified()) startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
            }
        };


        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = id.getText().toString();
                String pswrd = password.getText().toString();
                if (email.isEmpty()) {
                    id.setError("Please insert Email");
                    id.requestFocus();
                } else if (pswrd.isEmpty()) {
                    password.setError("Please insert Password");
                    password.requestFocus();
                } else {
                    loginbtn.setEnabled(false);
                    mFirebaseAuth.signInWithEmailAndPassword(email, pswrd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                if(user.isEmailVerified()){ // 그리고 그때 그 계정이 실제로 존재하는 계정인지
                                    Log.d("login", "signInWithEmail:success" + user.getEmail());
                                    Toast.makeText(LoginActivity.this, "signInWithEmail:success.", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(LoginActivity.this , MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }else{
                                    Toast.makeText(LoginActivity.this, "인증이 되지 않은 이메일입니다 해당 이메일 주소에서 링크를 클릭해주세요", Toast.LENGTH_SHORT).show();
                                    loginbtn.setEnabled(true);
                                }

                            } else {
                                Toast.makeText(LoginActivity.this, "Login Failed, Try again!", Toast.LENGTH_SHORT).show();
                                loginbtn.setEnabled(true);
                            }
                        }
                    });
                    //loginbtn.setEnabled(true);
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateLisnter);
    }


    @Override
    protected void onResume() {
        super.onResume();

        loginbtn.setEnabled(true);
    }

}