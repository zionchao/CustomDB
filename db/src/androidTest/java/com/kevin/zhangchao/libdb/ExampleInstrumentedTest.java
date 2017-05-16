package com.kevin.zhangchao.libdb;

import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import com.kevin.zhangchao.libdb.Utilities.Trace;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest{

    @Before
    public void setUp() throws Exception {
        DBManager.getInstance(getContext());
    }

    @Test
    public void newOrUpdate(){
        Company company=new Company();
        company.setAddress("shenzhen");
        company.setId("00001");
        company.setName("andorid");
        company.setTel("136");
        DBManager.getInstance(getContext()).newOrUpdate(company);
    }

    @Test
    public void queryById(){
        Company company=DBManager.getInstance(getContext()).queryById(Company.class,"00001");
        Trace.d(company.toString());
    }


}
