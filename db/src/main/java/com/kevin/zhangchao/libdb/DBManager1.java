package com.kevin.zhangchao.libdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kevin.zhangchao.libdb.Utilities.SerializeUtil;
import com.kevin.zhangchao.libdb.Utilities.TextUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangchao_a on 2017/5/15.
 */

public class DBManager1 {

    private static DBManager1 mInstance;
    private final Context context;
    private final SQLiteOpenHelper mHelper;
    private final SQLiteDatabase mDatabase;

    private DBManager1(Context context) {
        this.context=context;
        mHelper=new DatabaseHelper(context);
        mDatabase=mHelper.getWritableDatabase();
    }

    public static void init(Context context){
        if (mInstance==null){
            mInstance=new DBManager1(context);
        }
    }

    public static DBManager1 getInstance(Context context) {
        if (mInstance==null){
            mInstance=new DBManager1(context);
        }
        return mInstance;
    }

    public <T>void newOrUpdate(T t)
    {
        if (t.getClass().isAnnotationPresent(Table.class)){
            Field[] fields=t.getClass().getDeclaredFields();
            ContentValues values=new ContentValues();
            try {
                Field idField=t.getClass().getDeclaredField(DBUtil.getIDColumnName(t.getClass()));
                idField.setAccessible(true);
                String idValue= (String) idField.get(t);
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
                                byte[]value= SerializeUtil.serialize(field.get(t));
                                values.put(DBUtil.getColumnName(field),value);
                            }else if(type== Column.ColumnType.TONE){
                                Object tone=field.get(t);
                                if (column.autoRefresh()){
                                    newOrUpdate(tone);
                                }else {
                                    if (tone.getClass().isAnnotationPresent(Table.class)) {
                                        String idName = DBUtil.getIDColumnName(tone.getClass());
                                        Field toneIdField = tone.getClass().getDeclaredField(idName);
                                        field.setAccessible(true);
                                        values.put(DBUtil.getColumnName(field), toneIdField.get(tone).toString());
                                    }
                                }
                            }else if(type== Column.ColumnType.TMANY){
                                List<Object> tmany= (List<Object>) field.get(t);
                                mDatabase.delete(DBUtil.getAsscociationTableName(t.getClass(),field.getName()),
                                        DBUtil.PK1+"=?",new String[]{idValue});
                                if (tmany!=null){
                                    ContentValues assciationValues=new ContentValues();
                                    for (Object obj:tmany){
                                        if (column.autoRefresh()){
                                            newOrUpdate(obj);
                                        }
                                        assciationValues.clear();
                                        assciationValues.put(DBUtil.PK1,idValue);
                                        String idName = DBUtil.getIDColumnName(obj.getClass());
                                        Field tmanyIdField = obj.getClass().getDeclaredField(idName);
                                        field.setAccessible(true);
                                        assciationValues.put(DBUtil.PK2,tmanyIdField.get(obj).toString());
                                        mDatabase.replace(DBUtil.getAsscociationTableName(t.getClass(),field.getName()),null,assciationValues);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }
            //        values.put("id",1);
            mDatabase.replace(DBUtil.getTableName(t.getClass()),null,values);
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
                        field.setAccessible(true);
                        Class<?> classType=field.getType();
                        if (classType==String.class){
                            field.set(t,cursor.getString(cursor.getColumnIndex(DBUtil.getColumnName(field))));
                        }else if (classType==int.class||classType==Integer.class){
                            field.set(t,cursor.getInt(cursor.getColumnIndex(DBUtil.getColumnName(field))));
                        }else{
                            Column column=field.getAnnotation(Column.class);
                            Column.ColumnType columnType=column.type();
                            //TODO
                            if (columnType== Column.ColumnType.SERIALIZABLE){
                               Object object= SerializeUtil.deserialize(cursor.getBlob(cursor.getColumnIndex(DBUtil.getColumnName(field))));
                                field.set(t,object);
                            }else if (columnType== Column.ColumnType.TONE){
                                String toneId=cursor.getString(cursor.getColumnIndex(DBUtil.getColumnName(field)));
                                if (!TextUtil.isValidate(toneId))
                                    continue;
                                Object tone=null;
                                if (column.autoRefresh()) {
                                    tone= queryById(field.getType(),toneId);
                                } else {
                                    String idName=DBUtil.getIDColumnName(field.getType());
                                    tone=field.getType().newInstance();
                                    Field toneField=tone.getClass().getDeclaredField(idName);
                                    toneField.setAccessible(true);
                                    toneField.set(tone,toneId);
                                }
                                field.set(t,tone);
                            }else if (columnType== Column.ColumnType.TMANY){
                                Class realtedClass= (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                                Cursor tmanyCursor=mDatabase.rawQuery("select * from "+DBUtil.getAsscociationTableName(clz,field.getName())+ " where " + DBUtil.PK1 + "=?", new String[]{id});
                                ArrayList list=new ArrayList();
                                String tmanyId=null;
                                Object tmany=null;
                                while (tmanyCursor.moveToNext()){
                                    tmanyId=tmanyCursor.getString(tmanyCursor.getColumnIndex(DBUtil.PK2));
                                    if (column.autoRefresh()){
                                        tmany=queryById(realtedClass,tmanyId);
                                    }else{
                                        tmany=realtedClass.newInstance();
                                        String idName=DBUtil.getIDColumnName(realtedClass);
                                        Field idField=realtedClass.getDeclaredField(idName);
                                        idField.setAccessible(true);
                                        idField.set(tmany,tmanyId);
                                    }
                                    list.add(tmany);
                                }
                                if (!TextUtil.isValidate(list))
                                    continue;
                                field.set(t,list);
                            }
                        }
                    }
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }
}
