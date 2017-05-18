package com.kevin.zhangchao.libdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.kevin.zhangchao.libdb.Utilities.SerializeUtil;
import com.kevin.zhangchao.libdb.Utilities.TextUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by zhangchao_a on 2017/5/18.
 */

public class BaseDao<T> {

    private final Context context;
    private final Class<T> clz;
    private final SQLiteDatabase mDatabase;
    private String mTableName;
    private String mIdName;
    private Field mIdField;

    public BaseDao(Context context, Class<T> clz, SQLiteDatabase db) {
        this.context=context;
        this.clz=clz;
        try {
            this.mTableName=DBUtil.getTableName(clz);
            this.mIdName=DBUtil.getIDColumnName(clz);
            mIdField=clz.getDeclaredField(DBUtil.getIDColumnName(clz));
            mIdField.setAccessible(true);
            this.mDatabase=db;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void newOrUpdate(T t)
    {
        if (t.getClass().isAnnotationPresent(Table.class)){
            Field[] fields=t.getClass().getDeclaredFields();
            ContentValues values=new ContentValues();
            try {
                String idValue= (String) mIdField.get(t);
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
                                delete(DBUtil.getAsscociationTableName(t.getClass(),field.getName()), DBUtil.PK1+"=?",new String[]{idValue});
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
//                                        mDatabase.replace(DBUtil.getAsscociationTableName(t.getClass(),field.getName()),null,assciationValues);
                                        newOrUpdate(DBUtil.getAsscociationTableName(t.getClass(),field.getName()),assciationValues);
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
//            mDatabase.replace(DBUtil.getTableName(t.getClass()),null,values);
            newOrUpdate(DBUtil.getTableName(t.getClass()),values);
        }
    }

    public void newOrUpdate(String tableName,ContentValues values){
        mDatabase.replace(tableName,null,values);
    }

    public void delete(String tableName,String where,String args[]){
        mDatabase.delete(tableName, where,args);
    }
}
