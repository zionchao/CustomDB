package com.kevin.zhangchao.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kevin.zhangchao.libdb.Company;
import com.kevin.zhangchao.libdb.DBManager;
import com.kevin.zhangchao.libdb.Utilities.Trace;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBManager.getInstance(this);
//        newOrUpdate();
//        queryById();
        deleteById();
    }

    public void newOrUpdate(){
        Company company=new Company();
        company.setAddress("shenzhen");
        company.setId("00001");
        company.setName("andorid");
        company.setTel("136");
        DBManager.getInstance(this).newOrUpdate(company);
    }

    public void queryById(){
        Company company=DBManager.getInstance(this).queryById(Company.class,"00001");
        Trace.d(company.toString());
    }

    public void deleteById()
    {
        Company company=new Company();
        company.setId("00001");
        DBManager.getInstance(this).delete(company);
        queryById();
    }

}
