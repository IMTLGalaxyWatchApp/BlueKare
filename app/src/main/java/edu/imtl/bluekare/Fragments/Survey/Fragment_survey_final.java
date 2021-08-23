package edu.imtl.bluekare.Fragments.Survey;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.imtl.bluekare.Fragments.MainMenu.Fragment_main;
import edu.imtl.bluekare.Main.MainActivity;
import edu.imtl.bluekare.R;
import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
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

        ArrayList<Uri> uris=new ArrayList<Uri>();
        ArrayList<String[]> csv= new ArrayList<String[]>();

        String[] questions=new String[]{"성별","나이","거주지","사회 경제 상태","현재 직업","결혼 상태","교육 정도","총 교육 연학(년)","총 삽화 횟수(현재 삽화 포함)","현재의 우울증삽화가 시작된 시기","첫 번째 우울증삽화가 시작된 시기", "과거 삽화 시 정신병적 증상(환청, 피해의식) 동반", "자살에 대해서 심각한 고민을 해 본적 있습니까?(자살사고)","자살에 대해서 구체적인 계획을 세워 본 적이 있습니까?(자살계획)","자살시도를 한 적이 있습니까?", "계절성 변화","생리 전 증후군","가족이나 친척의 정신과 병력","환자와의 관계","진단명","치료유무","환자와의 관계","진단명","치료유무","환자와의 관계","진단명","치료유무", "과거 또는 현재 내외과적 질환 유무/Surgical history","진단명","진단연도","소견","진단명","진단연도","소견","진단명","진단연도","소견"};
        String[] answers=joinArrays(final_result_A, final_result_B, final_result_C,final_result_D);

        csv.add(questions);
        csv.add(answers);

        Calendar calendar=new GregorianCalendar();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH)+1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        CSVWriter writer;
        try {
            String filepath="피험자 정보 수집"+name+"_"+String.valueOf(mYear)+"_"+String.valueOf(mMonth)+"_"+String.valueOf(mDay)+".csv";

            writer = new CSVWriter(new FileWriter(filepath));
            writer.writeAll(csv);
            writer.close();

            File fileCsv = new File(filepath);
            Uri u = Uri.fromFile(fileCsv);
            uris.add(u);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent emailIntent=new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[]{""});
        emailIntent.putExtra(android.content.Intent.EXTRA_CC,
                new String[]{""});

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(Intent.createChooser(emailIntent, "Send mail"));


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