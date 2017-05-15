package com.kevin.zhangchao.libdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

public class DBManager {

    private static DBManager mInstance;
    private final Context context;
    private final SQLiteOpenHelper mHelper;
    private final SQLiteDatabase mDatabase;

    private DBManager(Context context, SQLiteOpenHelper helper) {
        this.context=context;
        mHelper=helper;
        mDatabase=mHelper.getWritableDatabase();
    }

    public static void init(Context context,SQLiteOpenHelper helper){
        if (mInstance==null){
            mInstance=new DBManager(context,helper);
        }
    }

    public static DBManager getInstance() {
        return mInstance;
    }

    public void newOrUpdate(Developer developer)
    {
        String sql="create table(id TEXT primary key NOT NULL , name TEXT, age TEXT, company TEXT, skills TEXT)";
        ContentValues values=new ContentValues();
        values.put("id",1);
        mDatabase.replace(DBUtil.getTableName(Developer.class),null,values);
    }

    public void delete(Developer developer){
        mDatabase.delete(DBUtil.getTableName(developer.getClass()),"id=?",new String[]{developer.getId()});
    }

    public Developer queryById(String id){
        Cursor cursor=mDatabase.rawQuery("select * from "+DBUtil.getTableName(Developer.class)+" where id=?",new String[]{id});
        Developer developer=null;
        if (cursor.moveToNext()){
            developer=new Developer();
            developer.setId(cursor.getString(cursor.getColumnIndex("id")));
            developer.setAge(cursor.getInt(cursor.getColumnIndex("age")));
        }
        return developer;
    }
}
