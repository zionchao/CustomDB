package com.kevin.zhangchao.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kevin.zhangchao.libdb.Company;
import com.kevin.zhangchao.libdb.DBManager;
import com.kevin.zhangchao.libdb.Developer;
import com.kevin.zhangchao.libdb.Skill;
import com.kevin.zhangchao.libdb.Utilities.Trace;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBManager.getInstance(this);
        newOrUpdate();
//        queryById();
//        deleteById();
    }

    public void newOrUpdate(){
        Company company = new Company();
        company.setId("00001");
        company.setName("stay4it");
        company.setUrl("www.stay4it.com");
        company.setTel("10086");
        company.setAddress("Shanghai");
        Developer developer = new Developer();
        developer.setId("00001");
        developer.setName("Stay");
        developer.setAge(17);
        Skill skill = new Skill();
        skill.setName("coding");
        skill.setDesc("android");
        ArrayList<Skill> skills = new ArrayList<Skill>();
        skills.add(skill);
        developer.setSkills(skills);
        ArrayList<Developer> developers = new ArrayList<Developer>();
        developers.add(developer);
        company.setDevelopers(developers);
        DBManager.getInstance(this).getDao(Company.class).newOrUpdate(company);
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
