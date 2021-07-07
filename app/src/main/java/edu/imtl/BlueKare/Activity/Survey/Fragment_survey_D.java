package edu.imtl.BlueKare.Activity.Survey;

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

import edu.imtl.BlueKare.R;

public class Fragment_survey_D extends Fragment {


    /*================= Question Type D  =====================*/
    private EditText question2_1_1;
    private EditText question2_1_2;
    private EditText question2_2_1;
    private EditText question2_2_2;
    private EditText question2_3_1;
    private EditText question2_3_2;
    private Spinner question1;
    private Spinner question2_1_3;
    private Spinner question2_2_3;
    private Spinner question2_3_3;
    String[] bool = new String[]{"예", "아니오"};
    private Button nextButtonD;
    /*=======================================================*/



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey_d, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        question1 = view.findViewById(R.id.record_questiond_1);
        question2_1_1 = view.findViewById(R.id.record_questiond_2_1_1);
        question2_1_2 = view.findViewById(R.id.record_questiond_2_1_2);
        question2_1_3 = view.findViewById(R.id.record_questiond_2_1_3);
        question2_2_1 = view.findViewById(R.id.record_questiond_2_2_1);
        question2_2_2 = view.findViewById(R.id.record_questiond_2_2_2);
        question2_2_3 = view.findViewById(R.id.record_questiond_2_2_3);
        question2_3_1 = view.findViewById(R.id.record_questiond_2_3_1);
        question2_3_2 = view.findViewById(R.id.record_questiond_2_3_2);
        question2_3_3 = view.findViewById(R.id.record_questiond_2_3_3);
        nextButtonD = view.findViewById(R.id.nextButtonD);

        ArrayAdapter<String> adapter_bool = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, bool);
        adapter_bool.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        question1.setAdapter(adapter_bool);
        question2_1_3.setAdapter(adapter_bool);
        question2_2_3.setAdapter(adapter_bool);
        question2_3_3.setAdapter(adapter_bool);


        nextButtonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.recordContainer, new Fragment_survey_final()).commit();

            }
        });

    }
}