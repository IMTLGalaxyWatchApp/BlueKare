package edu.imtl.BlueKare.Activity.Record;

import android.os.Bundle;
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
import androidx.fragment.app.FragmentManager;

import edu.imtl.BlueKare.Activity.MainMenu.Fragment_main;
import edu.imtl.BlueKare.R;


public class Fragment_record_A extends Fragment {

    private EditText patient_name;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record_a, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        patient_name = view.findViewById(R.id.patient_name);
        patient_age = view.findViewById(R.id.patient_age);
        patient_address = view.findViewById(R.id.patient_address);
        patient_education_total = view.findViewById(R.id.patient_education_total);
        patient_sex = view.findViewById(R.id.patient_sex);
        patient_socialstatus = view.findViewById(R.id.patient_socialstatus);
        patient_job = view.findViewById(R.id.patient_job);
        patient_marriage = view.findViewById(R.id.patient_marriage);
        patient_education = view.findViewById(R.id.patient_education);
        nextButtonA = view.findViewById(R.id.nextButtonA);

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

        nextButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.recordContainer, new Fragment_record_B()).commit();

            }
        });

    }
}