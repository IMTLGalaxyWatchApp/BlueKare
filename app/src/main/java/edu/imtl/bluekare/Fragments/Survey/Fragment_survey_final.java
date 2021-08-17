package edu.imtl.bluekare.Fragments.Survey;

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

    }
}