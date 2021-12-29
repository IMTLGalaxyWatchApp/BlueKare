package edu.imtl.bluekare.Main;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import edu.imtl.bluekare.Background.AlarmReceiver;
import edu.imtl.bluekare.Fragments.Download.Fragment_download;
import edu.imtl.bluekare.Fragments.Login.DeviceInfo;
import edu.imtl.bluekare.Fragments.MainMenu.Fragment_main;
import edu.imtl.bluekare.Fragments.Record.Fragment_record_audiolistFrag;
import edu.imtl.bluekare.Fragments.Record.Fragment_record_recordFrag;
import edu.imtl.bluekare.Fragments.Survey.Fragment_survey;
import edu.imtl.bluekare.Fragments.Record.Fragment_record;
import edu.imtl.bluekare.Fragments.Login.LoginActivity;
import edu.imtl.bluekare.R;
import edu.imtl.bluekare.SHealth.StepCountReporter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants.StepCount;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthUserProfile;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /*================= Samsung Health  =====================*/
    public static final String APP_TAG = "SimpleHealth";

    private HealthDataStore mStore;
    private StepCountReporter mReporter;
    /*================= Background Service  =====================*/
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Calendar calendar;
    /*=======================================================*/
    FloatingActionButton btn;
    DrawerLayout drawerLayout;
    public String uid,name,dob,gender,phone;
    String[] user_info;
    int type;
    ImageButton mSearchBtn;
    public static String finalUserId;
    public static int datasize;
    TextView musername, museremail,muserage, musergender;
    private long lastTimeBackPressed;

    DeviceInfo deviceInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawerlayout);
        setNavigationViewListener();

        NavigationView navigationView = findViewById(R.id.navigationlayout);
        View headerView = navigationView.getHeaderView(0);
        museremail = headerView.findViewById(R.id.userdraweremail);
        musername = headerView.findViewById(R.id.userdrawername);
        muserage=headerView.findViewById(R.id.userdrawerage);
        musergender=headerView.findViewById(R.id.userdrawergender);



        drawerLayout = findViewById(R.id.drawerlayout);

        findViewById(R.id.menu).setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        deviceInfo = new DeviceInfo(Build.BOARD, Build.BRAND, Build.CPU_ABI, Build.DEVICE, Build.DISPLAY,
                Build.FINGERPRINT, Build.HOST, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), Build.MANUFACTURER, Build.MODEL, Build.PRODUCT,
                Build.TAGS, Build.TYPE, Build.USER, Build.VERSION.RELEASE);


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

//        Async_get_registration async_get_registration = new Async_get_registration(MainActivity.this);
//        async_get_registration.execute();
        setUserInfo();
        setAlarm();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                drawerLayout = findViewById(R.id.drawerlayout);

                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else {

                    if (System.currentTimeMillis() - lastTimeBackPressed < 2000) {
                        finish();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
                    lastTimeBackPressed = System.currentTimeMillis();

                }
            }
        });

    }

    private void setAlarm() {
        calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.AM_PM, Calendar.PM);
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            CharSequence name = "bluekare";
            String description = "Channel for bluekare data update";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("bluekare", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public interface onKeyBackPressedListener {
        void onBackKey();
    }
    private onKeyBackPressedListener mOnKeyBackPressedListener;
    public void setOnKeyBackPressedListener(onKeyBackPressedListener listener) { mOnKeyBackPressedListener = listener; }

//    @Override
//    public void onBackPressed() {
//        drawerLayout = findViewById(R.id.drawerlayout);
//        Fragment_record record=(Fragment_record) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
//
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START);
//        }
//        else if(record.isAdded()){
//            Fragment_record_audiolistFrag frg=(Fragment_record_audiolistFrag) getSupportFragmentManager().findFragmentById(R.id.audioListFragment);
//            frg.changeView();
//        }
//        else if(!record.isAdded()){
//
//                if (System.currentTimeMillis() - lastTimeBackPressed < 2000) {
//                    finish();
//                    return;
//                }
//                Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
//                lastTimeBackPressed = System.currentTimeMillis();
//
//        }
//    }

    private void setNavigationViewListener() {
        NavigationView navigationView = findViewById(R.id.navigationlayout);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void setUserInfo(){

        name=getIntent().getStringExtra("name");
        uid=getIntent().getStringExtra("uid");
        dob=getIntent().getStringExtra("dob");
        phone=getIntent().getStringExtra("phone");
        type=getIntent().getIntExtra("type",0);
        if(getIntent().getStringExtra("gender")=="0") gender="남성";
        else gender="여성";
        user_info= new String[]{name, uid, dob, gender};
        ((userINFO)getApplication()).setUid(uid);
        ((userINFO)getApplication()).setName(name);
        ((userINFO)getApplication()).setDob(dob);
        ((userINFO)getApplication()).setPhone(phone);


        Log.e("setuserinfo",name+uid+dob+gender);
        if(name!=null && uid!=null && dob!=null){
            Calendar cal = new GregorianCalendar();;
            SimpleDateFormat formats;
            formats = new SimpleDateFormat ( "yyyy");

            // Finalvar.birth_year의 값은 1950년 1월 20일
            int time2 = Integer.parseInt(formats.format(cal.getTime()));
            int ageSum = Integer.parseInt(dob.substring(0,4));

            muserage.setText(Integer.toString(time2 - ageSum +1)+"세");
            musergender.setText(gender);
            museremail.setText(uid);
            musername.setText(name);
        }

    }

    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {

        @Override
        public void onConnected() {
            Log.d(APP_TAG, "Health data service is connected.");
            mReporter = new StepCountReporter(mStore, mStepCountObserver, new Handler(Looper.getMainLooper()), getApplicationContext(), user_info);
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
            SharedPreferences remember_pref = getSharedPreferences("renewal_token_pref", 0);
            SharedPreferences.Editor editor = remember_pref.edit();
            editor.putString("renew_token", "none");
            editor.putString("auto_renew_token", "none");
            editor.apply();
            Intent intent_login = new Intent(getBaseContext(), LoginActivity.class);
            intent_login.putExtra("uid", deviceInfo.getId());
            startActivity(intent_login);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}

