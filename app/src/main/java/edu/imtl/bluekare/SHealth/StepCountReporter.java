/*
 * Copyright (C) 2014 Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Mobile Communication Division,
 * Digital Media & Communications Business, Samsung Electronics Co., Ltd.
 *
 * This software and its documentation are confidential and proprietary
 * information of Samsung Electronics Co., Ltd.  No part of the software and
 * documents may be copied, reproduced, transmitted, translated, or reduced to
 * any electronic medium or machine-readable form without the prior written
 * consent of Samsung Electronics.
 *
 * Samsung Electronics makes no representations with respect to the contents,
 * and assumes no responsibility for any errors that might appear in the
 * software and documents. This publication and the contents hereof are subject
 * to change without notice.
 */

package edu.imtl.bluekare.SHealth;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthConstants.StepCount;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


import edu.imtl.bluekare.Main.MainActivity;


public class StepCountReporter {

    private final HealthDataStore mStore;
    private final StepCountObserver mStepCountObserver;
    private final HealthDataResolver mHealthDataResolver;
    private final HealthDataObserver mHealthDataObserver;
    private final Context mContext;

    public StepCountReporter(@NonNull HealthDataStore store, @NonNull StepCountObserver listener,
                             @Nullable Handler resultHandler, Context context) {

        mStore = store;
        mStepCountObserver = listener;
        mContext = context;
        mHealthDataResolver = new HealthDataResolver(mStore, resultHandler);
        mHealthDataObserver = new HealthDataObserver(resultHandler) {

            // Update the step count when a change event is received
            @Override
            public void onChange(String dataTypeName) {
                Log.d("APP_TAG", "Observer receives a data changed event");
                readToday();
            }
        };

    }


    public void start() {
        // Register an observer to listen changes of step count and get today step count

        HealthDataObserver.addObserver(mStore, StepCount.HEALTH_DATA_TYPE, mHealthDataObserver);

        readToday();
    }

    public void stop() {
        HealthDataObserver.removeObserver(mStore, mHealthDataObserver);
    }

    // Read the today
    private void readToday() {

        // Set time range from start time of today to the current time
        long startTime = getUtcStartOfDay(System.currentTimeMillis(), TimeZone.getDefault()) -TimeUnit.DAYS.toMillis(77);
        Log.d("TIME:",String.valueOf(startTime));
        long endTime = startTime + TimeUnit.DAYS.toMillis(1);
        SimpleDateFormat todate = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        SimpleDateFormat tomin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());

        ////////////STEP COUNT///////////////
        HealthDataResolver.ReadRequest request_stepcount = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(StepCount.HEALTH_DATA_TYPE)
                .setLocalTimeRange(StepCount.START_TIME, StepCount.TIME_OFFSET, startTime, endTime)
                .build();

        try {
            mHealthDataResolver.read(request_stepcount).setResultListener(readResult -> {
                StringBuilder data = new StringBuilder();
                data.append("Start Time,End Time,Step Count,Distance,Calorie,Speed");


                String Filename = "Step_Count_" + todate.format(new Date(startTime)) + ".csv";

                try (HealthDataResolver.ReadResult result = readResult) {

                    for (HealthData healthData : result) {
                        String startdateformat = tomin.format(new Date(healthData.getLong(StepCount.START_TIME)));
                        String enddateformat = tomin.format(new Date(healthData.getLong(StepCount.END_TIME)));
                        Log.d("HERE : START: ", String.valueOf(startdateformat));
                        Log.d("HERE : END: ", String.valueOf(enddateformat));
                        Log.d("HERE : COUNT", String.valueOf(healthData.getInt(StepCount.COUNT)));

                        data.append("\n").append(startdateformat)
                                .append(",").append(enddateformat)
                                .append(",").append(healthData.getString(StepCount.COUNT))
                                .append(",").append(healthData.getString(StepCount.DISTANCE))
                                .append(",").append(healthData.getString(StepCount.CALORIE))
                                .append(",").append(healthData.getString(StepCount.SPEED));
                    }

                    try{
                        FileOutputStream out = mContext.openFileOutput(Filename,Context.MODE_PRIVATE);
                        out.write((data.toString().getBytes()));
                        out.close();

                        File filelocation = new File(mContext.getFilesDir(), Filename);
                        Uri path = FileProvider.getUriForFile(mContext,"edu.imtl.bluekare.fileprovider", filelocation);
                        Intent fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType("text/csv");
                        fileIntent.putExtra(Intent.EXTRA_SUBJECT, Filename);
                        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                        fileIntent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION );

                        Intent chooser = Intent.createChooser(fileIntent, "Send Email");

                        List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            mContext.grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }

//                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(chooser);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("STEPCOUNT", "Getting step count fails.", e);
        }

        ////////////EXERCISE///////////////

        HealthDataResolver.ReadRequest request_exercise = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Exercise.HEALTH_DATA_TYPE)
                .setLocalTimeRange(HealthConstants.Exercise.START_TIME, HealthConstants.Exercise.TIME_OFFSET, startTime, endTime)
                .build();

        try {
            mHealthDataResolver.read(request_exercise).setResultListener(readResult -> {
                StringBuilder data = new StringBuilder();
                data.append("Start Time,End Time,Exercise_type,Calorie,Duration,Distance,Count,Max Speed, Mean Speed, Min Heart Rate, Max Heart Rate");

                String Filename = "Exercise_" + todate.format(new Date(startTime)) + ".csv";

                try (HealthDataResolver.ReadResult result = readResult) {

                    for (HealthData healthData : result) {
                        String startdateformat = tomin.format(new Date(healthData.getLong(HealthConstants.Exercise.START_TIME)));
                        String enddateformat = tomin.format(new Date(healthData.getLong(HealthConstants.Exercise.END_TIME)));

                        data.append("\n").append(startdateformat)
                                .append(",").append(enddateformat)
                                .append(",").append(healthData.getString(HealthConstants.Exercise.EXERCISE_TYPE))
                                .append(",").append(healthData.getString(HealthConstants.Exercise.CALORIE))
                                .append(",").append(healthData.getString(HealthConstants.Exercise.DURATION))
                                .append(",").append(healthData.getString(HealthConstants.Exercise.DISTANCE))
                                .append(",").append(healthData.getString(HealthConstants.Exercise.COUNT))
                                .append(",").append(healthData.getString(HealthConstants.Exercise.MAX_SPEED))
                                .append(",").append(healthData.getString(HealthConstants.Exercise.MEAN_SPEED))
                                .append(",").append(healthData.getString(HealthConstants.Exercise.MIN_HEART_RATE))
                                .append(",").append(healthData.getString(HealthConstants.Exercise.MAX_HEART_RATE));
                    }

                    try{
                        FileOutputStream out = mContext.openFileOutput(Filename,Context.MODE_PRIVATE);
                        out.write((data.toString().getBytes()));
                        out.close();

                        File filelocation = new File(mContext.getFilesDir(), Filename);
                        Uri path = FileProvider.getUriForFile(mContext,"edu.imtl.bluekare.fileprovider", filelocation);
                        Intent fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType("text/csv");
                        fileIntent.putExtra(Intent.EXTRA_SUBJECT, Filename);
                        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                        fileIntent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION );

                        Intent chooser = Intent.createChooser(fileIntent, "Send Email");

                        List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            mContext.grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }

//                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(chooser);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("STEPCOUNT", "Getting step count fails.", e);
        }
        ////////////EXERCISE///////////////

        ////////////SLEEP_STAGE///////////////

        HealthDataResolver.ReadRequest request_sleepStage = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.SleepStage.HEALTH_DATA_TYPE)
                .setLocalTimeRange(HealthConstants.SleepStage.START_TIME, HealthConstants.SleepStage.TIME_OFFSET, startTime, endTime)
                .build();

        try {
            mHealthDataResolver.read(request_sleepStage).setResultListener(readResult -> {
                StringBuilder data = new StringBuilder();
                data.append("Start Time,End Time,SLEEP_ID,STAGE");

                String Filename = "SleepStage_" + todate.format(new Date(startTime)) + ".csv";

                try (HealthDataResolver.ReadResult result = readResult) {

                    for (HealthData healthData : result) {
                        String startdateformat = tomin.format(new Date(healthData.getLong(HealthConstants.SleepStage.START_TIME)));
                        String enddateformat = tomin.format(new Date(healthData.getLong(HealthConstants.SleepStage.END_TIME)));

                        data.append("\n").append(startdateformat)
                                .append(",").append(enddateformat)
                                .append(",").append(healthData.getString(HealthConstants.SleepStage.SLEEP_ID))
                                .append(",");
                        Log.d("SLEEP", String.valueOf(healthData.getInt(HealthConstants.SleepStage.STAGE)));
                        switch (healthData.getInt(HealthConstants.SleepStage.STAGE)){
                            case 40001:
                                data.append("STAGE_AWAKE");
                                break;
                            case 40002:
                                data.append("STAGE_LIGHT");
                                break;
                            case 40003:
                                data.append("STAGE_DEEP");
                                break;
                            case 40004:
                                data.append("STAGE_REM");
                                break;
                            default:
                                data.append("Unknown");
                                break;
                        }
                    }

                    try{
                        FileOutputStream out = mContext.openFileOutput(Filename,Context.MODE_PRIVATE);
                        out.write((data.toString().getBytes()));
                        out.close();

                        File filelocation = new File(mContext.getFilesDir(), Filename);
                        Uri path = FileProvider.getUriForFile(mContext,"edu.imtl.bluekare.fileprovider", filelocation);
                        Intent fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType("text/csv");
                        fileIntent.putExtra(Intent.EXTRA_SUBJECT, Filename);
                        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                        fileIntent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION );

                        Intent chooser = Intent.createChooser(fileIntent, "Send Email");

                        List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            mContext.grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }

                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(chooser);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("SLEEPSTAGE", "Getting step count fails.", e);
        }
        ////////////SLEEP_STAGE///////////////

        ////////////HEART_RATE///////////////

        HealthDataResolver.ReadRequest request_heartRate = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.HeartRate.HEALTH_DATA_TYPE)
                .setLocalTimeRange(HealthConstants.HeartRate.START_TIME, HealthConstants.HeartRate.TIME_OFFSET, startTime, endTime)
                .build();

        try {
            mHealthDataResolver.read(request_heartRate).setResultListener(readResult -> {
                StringBuilder data = new StringBuilder();
                data.append("Start Time,End Time,Heart Rate,Heart Beat Count, Min, Max");

                String Filename = "HeartRate_" + todate.format(new Date(startTime)) + ".csv";

                try (HealthDataResolver.ReadResult result = readResult) {

                    for (HealthData healthData : result) {
                        String startdateformat = tomin.format(new Date(healthData.getLong(HealthConstants.HeartRate.START_TIME)));
                        String enddateformat = tomin.format(new Date(healthData.getLong(HealthConstants.HeartRate.END_TIME)));

                        data.append("\n").append(startdateformat)
                                .append(",").append(enddateformat)
                                .append(",").append(healthData.getString(HealthConstants.HeartRate.HEART_RATE))
                                .append(",").append(healthData.getString(HealthConstants.HeartRate.HEART_BEAT_COUNT))
                                .append(",").append(healthData.getString(HealthConstants.HeartRate.MIN))
                                .append(",").append(healthData.getString(HealthConstants.HeartRate.MAX));

                    }

                    try{
                        FileOutputStream out = mContext.openFileOutput(Filename,Context.MODE_PRIVATE);
                        out.write((data.toString().getBytes()));
                        out.close();

                        File filelocation = new File(mContext.getFilesDir(), Filename);
                        Uri path = FileProvider.getUriForFile(mContext,"edu.imtl.bluekare.fileprovider", filelocation);
                        Intent fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType("text/csv");
                        fileIntent.putExtra(Intent.EXTRA_SUBJECT, Filename);
                        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                        fileIntent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION );

                        Intent chooser = Intent.createChooser(fileIntent, "Send Email");

                        List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            mContext.grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }

//                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(chooser);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("SLEEPSTAGE", "Getting step count fails.", e);
        }
        ////////////HEART_RATE///////////////


        ////////////SPo2///////////////

        HealthDataResolver.ReadRequest request_oxygen = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.OxygenSaturation.HEALTH_DATA_TYPE)
                .setLocalTimeRange(HealthConstants.OxygenSaturation.START_TIME, HealthConstants.OxygenSaturation.TIME_OFFSET, startTime, endTime)
                .build();

        try {
            mHealthDataResolver.read(request_oxygen).setResultListener(readResult -> {
                StringBuilder data = new StringBuilder();
                data.append("Start Time,End Time,SPO2,Heart Rate");

                String Filename = "OxygenSaturation_" + todate.format(new Date(startTime)) + ".csv";

                try (HealthDataResolver.ReadResult result = readResult) {

                    for (HealthData healthData : result) {
                        String startdateformat = tomin.format(new Date(healthData.getLong(HealthConstants.OxygenSaturation.START_TIME)));
                        String enddateformat = tomin.format(new Date(healthData.getLong(HealthConstants.OxygenSaturation.END_TIME)));

                        data.append("\n").append(startdateformat)
                                .append(",").append(enddateformat)
                                .append(",").append(healthData.getString(HealthConstants.OxygenSaturation.SPO2))
                                .append(",").append(healthData.getString(HealthConstants.OxygenSaturation.HEART_RATE));

                    }

                    try{
                        FileOutputStream out = mContext.openFileOutput(Filename,Context.MODE_PRIVATE);
                        out.write((data.toString().getBytes()));
                        out.close();

                        File filelocation = new File(mContext.getFilesDir(), Filename);
                        Uri path = FileProvider.getUriForFile(mContext,"edu.imtl.bluekare.fileprovider", filelocation);
                        Intent fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType("text/csv");
                        fileIntent.putExtra(Intent.EXTRA_SUBJECT, Filename);
                        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                        fileIntent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION );

                        Intent chooser = Intent.createChooser(fileIntent, "Send Email");

                        List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            mContext.grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }

//                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(chooser);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("SLEEPSTAGE", "Getting step count fails.", e);
        }
        ////////////SPo2///////////////

        ////////////Blood_Pressure///////////////

        HealthDataResolver.ReadRequest request_blood = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.BloodPressure.HEALTH_DATA_TYPE)
                .setLocalTimeRange(HealthConstants.BloodPressure.START_TIME, HealthConstants.BloodPressure.TIME_OFFSET, startTime, endTime)
                .build();

        try {
            mHealthDataResolver.read(request_blood).setResultListener(readResult -> {
                StringBuilder data = new StringBuilder();
                data.append("Start Time,Systolic,Diastolic, Mean, Pulse");

                String Filename = "BloodPressure_" + todate.format(new Date(startTime)) + ".csv";

                try (HealthDataResolver.ReadResult result = readResult) {

                    for (HealthData healthData : result) {
                        String startdateformat = tomin.format(new Date(healthData.getLong(HealthConstants.BloodPressure.START_TIME)));

                        data.append("\n").append(startdateformat)
                                .append(",").append(healthData.getString(HealthConstants.BloodPressure.SYSTOLIC))
                                .append(",").append(healthData.getString(HealthConstants.BloodPressure.DIASTOLIC))
                                .append(",").append(healthData.getString(HealthConstants.BloodPressure.MEAN))
                                .append(",").append(healthData.getString(HealthConstants.BloodPressure.PULSE));

                    }

                    try{
                        FileOutputStream out = mContext.openFileOutput(Filename,Context.MODE_PRIVATE);
                        out.write((data.toString().getBytes()));
                        out.close();

                        File filelocation = new File(mContext.getFilesDir(), Filename);
                        Uri path = FileProvider.getUriForFile(mContext,"edu.imtl.bluekare.fileprovider", filelocation);
                        Intent fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType("text/csv");
                        fileIntent.putExtra(Intent.EXTRA_SUBJECT, Filename);
                        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                        fileIntent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION );

                        Intent chooser = Intent.createChooser(fileIntent, "Send Email");

                        List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            mContext.grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }

                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(chooser);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("SLEEPSTAGE", "Getting step count fails.", e);
        }
        ////////////SPo2///////////////
    }



    private long getUtcStartOfDay(long time, TimeZone tz) {
        Calendar cal = Calendar.getInstance(tz);
        cal.setTimeInMillis(time);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DATE);

        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    public interface StepCountObserver {
        void onChanged(int count);
    }
}
