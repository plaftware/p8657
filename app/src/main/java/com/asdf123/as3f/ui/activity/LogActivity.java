package com.asdf123.as3f.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.asdf123.as3f.R;
import com.asdf123.as3f.log.MyLog;
import com.asdf123.as3f.ui.activity.abs.AbstractActivity;

import java.util.ArrayList;
import java.util.LinkedList;

import butterknife.Bind;

/**
 * Created by Softcomputo_BarriosFreddy on 13/01/17.
 */

public class LogActivity extends AbstractActivity {

    @Bind(R.id.tool_bar)
    Toolbar toolbar;

    @Bind(R.id.list)
    ListView list;

    private LogActivity activity;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        this.setSupportActionBar(toolbar);

        LinkedList<String> log = MyLog.dump();
        this.listarlog(log);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }




    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_log;
    }


    public void listarlog(LinkedList<String> log){

        if (!log.isEmpty()){
            Log.d("Quantity", "Number "+log.size());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                    android.R.layout.simple_list_item_1, android.R.id.text1, log.toArray(new String[0]));

            list.setAdapter(adapter);

        }


    }

}
