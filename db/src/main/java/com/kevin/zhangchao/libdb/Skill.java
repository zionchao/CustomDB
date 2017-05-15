package com.kevin.zhangchao.libdb;

import java.io.Serializable;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

public class Skill implements Serializable{

    private String name;
    private String desc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
