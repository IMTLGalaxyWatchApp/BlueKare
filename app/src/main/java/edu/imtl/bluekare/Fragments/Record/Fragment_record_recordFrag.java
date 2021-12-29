package edu.imtl.bluekare.Fragments.Record;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.imtl.bluekare.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_record_recordFrag extends Fragment implements View.OnClickListener {

    private NavController navController;

    private ImageButton listBtn;
    private ImageButton recordBtn;
    private TextView filenameText;

    private TextInputEditText name, phone;

    private boolean isRecording = false;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;

    private MediaRecorder mediaRecorder;
    private String recordFile;

    private Chronometer timer;

    private String p_name, d_name;

    public Fragment_record_recordFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Intitialize Variables
        navController = Navigation.findNavController(view);
        listBtn = view.findViewById(R.id.record_list_btn);
        recordBtn = view.findViewById(R.id.record_btn);
        timer = view.findViewById(R.id.record_timer);
        filenameText = view.findViewById(R.id.record_filename);
        name=view.findViewById(R.id.name);
        phone=view.findViewById(R.id.phone);

        /* Setting up on click listener
           - Class must implement 'View.OnClickListener' and override 'onClick' method
         */
        listBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        File file = new File(requireActivity().getFilesDir().getAbsolutePath() + File.separator + "record");
        if (!file.exists())
            file.mkdir();

    }

    @Override
    public void onClick(View v) {
        p_name =name.getText().toString();
        d_name =phone.getText().toString();
        /*  Check, which button is pressed and do the task accordingly
         */
        switch (v.getId()) {
            case R.id.record_list_btn:
                /*
                Navigation Controller
                Part of Android Jetpack, used for navigation between both fragments
                 */
                if(isRecording){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                            isRecording = false;
                        }
                    });
                    alertDialog.setNegativeButton("CANCEL", null);
                    alertDialog.setTitle("Audio Still recording");
                    alertDialog.setMessage("Are you sure, you want to stop the recording?");
                    alertDialog.create().show();
                } else {
                    navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                }
                break;

            case R.id.record_btn:
                if(p_name.isEmpty()|| d_name.isEmpty()){
                    Toast.makeText(getContext(), "환자의 이름과 전화번호를 입력해주세요.",Toast.LENGTH_SHORT).show();
                }
                else {
                    if (isRecording) {
                        //Stop Recording
                        stopRecording();

                        // Change button image and set Recording state to false
                        recordBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.record_btn_stopped, null));
                        isRecording = false;
                    } else {
                        //Check permission to record audio
                        if (checkPermissions()) {
                            //Start Recording
                            startRecording();

                            // Change button image and set Recording state to false
                            recordBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.record_btn_recording, null));
                            isRecording = true;
                        }
                    }
                }
                break;
        }
    }

    private void stopRecording() {
        //Stop Timer, very obvious
        timer.stop();

        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        //Change text on page to file saved
        filenameText.setText("Recording Stopped, File Saved : " + recordFile);

        File filelocation = new File(new File(requireActivity().getFilesDir().getAbsolutePath() + File.separator + "record"), recordFile);
        Uri path = FileProvider.getUriForFile(requireActivity(),"edu.imtl.bluekare.fileprovider", filelocation);
        Intent fileIntent = new Intent(Intent.ACTION_SEND);
        fileIntent.setType("audio/*");
        fileIntent.putExtra(Intent.EXTRA_SUBJECT, recordFile);
        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
        fileIntent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION );

        Intent chooser = Intent.createChooser(fileIntent, "Send Email");

        List<ResolveInfo> resInfoList = requireActivity().getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            requireActivity().grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        requireActivity().startActivity(chooser);
        Log.e("filelocation", filelocation.toString());
        Async_post_mp3 async_post_mp3=new Async_post_mp3(getActivity().getApplicationContext(), new String[]{p_name, d_name},filelocation.toString(),recordFile);
        async_post_mp3.execute();
    }

    private void startRecording() {
        //Start timer from 0
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        //Get app external directory path
        String recordPath = requireActivity().getFilesDir().getAbsolutePath() + File.separator + "record";

        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault());
        Date now = new Date();

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_" + p_name +"_"+ formatter.format(now) + ".mp3";

        filenameText.setText("Recording, File Name : " + recordFile);

        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(recordPath + File.separator + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start Recording
        mediaRecorder.start();
    }

    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(requireContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(requireActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isRecording){
            stopRecording();
        }
        //            FileOutputStream out = requireActivity().openFileOutput(recordFile, Context.MODE_PRIVATE);
//            out.write((survey.toString()).getBytes());
//            out.close();


    }
}
