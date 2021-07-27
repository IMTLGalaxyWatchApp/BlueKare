package edu.imtl.bluekare.Fragments.Download;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.imtl.bluekare.Fragments.Register.DatePickerActivity;
import edu.imtl.bluekare.Fragments.Register.RegisterActivity;
import edu.imtl.bluekare.R;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


public class Fragment_download extends Fragment {
    private Spinner period;
    String[] periodArray=new String[]{"전체","3개월","1개월","직접설정"};

    private Button btn;
    private CheckBox all, HR, ECG, Step, BP, OS, SS, Ex;
    private DatePickerDialog.OnDateSetListener callbackMethod;
    private TextView textView;
    private EditText searchName;
    int mYear, mMonth, mDay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        period=view.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, periodArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        period.setAdapter(adapter);

        btn=view.findViewById(R.id.button);

        HR=view.findViewById(R.id.checkBox);
        ECG=view.findViewById(R.id.checkBox2);
        Step=view.findViewById(R.id.checkBox3);
        BP=view.findViewById(R.id.checkBox4);
        OS=view.findViewById(R.id.checkBox5);
        SS=view.findViewById(R.id.checkBox6);
        Ex=view.findViewById(R.id.checkBox7);
        textView=view.findViewById(R.id.DateText);

        searchName=view.findViewById(R.id.searchName);

        Calendar calendar = new GregorianCalendar();

        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH)+1;
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        period.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position==0){
                    //전체
                    System.out.println("0 clicked");
                    textView.setText("전체");
                }
                else if(position==1){
                    //3개월 전
                    System.out.println("1 clicked");
                    String str=mYear+"-"+(mMonth-3)+"-"+mDay;
                    textView.setText(str);
                }
                else if(position==2){
                    //1개월전
                    String str=mYear+"-"+(mMonth-1)+"-"+mDay;
                    textView.setText(str);
                    System.out.println("2 clicked");
                }
                else{
                    //직접설정
                    Intent intent=new Intent(getActivity(), DatePickerActivity.class);
                    startActivityForResult(intent,100);
                    System.out.println("직접설정 clicked");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });





    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            if (resultCode != Activity.RESULT_OK) {
                Log.e("asdf","intent error");
                return;
            }
            String sendText = data.getExtras().getInt("mYear")+"-"+data.getExtras().getInt("mMonth")+"-"+data.getExtras().getInt("mDay");
            textView.setText(sendText);
        }
    }


}