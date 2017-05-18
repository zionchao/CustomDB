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
