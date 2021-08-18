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
import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.name;
import static edu.imtl.bluekare.Fragments.Survey.Fragment_survey.phoneNum;
public class Fragment_survey_Pre extends Fragment {


    private Button ButtonA;
    EditText _name;
    EditText _phoneNum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey_pre, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButtonA = view.findViewById(R.id.toA);
        _name=view.findViewById(R.id.editTextTextPersonName);
        _phoneNum=view.findViewById(R.id.editTextPhone);

        ButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name=_name.getText().toString();
                phoneNum=_phoneNum.getText().toString();
                getParentFragmentManager().beginTransaction().replace(R.id.survey_container, new Fragment_survey_A()).addToBackStack(null).commit();
            }
        });



    }
}