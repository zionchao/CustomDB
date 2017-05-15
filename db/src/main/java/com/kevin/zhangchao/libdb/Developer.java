package com.kevin.zhangchao.libdb;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

public class Developer implements Serializable{

    private String id;
    private String name;
    private int age;
    private Company company;
    private ArrayList<Skill> skills;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ArrayList<Skill> getSkills() {
        return skills;
    }

    public void setSkills(ArrayList<Skill> skills) {
        this.skills = skills;
    }
}
