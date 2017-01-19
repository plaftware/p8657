package com.asdf123.as3f.ui.activity;

import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.asdf123.as3f.R;
import com.asdf123.as3f.service.Util;
import com.asdf123.as3f.service.as3fService;
import com.asdf123.as3f.ui.activity.abs.AbstractActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;

public class ProfileActivity extends AbstractActivity{

    @Bind(R.id.idTextView)
    TextView idTextView;

    @Bind(R.id.networkReleaseText)
    TextView networkReleaseText;

    @Bind(R.id.expirationInfoView)
    TextView expirationInfoView;

    @Bind(R.id.consumo)
    TextView consumo;

    @Bind(R.id.tool_bar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toolbar.setTitle(R.string.app_name_profile);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        this.setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadInfoUser();
    }

    private void loadInfoUser() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        long fechaExpiracion = sharedPref.getLong("user_fechaExpiracion", 0l);

        if(fechaExpiracion != 0){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            networkReleaseText.setText(simpleDateFormat.format(new Date(fechaExpiracion)));
        }else{
            networkReleaseText.setVisibility(View.GONE);
            expirationInfoView.setVisibility(View.GONE);
        }

        idTextView.setText(sharedPref.getString("user_name", ""));

        consumo.setText(android.text.format.Formatter.formatFileSize(this, getTraffic()));
    }

    private long getTraffic(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(as3fService.vpn_ready){
            return sharedPref.getLong("traffic", 0) + (Util.getTraffic() - sharedPref.getLong("traffic0", 0));
        }
        return  sharedPref.getLong("traffic", 0);
    }


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_profile;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
