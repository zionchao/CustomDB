package com.kevin.zhangchao.libdb;

import java.util.ArrayList;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

@Table(name="company")
public class Company {
    @Column(id = true)
    private String id;
    @Column
    private String name;
    @Column
    private String url;
    @Column
    private String tel;
    @Column
    private String address;

//    private String group;
    @Column(type= Column.ColumnType.TMANY,autoRefresh = true)
    private ArrayList<Developer> developers;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<Developer> getDevelopers() {
        return developers;
    }

    public void setDevelopers(ArrayList<Developer> developers) {
        this.developers = developers;
    }

    @Override
    public String toString() {
        return "id="+id+":"+
                "name="+name+":"+
                "url="+url+":"+
                "tel="+tel+":"+
                "address="+address+
                "developers="+developers;

    }
}
