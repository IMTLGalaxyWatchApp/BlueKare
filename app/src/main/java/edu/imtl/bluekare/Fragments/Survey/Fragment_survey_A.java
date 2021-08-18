package edu.imtl.bluekare.Fragments.Survey;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.imtl.bluekare.R;
import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.result_A;
import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.final_result_A;

public class Fragment_survey_A extends Fragment {

    private EditText patient_age;
    private EditText patient_address;
    private EditText patient_education_total;
    private Spinner patient_sex;

    String[] sex = new String[]{"남", "녀"};
    private Spinner patient_socialstatus;
    String[] socialstatus = new String[]{"상", "중상", "중", "중하", "하"};
    private Spinner patient_job;
    String[] job = new String[]{"전문.관리 ", "사무 ", "농업.노무 ", "주부", "학생", "무직"};
    private Spinner patient_marriage;
    String[] marriage = new String[]{"미혼 ", "동거", "기혼 ", "재혼 ", "이혼 ", "별거 ", "사별", "기타 "};
    private Spinner patient_education;
    String[] education = new String[]{"무학", "초졸", "중졸", "고졸", "전문대졸", "대졸", "대학원졸"};
    private Button nextButtonA;

    String[] temp=new String[8];


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey_a, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        patient_age = view.findViewById(R.id.patient_age);
        patient_address = view.findViewById(R.id.patient_address);
        patient_education_total = view.findViewById(R.id.patient_education_total);
        patient_sex = view.findViewById(R.id.patient_sex);
        patient_socialstatus = view.findViewById(R.id.patient_socialstatus);
        patient_job = view.findViewById(R.id.patient_job);
        patient_marriage = view.findViewById(R.id.patient_marriage);
        patient_education = view.findViewById(R.id.patient_education);
        nextButtonA = view.findViewById(R.id.nextButtonA);

        temp=result_A;


        ArrayAdapter<String> adapter_sex = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, sex);
        adapter_sex.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        patient_sex.setAdapter(adapter_sex);

        ArrayAdapter<String> adapter_socialstatus = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, socialstatus);
        adapter_socialstatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        patient_socialstatus.setAdapter(adapter_socialstatus);

        ArrayAdapter<String> adapter_job = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, job);
        adapter_job.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        patient_job.setAdapter(adapter_job);

        ArrayAdapter<String> adapter_marriage = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, marriage);
        adapter_marriage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        patient_marriage.setAdapter(adapter_marriage);

        ArrayAdapter<String> adapter_education = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, education);
        adapter_education.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        patient_education.setAdapter(adapter_education);

        setFrag(); //위치 어레이 어뎁터 뒤로 고정


        nextButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFrag();
                convertSpin();
                getParentFragmentManager().beginTransaction().replace(R.id.survey_container, new Fragment_survey_B()).addToBackStack(null).commit();

            }
        });

    }
    public void saveFrag(){
        temp[0]=String.valueOf(patient_sex.getSelectedItemPosition());
        temp[1]=patient_age.getText().toString();
        temp[2]=patient_address.getText().toString();
        temp[3]=String.valueOf(patient_socialstatus.getSelectedItemPosition());
        temp[4]=String.valueOf(patient_job.getSelectedItemPosition());
        temp[5]=String.valueOf(patient_marriage.getSelectedItemPosition());
        temp[6]=String.valueOf(patient_education.getSelectedItemPosition());
        temp[7]=patient_education_total.getText().toString();

        //for(int i=0;i<9;i++) Log.e(String.valueOf(i),temp[i]);
    }
    public void setFrag(){
//        for(int i=0;i<9;i++)
//            if(temp[i]!=null) Log.e(String.valueOf(i),temp[i]);
        if(temp[0]!=null)patient_sex.setSelection(Integer.valueOf(temp[0]));
        patient_age.setText(temp[1]);
        patient_address.setText(temp[2]);
        if(temp[3]!=null)patient_socialstatus.setSelection(Integer.valueOf(temp[3]));
        if(temp[4]!=null)patient_job.setSelection(Integer.valueOf(temp[4]));
        if(temp[5]!=null)patient_marriage.setSelection(Integer.valueOf(temp[5]));
        if(temp[6]!=null)patient_education.setSelection(Integer.valueOf(temp[6]));
        patient_education_total.setText(temp[7]);
    }
    public void convertSpin(){
        final_result_A=temp.clone();
        final_result_A[0]=patient_sex.getSelectedItem().toString();
        final_result_A[3]=patient_socialstatus.getSelectedItem().toString();
        final_result_A[4]=patient_job.getSelectedItem().toString();
        final_result_A[5]=patient_marriage.getSelectedItem().toString();
        final_result_A[6]=patient_education.getSelectedItem().toString();
        for(int i=0;i<8;i++)
            if(temp[i]!=null) Log.e(String.valueOf(i),temp[i]);
    }
}