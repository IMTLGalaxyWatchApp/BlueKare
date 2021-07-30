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
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import edu.imtl.bluekare.Main.MainActivity;


public class StepCountReporter {

    private final HealthDataStore mStore;
    private final StepCountObserver mStepCountObserver;
    private final HealthDataResolver mHealthDataResolver;
    private final HealthDataObserver mHealthDataObserver;
    private Context mContext;

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
        long startTime = getUtcStartOfDay(System.currentTimeMillis(), TimeZone.getDefault()) -TimeUnit.DAYS.toMillis(6);
        long endTime = startTime + TimeUnit.DAYS.toMillis(1);

        ////////////STEP COUNT///////////////
        HealthDataResolver.ReadRequest request_stepcount = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(StepCount.HEALTH_DATA_TYPE)
                .setLocalTimeRange(StepCount.START_TIME, StepCount.TIME_OFFSET, startTime, endTime)
                .build();

        try {
            mHealthDataResolver.read(request_stepcount).setResultListener(readResult -> {
                StringBuilder stepCountdata = new StringBuilder();
                stepCountdata.append("Start Time,End Time,Step Count,Distance,Calorie,Speed");
                SimpleDateFormat todate = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                SimpleDateFormat tomin = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());

                String Filename = "Step_Count_" + String.valueOf(todate.format(new Date(startTime))) + ".csv";

                try (HealthDataResolver.ReadResult result = readResult) {

                    for (HealthData healthData : result) {
                        HealthData data = healthData;
                        String startdateformat = tomin.format(new Date(data.getLong(StepCount.START_TIME)));
                        String enddateformat = tomin.format(new Date(data.getLong(StepCount.END_TIME)));
                        Log.d("HERE : START: ", String.valueOf(startdateformat));
                        Log.d("HERE : END: ", String.valueOf(enddateformat));
                        Log.d("HERE : COUNT", String.valueOf(data.getInt(StepCount.COUNT)));

//                        stepCountdata.append("\n"+String.valueOf(1)+","+String.valueOf(1*1));
                        stepCountdata.append("\n").append(startdateformat)
                                .append(",").append(enddateformat)
                                .append(",").append(data.getString(StepCount.COUNT))
                                .append(",").append(data.getString(StepCount.DISTANCE))
                                .append(",").append(data.getString(StepCount.CALORIE))
                                .append(",").append(data.getString(StepCount.SPEED));
                    }

                    try{
                        FileOutputStream out = mContext.openFileOutput(Filename,Context.MODE_PRIVATE);
                        out.write((stepCountdata.toString().getBytes()));
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
            Log.e("STEPCOUNT", "Getting step count fails.", e);
        }

        ////////////EXERCISE///////////////

        HealthDataResolver.ReadRequest request_exercise = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Exercise.HEALTH_DATA_TYPE)
//                .addFunction(AggregateFunction.SUM, StepCount.COUNT, "total_step")
//                .setProperties(new String[]{HealthConstants.StepCount.COUNT})
                .setLocalTimeRange(HealthConstants.Exercise.START_TIME, HealthConstants.Exercise.TIME_OFFSET, startTime, endTime)
//                .setTimeAfter(BeginTime)
                //                .setSort(ALIAS_BINNING_TIME, HealthDataResolver.SortOrder.ASC)

                .build();

        try {
            mHealthDataResolver.read(request_exercise).setResultListener(readResult -> {
                StringBuilder exercise_data = new StringBuilder();
                exercise_data.append("Start Time,End Time,Exercise_type,Calorie,Duration,Distance,Count,Max Speed, Mean Speed, Min Heart Rate, Max Heart Rate");
                SimpleDateFormat todate = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                SimpleDateFormat tomin = new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());

                String Filename = "Exercise_" + String.valueOf(todate.format(new Date(startTime))) + ".csv";

                try (HealthDataResolver.ReadResult result = readResult) {

                    for (HealthData healthData : result) {
                        HealthData data = healthData;
                        String startdateformat = tomin.format(new Date(data.getLong(HealthConstants.Exercise.START_TIME)));
                        String enddateformat = tomin.format(new Date(data.getLong(HealthConstants.Exercise.END_TIME)));
                        Log.d("HERE : START: ", String.valueOf(startdateformat));
                        Log.d("HERE : END: ", String.valueOf(enddateformat));
                        Log.d("HERE : COUNT", String.valueOf(data.getInt(HealthConstants.Exercise.COUNT)));

                        exercise_data.append("\n").append(startdateformat)
                                .append(",").append(enddateformat)
                                .append(",").append(data.getString(HealthConstants.Exercise.EXERCISE_TYPE))
                                .append(",").append(data.getString(HealthConstants.Exercise.CALORIE))
                                .append(",").append(data.getString(HealthConstants.Exercise.DURATION))
                                .append(",").append(data.getString(HealthConstants.Exercise.DISTANCE))
                                .append(",").append(data.getString(HealthConstants.Exercise.COUNT))
                                .append(",").append(data.getString(HealthConstants.Exercise.MAX_SPEED))
                                .append(",").append(data.getString(HealthConstants.Exercise.MEAN_SPEED))
                                .append(",").append(data.getString(HealthConstants.Exercise.MIN_HEART_RATE))
                                .append(",").append(data.getString(HealthConstants.Exercise.MAX_HEART_RATE));
                    }

                    try{
                        FileOutputStream out = mContext.openFileOutput(Filename,Context.MODE_PRIVATE);
                        out.write((exercise_data.toString().getBytes()));
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
            Log.e("STEPCOUNT", "Getting step count fails.", e);
        }

        ////////////EXERCISE///////////////

    }


    public static long getTodayStartUtcTime() {
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today.getTimeInMillis();
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
