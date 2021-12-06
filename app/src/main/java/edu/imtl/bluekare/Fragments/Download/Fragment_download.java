package edu.imtl.bluekare.Fragments.Download;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import edu.imtl.bluekare.Fragments.Register.DatePickerActivity;
import edu.imtl.bluekare.Fragments.Survey.Async_post_mqtt_survey;
import edu.imtl.bluekare.Main.NDSpinner;
import edu.imtl.bluekare.R;


public class Fragment_download extends Fragment {
    private NDSpinner period;
    String[] periodArray=new String[]{"전체","3개월","1개월","직접설정"};
    String[] genderArray=new String[]{"모름","남성","여성"};
    String[] typeArray=new String[]{"설문조사 결과","헬스 데이터"};
    Spinner downloadtype;
    private Button btn;
    private CheckBox all, HR, ECG, Step, BP, OS, SS, Ex;
    private DatePickerDialog.OnDateSetListener callbackMethod;
    private TextView textView;
    private EditText searchName, phonenum;
    int mYear, mMonth, mDay;
    int[] types=new int[7];
    private Spinner gender;

    LinearLayout check;

    String name,sex,phone;

    int download;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        period=view.findViewById(R.id.spinner);
        gender=view.findViewById(R.id.spinner2);
        check=view.findViewById(R.id.checks);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, periodArray);
        ArrayAdapter<String> adapter2=new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, genderArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        period.setAdapter(adapter);
        gender.setAdapter(adapter2);

        downloadtype=view.findViewById(R.id.spinner4);
        ArrayAdapter<String> adapter3=new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, typeArray);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        downloadtype.setAdapter(adapter3);

        phonenum=view.findViewById(R.id.phone);

        btn=view.findViewById(R.id.button);

        HR=view.findViewById(R.id.checkBox);
        ECG=view.findViewById(R.id.checkBox2);
        Step=view.findViewById(R.id.checkBox3);
        BP=view.findViewById(R.id.checkBox4);
        OS=view.findViewById(R.id.checkBox5);
        SS=view.findViewById(R.id.checkBox6);
        Ex=view.findViewById(R.id.checkBox7);

        CheckBox[] checks=new CheckBox[]{HR,ECG,Step,BP,OS,SS,Ex};

        ECG.setEnabled(false);
        ECG.setChecked(false);
        types[0]=HR.isChecked()?1:0;
        types[1]=ECG.isChecked()?1:0;
        types[2]=Step.isChecked()?1:0;
        types[3]=BP.isChecked()?1:0;
        types[4]=OS.isChecked()?1:0;
        types[5]=SS.isChecked()?1:0;
        types[6]=Ex.isChecked()?1:0;

        textView=view.findViewById(R.id.DateText);

        searchName=view.findViewById(R.id.searchName);
        name="";
        phone="";


        Calendar calendar = new GregorianCalendar();

//        mYear = calendar.get(Calendar.YEAR);
//        mMonth = calendar.get(Calendar.MONTH)+1;
//        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        period.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH)+1;
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
                if(position==0){
                    //전체
                    System.out.println("0 clicked");
                    textView.setText("전체");
                    mYear = 0;
                    mMonth = 0;
                    mDay = 0;
                }
                else if(position==1){
                    //3개월 전
                    System.out.println("1 clicked");
                    String str;
                    if(mMonth<=3){
                        str=(mYear-1)+"-"+(mMonth+9)+"-"+mDay;
                    }
                    else{
                        str=mYear+"-"+(mMonth-3)+"-"+mDay;
                    }

                    textView.setText(str+"부터");
                }
                else if(position==2){
                    //1개월전
                    String str;

                    if(mMonth<=1){
                        str=(mYear-1)+"-"+(mMonth+11)+"-"+mDay;
                    }
                    else{
                        str=mYear+"-"+(mMonth-1)+"-"+mDay;
                    }
                    textView.setText(str+"부터");
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

        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i==0){
                    sex="";
                }
                else if(i==1) sex="남성";
                else sex="여성";
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        downloadtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    download=0;
                    check.setVisibility(View.INVISIBLE);
                }
                else{
                    download=1;
                    check.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Calendar dest_cal = new GregorianCalendar();
        dest_cal.set(mYear, mMonth, mDay);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i:types) Log.d("download", String.valueOf(i));
                Log.d("download",String.valueOf(dest_cal.getTimeInMillis()));
                name=searchName.getText().toString();
                phone=phonenum.getText().toString();
                if(download==1) {
                    Async_fetch_mongodb_health fetch_mongodb=new Async_fetch_mongodb_health(getActivity().getApplicationContext(), new String[]{name, String.valueOf(dest_cal.getTimeInMillis()),sex,phone}, types);
                    fetch_mongodb.execute();}
                else{
                    Async_fetch_mongodb_survey async_fetch_mongodb_survey=new Async_fetch_mongodb_survey(getActivity().getApplicationContext(), new String[]{name, String.valueOf(dest_cal.getTimeInMillis()),sex,phone});
                    async_fetch_mongodb_survey.execute();
                }
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
            textView.setText(sendText+"부터");
        }
    }


}