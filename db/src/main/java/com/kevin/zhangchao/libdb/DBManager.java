package com.kevin.zhangchao.libdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kevin.zhangchao.libdb.Utilities.TextUtil;

import java.lang.reflect.Field;

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

    public <T>void newOrUpdate(T t)
    {
        if (t.getClass().isAnnotationPresent(Table.class)){
            Field[] fields=t.getClass().getDeclaredFields();
            ContentValues values=new ContentValues();
            try {
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Column.class)){
                        field.setAccessible(true);
                        Class<?> clz=field.getType();
                        if (clz==String.class){
                            Object value=field.get(t);
                            if (value!=null)
                                values.put(DBUtil.getColumnName(field),value.toString());
                        }else  if (clz==int.class||clz==Integer.class){
                            values.put(DBUtil.getColumnName(field),field.getInt(t));
                        }else {
                            Column column=field.getAnnotation(Column.class);
                            Column.ColumnType type=column.type();
                            if (!TextUtil.isValidate(type.name())){
                                throw new IllegalArgumentException("you should set type to the special column:" + t.getClass().getSimpleName() + "."
                                        + field.getName());
                            }
                            //TODO 非常规类型
                            if (type== Column.ColumnType.SERIALIZABLE){

                            }else if(type== Column.ColumnType.TONE){

                            }else if(type== Column.ColumnType.TMANY){

                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
//        values.put("id",1);
            mDatabase.replace(DBUtil.getTableName(Developer.class),null,values);
        }
    }

    public <T>void delete(T t){
        String idName=DBUtil.getIDColumnName(t.getClass());
        try {
            //TODO 注解的名称，idName
            Field field=t.getClass().getField(idName);
            field.setAccessible(true);
            String id= (String) field.get(t);
            mDatabase.delete(DBUtil.getTableName(t.getClass()),idName+"=?",new String[]{id});
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public <T> T queryById(Class<T> clz,String id){
        Cursor cursor=mDatabase.rawQuery("select * from "+DBUtil.getTableName(clz)+" where "+
                DBUtil.getIDColumnName(clz)+"=?",new String[]{id});
        T t=null;
        if (cursor.moveToNext()){
            try {
                t=clz.newInstance();
                Field[] fields=clz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Column.class)){
                        Class<?> classType=field.getType();
                        if (classType==String.class){
                            field.set(t,field.get(t));
                        }else if (classType==int.class||classType==Integer.class){
                            field.setInt(t,field.getInt(t));
                        }else{
                            Column column=field.getAnnotation(Column.class);
                            Column.ColumnType columnType=column.type();
                            //TODO
                            if (columnType== Column.ColumnType.SERIALIZABLE){

                            }else if (columnType== Column.ColumnType.TONE){

                            }else if (columnType== Column.ColumnType.TMANY){

                            }
                        }
                    }
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return t;
    }
}
