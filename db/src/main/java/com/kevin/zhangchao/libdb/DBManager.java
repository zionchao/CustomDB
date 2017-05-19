package com.kevin.zhangchao.libdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kevin.zhangchao.libdb.Utilities.SerializeUtil;
import com.kevin.zhangchao.libdb.Utilities.TextUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

public class DBManager {

    private static DBManager mInstance;
    private final Context context;
    private final SQLiteOpenHelper mHelper;
    private final SQLiteDatabase mDatabase;

    private HashMap<String,BaseDao> mCacheDao;

    private DBManager(Context context) {
        this.context=context;
        mHelper=new DatabaseHelper(context);
        mDatabase=mHelper.getWritableDatabase();
        mCacheDao=new HashMap<>();
    }

    public static void init(Context context){
        if (mInstance==null){
            mInstance=new DBManager(context);
        }
    }

    public static DBManager getInstance(Context context) {
        if (mInstance==null){
            mInstance=new DBManager(context);
        }
        return mInstance;
    }


    public <T> BaseDao<T> getDao(Class<T> clz){
        if (mCacheDao.containsKey(clz.getName())){
            return mCacheDao.get(clz.getSimpleName());
        }else{
            BaseDao<T> dao=new BaseDao<>(context,clz,mDatabase);
            mCacheDao.put(clz.getSimpleName(),dao);
            return dao;
        }
    }

    public <T>void delete(T t){
        try {
            String idName=DBUtil.getIDColumnName(t.getClass());
            //TODO 注解的名称，idName
            Field field=t.getClass().getDeclaredField(idName);
            field.setAccessible(true);
            String id= (String) field.get(t);
            mDatabase.delete(DBUtil.getTableName(t.getClass()),idName+"=?",new String[]{id});
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}
