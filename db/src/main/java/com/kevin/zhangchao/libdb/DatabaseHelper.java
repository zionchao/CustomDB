package com.kevin.zhangchao.libdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME="kevin_db";
    private static final int DB_VERSION=1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
