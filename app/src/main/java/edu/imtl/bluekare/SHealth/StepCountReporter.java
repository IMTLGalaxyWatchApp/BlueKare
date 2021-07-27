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
                readTodayStepCount();
            }
        };

    }


    public void start() {
        // Register an observer to listen changes of step count and get today step count

        HealthDataObserver.addObserver(mStore, StepCount.HEALTH_DATA_TYPE, mHealthDataObserver);

        readTodayStepCount();
    }

    public void stop() {
        HealthDataObserver.removeObserver(mStore, mHealthDataObserver);
    }


//    private void readTodayStepCount() {
//        private String ALIAS_BINNING_TIME = "binning_time"
//
//        // Create a filter for today's steps from all source devices
//        HealthDataResolver.Filter filter = HealthDataResolver.Filter.and(
////                HealthDataResolver.Filter.eq(HealthConstants.StepDailyTrend.DAY_TIME, getTodayStartUtcTime()),
//                HealthDataResolver.Filter.eq(HealthConstants.StepDailyTrend.SOURCE_TYPE, HealthConstants.StepDailyTrend.SOURCE_TYPE_ALL));
//
//        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
//                // Set the data type
//                .setDataType(HealthConstants.StepDailyTrend.HEALTH_DATA_TYPE)
//                .setTimeGroup(HealthDataResolver.AggregateRequest.TimeGroupUnit.MINUTELY, 10, StepCount.START_TIME, StepCount.TIME_OFFSET, ALIAS_BINNING_TIME)
//                .setSort(ALIAS_BINNING_TIME, HealthDataResolver.SortOrder.ASC)
//
//                // Set a filter
//                .setFilter(filter)
//                // Build
//                .build();
//        mHealthDataResolver = new HealthDataResolver(mStore, null);
//
//        try {
//            mHealthDataResolver.read(request).setResultListener(result -> {
//                long dayTime = 0;
//                int totalCount = 0;
//
//                try {
//                    for (HealthData healthData : result) {
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//                        HealthData data = healthData;
//                        dayTime = data.getLong(HealthConstants.StepDailyTrend.DAY_TIME);
//                        Date date = new Date(dayTime);
//                        String dateformat = sdf.format(date);
//                        totalCount = data.getInt(HealthConstants.StepDailyTrend.COUNT);
//                        Log.d("HERE", String.valueOf(dateformat));
//                        Log.d("HERE", String.valueOf(totalCount));
//
//                    }
//                } finally {
//                    result.close();
//                }
//            });
//        } catch (Exception e) {
//            Log.e(MainActivity.APP_TAG, e.getClass().getName() + " - " + e.getMessage());
//        }
//    }


    // Read the today's step count on demand
    private void readTodayStepCount() {

        // Set time range from start time of today to the current time
        long startTime = getUtcStartOfDay(System.currentTimeMillis(), TimeZone.getDefault());
        long endTime = startTime + TimeUnit.DAYS.toMillis(1);




        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(StepCount.HEALTH_DATA_TYPE)
//                .addFunction(AggregateFunction.SUM, StepCount.COUNT, "total_step")
//                .setProperties(new String[]{HealthConstants.StepCount.COUNT})
                .setLocalTimeRange(StepCount.START_TIME, StepCount.TIME_OFFSET, startTime, endTime)
                //                .setSort(ALIAS_BINNING_TIME, HealthDataResolver.SortOrder.ASC)

                .build();


        try {
            mHealthDataResolver.read(request).setResultListener(readResult -> {
                StringBuilder stepCountdata = new StringBuilder();
                stepCountdata.append("Start Time,Step Count");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                String Filename = "Step_Count_" + String.valueOf(sdf.format(new Date(startTime))) + ".csv";

                try (HealthDataResolver.ReadResult result = readResult) {

                    long start = 0;
                    long end = 0;
                    for (HealthData healthData : result) {


                        HealthData data = healthData;
                        start = data.getLong(StepCount.START_TIME);
                        end = data.getLong(StepCount.END_TIME);

                        Date startdate = new Date(start);
                        Date enddate = new Date(end);
                        String startdateformat = sdf.format(startdate);
                        String enddateformat = sdf.format(enddate);
                        Log.d("HERE : START: ", String.valueOf(startdateformat));
                        Log.d("HERE : END: ", String.valueOf(enddateformat));
                        Log.d("HERE : COUNT", String.valueOf(data.getInt(StepCount.COUNT)));

//                        stepCountdata.append("\n"+String.valueOf(1)+","+String.valueOf(1*1));
                        stepCountdata.append("\n").append(startdateformat).append(",").append(data.getInt(StepCount.COUNT));



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
//            chooser.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        mContext.startActivity(chooser);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("STEPCOUNT", "Getting step count fails.", e);
        }


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