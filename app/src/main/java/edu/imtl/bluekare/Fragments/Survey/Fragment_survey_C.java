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


import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.result_C;
import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.final_result_C;

public class Fragment_survey_C extends Fragment {


    /*================= Question Type A  =====================*/
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
    private Button nextButtonC;
    private Button preButtonC;
    /*=======================================================*/

    String[] temp=new String[10];
    EditText[] edits=new EditText[6];
    Spinner[] spins=new Spinner[4];
    Integer[] temp_pos=new Integer[10];



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey_c, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        question1 = view.findViewById(R.id.record_questionc_1);
        question2_1_3 = view.findViewById(R.id.record_questionc_2_1_3);
        question2_2_3 = view.findViewById(R.id.record_questionc_2_2_3);
        question2_3_3 = view.findViewById(R.id.record_questionc_2_3_3);

        spins=new Spinner[]{question1, question2_1_3, question2_2_3, question2_3_3};

        question2_1_1 = view.findViewById(R.id.record_questionc_2_1_1);
        question2_1_2 = view.findViewById(R.id.record_questionc_2_1_2);
        question2_2_1 = view.findViewById(R.id.record_questionc_2_2_1);
        question2_2_2 = view.findViewById(R.id.record_questionc_2_2_2);
        question2_3_1 = view.findViewById(R.id.record_questionc_2_3_1);
        question2_3_2 = view.findViewById(R.id.record_questionc_2_3_2);

        edits=new EditText[]{question2_1_1, question2_1_2, question2_2_1, question2_2_2, question2_3_1, question2_3_2};
        temp_pos=new Integer[]{1,2,4,5,7,8,0,3,6,9};

        nextButtonC = view.findViewById(R.id.nextButtonC);
        preButtonC=view.findViewById(R.id.preButtonC);

        ArrayAdapter<String> adapter_bool = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, bool);
        adapter_bool.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        question1.setAdapter(adapter_bool);
        question2_1_3.setAdapter(adapter_bool);
        question2_2_3.setAdapter(adapter_bool);
        question2_3_3.setAdapter(adapter_bool);

        temp=result_C;
        setFrag(temp);

        nextButtonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFrag();
                convertSpin();
                getParentFragmentManager().beginTransaction().replace(R.id.survey_container, new Fragment_survey_D()).addToBackStack(null).commit();

            }
        });
        preButtonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFrag();
                convertSpin();
                getParentFragmentManager().beginTransaction().replace(R.id.survey_container, new Fragment_survey_B()).addToBackStack(null).commit();
            }
        });

    }
    private void saveFrag(){
        for(int i=0;i<6;i++){
            temp[temp_pos[i]]=edits[i].getText().toString();
        }
        for(int i=0;i<4;i++){
            temp[temp_pos[i+6]]=String.valueOf(spins[i].getSelectedItemPosition());
        }
    }
    private void setFrag(String[] temp){
        for(int i=0;i<6;i++){
            edits[i].setText(temp[temp_pos[i]]);
        }
        for(int i=0;i<4;i++) {
            if(temp[temp_pos[i+6]]!=null) spins[i].setSelection(Integer.parseInt(temp[temp_pos[i+6]]));
        }

    }
    private void convertSpin(){
        final_result_C=temp.clone();
        for(int i=0;i<4;i++){
            final_result_C[temp_pos[i+6]]=spins[i].getSelectedItem().toString();
        }
    }

}