package edu.imtl.bluekare.Fragments.Survey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.imtl.bluekare.R;



public class Fragment_survey extends Fragment {
    static String name;
    static String phoneNum;

    static String[] result_A=new String[8];
    static String[] result_B=new String[9];
    static String[] result_C=new String[10];
    static String[] result_D=new String[10];

    static String[] final_result_A=new String[9];
    static String[] final_result_B=new String[9];
    static String[] final_result_C=new String[10];
    static String[] final_result_D=new String[10];
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager().beginTransaction().replace(R.id.survey_container, new Fragment_survey_Pre()).addToBackStack(null).commit();

    }
}