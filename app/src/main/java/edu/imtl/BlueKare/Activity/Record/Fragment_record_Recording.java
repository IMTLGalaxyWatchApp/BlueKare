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

import edu.imtl.BlueKare.R;


public class Fragment_record_Recording extends Fragment {

    /*================= Recording  =====================*/
    private Button RecordButton;
    /*=======================================================*/



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record_recording, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecordButton = view.findViewById(R.id.recordButton);



//        RecordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getParentFragmentManager().beginTransaction().replace(R.id.recordContainer, new Fragment_record_C()).commit();
//
//            }
//        });

    }
}