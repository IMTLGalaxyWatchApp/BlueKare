package edu.imtl.bluekare.Fragments.Survey;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import edu.imtl.bluekare.Fragments.MainMenu.Fragment_main;
import edu.imtl.bluekare.Main.MainActivity;
import edu.imtl.bluekare.R;
import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Fragment_survey_final extends Fragment {


    /*================= Question Type final  =====================*/


    /*=======================================================*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey_final, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StringBuilder survey = new StringBuilder();
        String Filename = "Survey_1" + ".csv";
        survey.append("성별,나이,거주지,사회 경제 상태,현재 직업,결혼 상태,교육 정도,총 교육 연학(년),총 삽화 횟수(현재 삽화 포함),현재의 우울증삽화가 시작된 시기,첫 번째 우울증삽화가 시작된 시기, 과거 삽화 시 정신병적 증상(환청, 피해의식) 동반, 자살에 대해서 심각한 고민을 해 본적 있습니까?(자살사고),자살에 대해서 구체적인 계획을 세워 본 적이 있습니까?(자살계획),자살시도를 한 적이 있습니까?, 계절성 변화,생리 전 증후군,가족이나 친척의 정신과 병력,환자와의 관계,진단명,치료유무,환자와의 관계,진단명,치료유무,환자와의 관계,진단명,치료유무, 과거 또는 현재 내외과적 질환 유무/Surgical history,진단명,진단연도,소견,진단명,진단연도,소견,진단명,진단연도,소견");
        survey.append("\n");

        String[] answers=joinArrays(final_result_A, final_result_B, final_result_C,final_result_D);
        for (String data : answers){
            survey.append(data).append(",");
        }
        try{
            FileOutputStream out = getActivity().openFileOutput(Filename, Context.MODE_PRIVATE);
            out.write((survey.toString()).getBytes(Charset.forName("EUC-KR")));
            out.close();

            File filelocation = new File(getActivity().getFilesDir(), Filename);
            Uri path = FileProvider.getUriForFile(getActivity(),"edu.imtl.bluekare.fileprovider", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, Filename);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            fileIntent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION );

            Intent chooser = Intent.createChooser(fileIntent, "Send Email");

            List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                getActivity().grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(chooser);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static String[] joinArrays(String[]... arrays) {
        int len = 0;
        for (String[] array : arrays) {
            len += array.length;
        }

        String[] result = (String[]) Array.newInstance(String.class, len);

        int offset = 0;
        for (String[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

}