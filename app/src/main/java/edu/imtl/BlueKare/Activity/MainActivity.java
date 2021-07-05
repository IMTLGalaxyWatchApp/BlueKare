package edu.imtl.BlueKare.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import edu.imtl.BlueKare.Activity.Download.Fragment_download;
import edu.imtl.BlueKare.Activity.MainMenu.Fragment_main;
import edu.imtl.BlueKare.Activity.Record.Fragment_record;
import edu.imtl.BlueKare.Activity.Login.LoginActivity;
import edu.imtl.BlueKare.R;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    FloatingActionButton btn;
    DrawerLayout drawerLayout;
    public static FirebaseFirestore fstore;
    FirebaseAuth mFirebaseAuth;
    String userID;
    ImageButton mSearchBtn;
    public static String finalUserId;
    public static int datasize;
    TextView musername, museremail;
    private long lastTimeBackPressed;;


    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawerlayout);
        setNavigationViewListener();

        mFirebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userID= Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
        NavigationView navigationView = findViewById(R.id.navigationlayout);
        View headerView = navigationView.getHeaderView(0);
        museremail= headerView.findViewById(R.id.userdraweremail);
        musername= headerView.findViewById(R.id.userdrawername);



        DocumentReference docRef = fstore.collection("users").document(userID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    musername.setText(document.getString("fName"));
                    museremail.setText(document.getString("email"));
                    finalUserId=userID;
                }
            }
            else {
                Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        drawerLayout = findViewById(R.id.drawerlayout);

        findViewById(R.id.menu).setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_main()).commit();
            navigationView.setCheckedItem(R.id.Main_Menu_nav);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.Main_Menu_nav: {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_main()).commit();
                break;
            }
            case R.id.Record_nav: {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_record()).commit();
                break;
            }
            case R.id.Download_nav: {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_download()).commit();
                break;
            }
            case R.id.Logout: {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this , LoginActivity.class));
                this.finish();
                break;
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        drawerLayout = findViewById(R.id.drawerlayout);
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            if (System.currentTimeMillis() - lastTimeBackPressed < 2000)
            {
                finish();
                return;
            }
            Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
            lastTimeBackPressed = System.currentTimeMillis();
        }
    }

    private void setNavigationViewListener() {
        NavigationView navigationView = findViewById(R.id.navigationlayout);
        navigationView.setNavigationItemSelectedListener(this);
    }
}

