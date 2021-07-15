package edu.imtl.bluekare;

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

import edu.imtl.bluekare.Fragments.Download.Fragment_download;
import edu.imtl.bluekare.Fragments.MainMenu.Fragment_main;
import edu.imtl.bluekare.Fragments.Survey.Fragment_survey;
import edu.imtl.bluekare.Fragments.Record.Fragment_record;
import edu.imtl.bluekare.Fragments.Login.LoginActivity;
import edu.imtl.bluekare.SHealth.StepCountReporter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants.StepCount;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;
import com.samsung.android.sdk.healthdata.HealthUserProfile;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /*================= Samsung Health  =====================*/
    public static final String APP_TAG = "SimpleHealth";

    private HealthDataStore mStore;
    private StepCountReporter mReporter;


    /*=======================================================*/

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawerlayout);
        setNavigationViewListener();

        mFirebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userID = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
        NavigationView navigationView = findViewById(R.id.navigationlayout);
        View headerView = navigationView.getHeaderView(0);
        museremail = headerView.findViewById(R.id.userdraweremail);
        musername = headerView.findViewById(R.id.userdrawername);


        DocumentReference docRef = fstore.collection("users").document(userID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    musername.setText(document.getString("fName"));
                    museremail.setText(document.getString("email"));
                    finalUserId = userID;
                }
            } else {
                Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        drawerLayout = findViewById(R.id.drawerlayout);

        findViewById(R.id.menu).setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_main()).commit();
            navigationView.setCheckedItem(R.id.Main_Menu_nav);
        }

        /*================= Samsung Health  =====================*/
        // Create a HealthDataStore instance and set its listener
        mStore = new HealthDataStore(this, mConnectionListener);
        // Request the connection to the health data store
        mStore.connectService();

        /*=======================================================*/


    }


    @Override
    public void onBackPressed() {
        drawerLayout = findViewById(R.id.drawerlayout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (System.currentTimeMillis() - lastTimeBackPressed < 2000) {
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

    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {

        @Override
        public void onConnected() {
            Log.d(APP_TAG, "Health data service is connected.");
            mReporter = new StepCountReporter(mStore, mStepCountObserver, new Handler(Looper.getMainLooper()));
            if (isPermissionAcquired()) {
                mReporter.start();
                HealthUserProfile usrProfile = HealthUserProfile.getProfile(mStore);
                Log.d(APP_TAG,String.valueOf(usrProfile.getBirthDate()));
                Log.d(APP_TAG,String.valueOf(usrProfile.getGender()));
                Log.d(APP_TAG,String.valueOf(usrProfile.getHeight()));
                Log.d(APP_TAG,String.valueOf(usrProfile.getWeight()));
            } else {
                requestPermission();


            }
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            Log.d(APP_TAG, "Health data service is not available.");
            showConnectionFailureDialog(error);
        }

        @Override
        public void onDisconnected() {
            Log.d(APP_TAG, "Health data service is disconnected.");
            if (!isFinishing()) {
                mStore.connectService();
            }
        }
    };

    private void showPermissionAlarmDialog() {
        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle(R.string.notice)
                .setMessage(R.string.msg_perm_acquired)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showConnectionFailureDialog(final HealthConnectionErrorResult error) {
        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if (error.hasResolution()) {
            switch (error.getErrorCode()) {
                case HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED:
                    alert.setMessage(R.string.msg_req_install);
                    break;
                case HealthConnectionErrorResult.OLD_VERSION_PLATFORM:
                    alert.setMessage(R.string.msg_req_upgrade);
                    break;
                case HealthConnectionErrorResult.PLATFORM_DISABLED:
                    alert.setMessage(R.string.msg_req_enable);
                    break;
                case HealthConnectionErrorResult.USER_AGREEMENT_NEEDED:
                    alert.setMessage(R.string.msg_req_agree);
                    break;
                default:
                    alert.setMessage(R.string.msg_req_available);
                    break;
            }
        } else {
            alert.setMessage(R.string.msg_conn_not_available);
        }

        alert.setPositiveButton(R.string.ok, (dialog, id) -> {
            if (error.hasResolution()) {
                error.resolve(MainActivity.this);
            }
        });

        if (error.hasResolution()) {
            alert.setNegativeButton(R.string.cancel, null);
        }

        alert.show();
    }

    private boolean isPermissionAcquired() {
        Set<PermissionKey> mKeySet = new HashSet<>();
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.USER_PROFILE_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(StepCount.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.BloodPressure.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.Exercise.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.HeartRate.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.OxygenSaturation.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.Sleep.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.SleepStage.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.StepDailyTrend.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            // Check whether the permissions that this application needs are acquired
            Map<PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeySet);
            return !resultMap.containsValue(Boolean.FALSE);
        } catch (Exception e) {
            Log.e(APP_TAG, "Permission request fails.", e);
        }
        return false;
    }

    private void requestPermission() {
        Set<PermissionKey> mKeySet = new HashSet<>();
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.USER_PROFILE_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(StepCount.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.BloodPressure.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.Exercise.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.HeartRate.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.OxygenSaturation.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.Sleep.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.SleepStage.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));
        mKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.StepDailyTrend.HEALTH_DATA_TYPE,
                HealthPermissionManager.PermissionType.READ));

        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            // Show user permission UI for allowing user to change options
            pmsManager.requestPermissions(mKeySet, MainActivity.this)
                    .setResultListener(result -> {
                        Log.d(APP_TAG, "Permission callback is received.");
                        Map<PermissionKey, Boolean> resultMap = result.getResultMap();

                        if (resultMap.containsValue(Boolean.FALSE)) {
                            updateStepCountView("");
                            showPermissionAlarmDialog();
                        } else {
                            // Get the current step count and display it
                            mReporter.start();
                        }
                    });
        } catch (Exception e) {
            Log.e(APP_TAG, "Permission setting fails.", e);
        }
    }

    private final StepCountReporter.StepCountObserver mStepCountObserver = count -> {
//        Log.d(APP_TAG, "Step reported : " + count);
        updateStepCountView(String.valueOf(count));
    };

    private void updateStepCountView(final String count) {
//        runOnUiThread(() -> Log.e(APP_TAG, String.valueOf(count)));
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        if (item.getItemId() == R.id.Main_Menu_nav) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_main()).commit();
        }
        if (item.getItemId() == R.id.Survey_nav) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_survey()).commit();
        }
        if (item.getItemId() == R.id.Record_nav) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_record()).commit();
        }
        if (item.getItemId() == R.id.Download_nav) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_download()).commit();
        }
        if (item.getItemId() == R.id.shealthConnect) {
            requestPermission();
        }
        if (item.getItemId() == R.id.Logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}

