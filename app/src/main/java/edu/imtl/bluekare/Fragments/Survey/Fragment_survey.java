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

    static String[] result_A=new String[9];
    static String[] result_B=new String[9];
    static String[] result_C=new String[9];
    static String[] result_D=new String[9];
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager().beginTransaction().replace(R.id.survey_container, new Fragment_survey_Pre()).addToBackStack(null).commit();

    }
    public static String[] get_A(){ return result_A;}
    public static String[] get_B(){ return result_B;}
    public static String[] get_C(){ return result_C;}
    public static String[] get_D(){ return result_D;}

    public static void set_A(String[] temp){ result_A=temp;}
    public static void set_B(String[] temp){ result_B=temp;}
    public static void set_C(String[] temp){ result_C=temp;}
    public static void set_D(String[] temp){ result_D=temp;}
}