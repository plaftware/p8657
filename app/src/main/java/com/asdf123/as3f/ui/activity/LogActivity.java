package com.asdf123.as3f.ui.activity;

import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.EditText;

import com.asdf123.as3f.R;
import com.asdf123.as3f.log.MyLog;
import com.asdf123.as3f.ui.activity.abs.AbstractActivity;

import butterknife.Bind;

public class LogActivity extends AbstractActivity {

    @Bind(R.id.logText)
    EditText log_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        log_text.setText(fromHtml(MyLog.dump()));
    }

    private Spanned fromHtml(String html){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }else{
            return Html.fromHtml(html);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_log;
    }
}
