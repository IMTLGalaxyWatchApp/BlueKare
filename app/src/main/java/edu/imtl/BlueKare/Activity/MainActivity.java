package edu.imtl.BlueKare.Activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
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

import edu.imtl.BlueKare.Activity.AR.ArActivity;
import edu.imtl.BlueKare.Activity.MainPackage.fragment2_test;
import edu.imtl.BlueKare.Activity.MainPackage.fragment3;
import edu.imtl.BlueKare.Activity.Search.SearchActivity;
import edu.imtl.BlueKare.Activity.login.LoginActivity;
import edu.imtl.BlueKare.R;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerlayout);
        setNavigationViewListener();

        //AR 버튼
        btn = findViewById(R.id.arbutton);
        btn.setOnClickListener(view -> {
            Intent intentAr = new Intent(MainActivity.this, ArActivity.class);
            try {
                startActivity(intentAr);
                finish();
            } catch (ActivityNotFoundException e) {
                System.out.println("error");
            }
        });
        mFirebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userID= Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationlayout);
        View headerView = navigationView.getHeaderView(0);
        museremail=(TextView) headerView.findViewById(R.id.userdraweremail);
        musername=(TextView) headerView.findViewById(R.id.userdrawername);



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

        drawerLayout = (DrawerLayout)findViewById(R.id.drawerlayout);

        findViewById(R.id.menu).setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        //search
        mSearchBtn = (ImageButton) findViewById(R.id.search_go_btn);
        mSearchBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this , SearchActivity.class));
            finish();
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new fragment2_test()).commit();
            navigationView.setCheckedItem(R.id.fragment2_test_nav);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.Logout: {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this , LoginActivity.class));
                this.finish();
                break;
            }
            case R.id.fragment2_test_nav: {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new fragment2_test()).commit();
                break;
            }
            case R.id.fragment3_nav: {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new fragment3()).commit();
                break;
            }
            case R.id.History_nav: {
                startActivity(new Intent(MainActivity.this , SearchActivity.class));
                break;
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerlayout);
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
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationlayout);
        navigationView.setNavigationItemSelectedListener(this);
    }
}

