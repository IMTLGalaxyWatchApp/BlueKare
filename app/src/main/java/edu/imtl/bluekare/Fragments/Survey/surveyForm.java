package edu.imtl.bluekare.Fragments.Survey;

public class surveyForm {
    public String[] result_A=new String[9];
    public String[] result_B=new String[9];
    public String[] result_C=new String[10];
    public String[] result_D=new String[10];
    public String regiNm;

    public String[] final_result_A=new String[9];
    public String[] final_result_B=new String[9];
    public String[] final_result_C=new String[10];
    public String[] final_result_D=new String[10];

    public String[] get_A(){ return result_A;}
    public String[] get_B(){ return result_B;}
    public String[] get_C(){ return result_C;}
    public String[] get_D(){ return result_D;}

    public void set_A(String[] temp){ result_A=temp;}
    public void set_B(String[] temp){ result_B=temp;}
    public void set_C(String[] temp){ result_C=temp;}
    public void set_D(String[] temp){ result_D=temp;}

    public String get_Nm(){return regiNm;}
    public void set_Nm(String temp){regiNm=temp;}


    public void set_fianlA(String[] tempFinal){ final_result_A=tempFinal;}
    public void set_fianlB(String[] tempFinal){ final_result_B=tempFinal;}
    public void set_fianlC(String[] tempFinal){ final_result_C=tempFinal;}
    public void set_fianlD(String[] tempFinal){ final_result_D=tempFinal;}
}
