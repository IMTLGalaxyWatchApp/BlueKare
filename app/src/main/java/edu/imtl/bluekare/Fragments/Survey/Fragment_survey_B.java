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
import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.result_B;
import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.final_result_B;


public class Fragment_survey_B extends Fragment {


    /*================= Question Type B  =====================*/
    private EditText question1;
    private EditText question2;
    private EditText question3;
    private EditText question4;
    private EditText question7;
    private Spinner question5;
    private Spinner question6;
    private Spinner question8;
    private Spinner question9;
    String[] bool = new String[]{"예", "아니오"};
    private Button nextButtonB;
    private Button preButtonB;
    /*=======================================================*/

    String[] temp=new String[9];
    EditText[] edits=new EditText[5];
    Spinner[] spins=new Spinner[4];
    Integer[] temp_pos=new Integer[9];


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey_b, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        question1 = view.findViewById(R.id.record_questionb_1);
        question2 = view.findViewById(R.id.record_questionb_2);
        question3 = view.findViewById(R.id.record_questionb_3);
        question4 = view.findViewById(R.id.record_questionb_4);
        question7 = view.findViewById(R.id.record_questionb_7);

        edits= new EditText[]{question1, question2, question3, question4, question7};
        temp_pos=new Integer[]{1,2,3,4,7,5,6,8,9};

        question5 = view.findViewById(R.id.record_questionb_5);
        question6 = view.findViewById(R.id.record_questionb_6);
        question8 = view.findViewById(R.id.record_questionb_8);
        question9 = view.findViewById(R.id.record_questionb_9);
        nextButtonB = view.findViewById(R.id.nextButtonB);
        preButtonB=view.findViewById(R.id.preButtonB);

        ArrayAdapter<String> adapter_bool = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, bool);
        adapter_bool.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        question5.setAdapter(adapter_bool);
        question6.setAdapter(adapter_bool);
        question8.setAdapter(adapter_bool);
        question9.setAdapter(adapter_bool);

        spins=new Spinner[]{question5,question6,question8,question9};

        temp=result_B;
        setFrag(temp);


        nextButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFrag();
                convertSpin();
                getParentFragmentManager().beginTransaction().replace(R.id.survey_container, new Fragment_survey_C()).addToBackStack(null).commit();
            }
        });
        preButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFrag();
                convertSpin();
                getParentFragmentManager().beginTransaction().replace(R.id.survey_container, new Fragment_survey_A()).addToBackStack(null).commit();
            }
        });

    }

    private void saveFrag(){
        for(int i=0;i<5;i++){
            temp[temp_pos[i]-1]=edits[i].getText().toString();
        }
        for(int i=0;i<4;i++){
            temp[temp_pos[i+5]-1]=String.valueOf(spins[i].getSelectedItemPosition());
        }
    }
    private void setFrag(String[] temp){
        for(int i=0;i<5;i++){
            edits[i].setText(temp[temp_pos[i]-1]);
        }
        for(int i=0;i<4;i++) {
            if(temp[temp_pos[i+5]-1]!=null) spins[i].setSelection(Integer.parseInt(temp[temp_pos[i+5]-1]));
        }

    }
    private void convertSpin(){
        final_result_B=temp.clone();
        for(int i=0;i<4;i++){
            final_result_B[temp_pos[i+5]-1]=spins[i].getSelectedItem().toString();
        }
    }
}