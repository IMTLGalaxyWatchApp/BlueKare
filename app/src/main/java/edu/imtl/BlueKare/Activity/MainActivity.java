package edu.imtl.BlueKare.Activity;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;

import edu.imtl.BlueKare.Activity.AR.ArActivity;
import edu.imtl.BlueKare.Activity.MainPackage.fragment2_test;
import edu.imtl.BlueKare.Activity.MainPackage.fragment3;
import edu.imtl.BlueKare.Activity.Search.SearchActivity;
import edu.imtl.BlueKare.Activity.login.LoginActivity;
import edu.imtl.BlueKare.R;
import edu.imtl.BlueKare.Utils.TreesContent;
import edu.imtl.BlueKare.helpers.LocationHelper;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static edu.imtl.BlueKare.Activity.MainPackage.fragment2_test.dbhlist;
import static edu.imtl.BlueKare.Activity.MainPackage.fragment2_test.geolist;
import static edu.imtl.BlueKare.Activity.MainPackage.fragment2_test.helist;
import static edu.imtl.BlueKare.Activity.MainPackage.fragment2_test.namelist;
import static edu.imtl.BlueKare.Activity.MainPackage.fragment2_test.splist;
import static edu.imtl.BlueKare.Activity.MainPackage.fragment2_test.tData;
import static edu.imtl.BlueKare.Activity.MainPackage.fragment2_test.tList;

public class MainActivity extends AppCompatActivity{
    FloatingActionButton btn;
    DrawerLayout drawerLayout;
    public static FirebaseFirestore fstore;
    FirebaseAuth mFirebaseAuth;
    String userID;
    ImageButton mSearchBtn;
    public static String finalUserId;
    public static int datasize;
    TextView musername, museremail;
    private long lastTimeBackPressed;
    ;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public static String userName;//세중 추가


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);

        //AR 버튼
        btn = findViewById(R.id.arbutton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentAr = new Intent(MainActivity.this, ArActivity.class);
                try {
                    startActivity(intentAr);
                    finish();
                } catch (ActivityNotFoundException e) {
                    System.out.println("error");
                }
            }
        });
        mFirebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userID = mFirebaseAuth.getCurrentUser().getUid();
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationlayout);
        View headerView = navigationView.getHeaderView(0);
        museremail = (TextView) headerView.findViewById(R.id.userdraweremail);
        musername = (TextView) headerView.findViewById(R.id.userdrawername);

        DocumentReference docRef = fstore.collection("users").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        musername.setText(document.getString("fName"));
                        museremail.setText(document.getString("email"));
                        finalUserId = userID;
                    }
                } else {
                    Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                }
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);

        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }


    @Override
    public void onBackPressed() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            //super.onBackPressed();
            if (System.currentTimeMillis() - lastTimeBackPressed < 2000) {
                finish();
                return;
            }
            Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
            lastTimeBackPressed = System.currentTimeMillis();
        }
    }
}

