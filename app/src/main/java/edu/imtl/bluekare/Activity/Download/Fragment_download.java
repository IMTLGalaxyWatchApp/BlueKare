package edu.imtl.bluekare.Activity.Download;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import edu.imtl.bluekare.R;


public class Fragment_download extends Fragment {
    private Spinner period;
    String[] periodArray=new String[]{"전체","3개월","1개월","직접설정"};

    private Button btn;
    private CheckBox all, HR, ECG, Step, BP, OS, SS, Ex;
    private DatePickerDialog.OnDateSetListener callbackMethod;
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


        period.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    //전체
                    System.out.println("0 clicked");
                }
                else if(position==1){
                    //1개월 전
                    System.out.println("1 clicked");
                }
                else if(position==2){
                    //1시간 전
                    System.out.println("2 clicked");
                }
                else{
                    //직접설정
                    selectDate();
                    System.out.println("직접설정 clicked");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }
    void selectDate(){
        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                updateDisplay();
            }
        };

        DatePickerDialog d = new DatePickerDialog(getActivity(),mDateSetListener, mYear, mMonth, mDay);
        d.show();
    }
    private void updateDisplay() {

        GregorianCalendar c = new GregorianCalendar(mYear, mMonth, mDay);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");

       // periodArray[3]=sdf.format(c.getTime());

        sdf = new SimpleDateFormat("yyyy-MM-dd");

        String transDateString=sdf.format(c.getTime());
        //periodArray[3]=transDateString;
    }

}