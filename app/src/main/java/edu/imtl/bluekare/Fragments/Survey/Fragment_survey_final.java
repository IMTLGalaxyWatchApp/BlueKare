package edu.imtl.bluekare.Fragments.Survey;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import edu.imtl.bluekare.Main.MainActivity;
import edu.imtl.bluekare.Main.userINFO;
import edu.imtl.bluekare.R;
import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Date;
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

//        StringBuilder survey = new StringBuilder();
//        String Filename = "Survey_"+".csv";
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            Filename = "Survey_" + java.time.LocalDate.now() +".csv";
//        }
        String str="성별,나이,거주지,사회 경제 상태,현재 직업,결혼 상태,교육 정도,총 교육 연학(년),총 삽화 횟수(현재 삽화 포함),현재의 우울증삽화가 시작된 시기," +
                "첫 번째 우울증삽화가 시작된 시기,과거 삽화 시 정신병적 증상(환청 및 피해의식) 동반,자살에 대해서 심각한 고민을 해 본적 있습니까?(자살사고),자살에 대해서 구체적인 계획을 세워 본 적이 있습니까?" +
                "(자살계획),자살시도를 한 적이 있습니까?,계절성 변화,생리 전 증후군,가족이나 친척의 정신과 병력,1)환자와의 관계,1)진단명,1)치료유무,2)환자와의 관계,2)진단명,2)치료유무,3)환자와의 관계,3)진단명,3)치료유무, " +
                "과거 또는 현재 내외과적 질환 유무/Surgical history,1)진단명,1)진단연도,1)소견,2)진단명,2)진단연도,2)소견,3)진단명,3)진단연도,3)소견";
        JSONObject jsonObject = new JSONObject();
        String[] questions= str.split(",");


        String[] answers=joinArrays(final_result_A, final_result_B, final_result_C,final_result_D);

        System.out.println(questions.length);
        System.out.println(answers.length);
        for (int i=0; i<questions.length;i++){
            try {
                jsonObject.put(questions[i],answers[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("json", "생성한 json : " + jsonObject.toString());


        Async_post_mqtt_survey async_post_mqtt_survey=new Async_post_mqtt_survey(getActivity().getApplicationContext(), new String[]{((userINFO)getActivity().getApplication()).getUid(),name, phoneNum}, jsonObject);
        async_post_mqtt_survey.execute();


//        try{
//            FileOutputStream out = requireActivity().openFileOutput(Filename, Context.MODE_PRIVATE);
//            out.write((survey.toString()).getBytes(Charset.forName("EUC-KR")));
//            out.close();
//
//            File filelocation = new File(requireActivity().getFilesDir(), Filename);
//            Uri path = FileProvider.getUriForFile(requireActivity(),"edu.imtl.bluekare.fileprovider", filelocation);
//            Intent fileIntent = new Intent(Intent.ACTION_SEND);
//            fileIntent.setType("text/csv");
//            fileIntent.putExtra(Intent.EXTRA_SUBJECT, Filename);
//            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
//            fileIntent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION );
//
//            Intent chooser = Intent.createChooser(fileIntent, "Send Email");
//
//            List<ResolveInfo> resInfoList = requireActivity().getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
//
//            for (ResolveInfo resolveInfo : resInfoList) {
//                String packageName = resolveInfo.activityInfo.packageName;
//                requireActivity().grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            }
//
//            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            requireActivity().startActivity(chooser);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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