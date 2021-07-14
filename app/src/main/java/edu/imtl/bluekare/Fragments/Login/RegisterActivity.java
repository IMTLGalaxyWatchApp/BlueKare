package edu.imtl.bluekare.Fragments.Login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import edu.imtl.bluekare.R;

public class RegisterActivity extends AppCompatActivity {
    EditText id, password, repassword, fullname;
    Button registerbtn;
    TextView login;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore fstore;
    String userID;

    private static final String TAG = "TAGG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFirebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        id = findViewById(R.id.rt_email);
        password = findViewById(R.id.rt_password);
        repassword = findViewById(R.id.rt_repassword);

        registerbtn = findViewById(R.id.btn_register);
        login = findViewById(R.id.toLogin);
        fullname = findViewById(R.id.rt_name);

        registerbtn.setOnClickListener(v -> {

            String email = id.getText().toString();
            String pswrd = password.getText().toString();
            String repswrd = repassword.getText().toString();
            String fullName = fullname.getText().toString();


            if (email.isEmpty()) {
                id.setError("Please insert Email");
                id.requestFocus();
            } else if (fullName.isEmpty()) {
                fullname.setError("Please insert Name");
                fullname.requestFocus();
            } else if (pswrd.isEmpty()) {
                password.setError("Please insert Password");
                password.requestFocus();
            } else if (repswrd.isEmpty()) {
                repassword.setError("Please insert Password");
                repassword.requestFocus();
            } else {
                if (!(password.getText().toString().equals(repassword.getText().toString()))) {
                    Toast.makeText(RegisterActivity.this, "Please check your password", Toast.LENGTH_SHORT).show();
                } else {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, pswrd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            registerbtn.setEnabled(false);
                            if (!task.isSuccessful()) {
                                try{
                                    task.getResult();
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d("Fail_register_email",e.getMessage());
                                    Toast.makeText(RegisterActivity.this, "이미 존재하는 이메일입니다.", Toast.LENGTH_LONG).show();
                                }
                                registerbtn.setEnabled(true);

                            } else {
                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                userID = Objects.requireNonNull(user).getUid();
                                emailVerification();

                 /*                   DocumentReference users = fstore.collection("users").document(userID);

                                    Map<String,Object> user = new HashMap<>();
                                    Map<String,Object> userids = new HashMap<>();
                                    userids.put("userID",userID);
                                    user.put("userID", userID);
                                    user.put("fName",fullName);
                                    user.put("email",email);

                                    user.put("IsAdmin",false);
                                    user.put("IsWritable",true);
                                    users.set(user).addOnSuccessListener(aVoid -> {
                                    });*/



                            }
                        }
                    });
                }
            }
        });
        login.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerbtn.setEnabled(true);
    }
    public void emailVerification(){
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Email sent.");
                    Toast.makeText(RegisterActivity.this, "이메일 인증을 진행해주세요.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                }
                else{
                    Toast.makeText(RegisterActivity.this, "메일 발송에 실패했습니다" + user.getEmail() + "입니다", Toast.LENGTH_SHORT).show();
                    try {
                        task.getResult();
                    }catch (Exception e){
                        Log.d("Fail send_email" , e.getMessage());
                    }finally {
                        return;
                    }
                }

            }
        });




    }

}