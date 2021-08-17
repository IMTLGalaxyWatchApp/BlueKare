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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.imtl.bluekare.R;
public class Fragment_survey_Pre extends Fragment {


    private Button ButtonA;
    private EditText Fnum;
    private EditText Bnum;
    private String temp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey_pre, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButtonA = view.findViewById(R.id.toA);

        Fnum=view.findViewById(R.id.editTextNumber);
        Bnum=view.findViewById(R.id.editTextNumberPassword);

        ButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("주민등록번호", String.valueOf(Integer.valueOf(Fnum.getText().toString().length())));
                Log.e("주민등록번호 뒤", String.valueOf(Integer.valueOf(Bnum.getText().toString().length())));
                temp=Fnum.getText().toString()+"-"+Bnum.getText().toString();
                Log.e("풀 주민등록번호", temp);
                if(Fnum.getText().toString().length()!=6 || Bnum.getText().toString().length()!=7)
                    Toast.makeText(getContext(),"주민등록번호가 올바르지 않습니다",Toast.LENGTH_LONG).show();
                else {
                    getParentFragmentManager().beginTransaction().replace(R.id.survey_container, new Fragment_survey_A()).addToBackStack(null).commit();
                }
            }
        });



    }
}