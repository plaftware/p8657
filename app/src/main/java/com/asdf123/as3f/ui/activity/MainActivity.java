package com.asdf123.as3f.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.TrafficStats;
import android.net.VpnService;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.asdf123.as3f.BuildConfig;
import com.asdf123.as3f.R;
import com.asdf123.as3f.log.MyLog;
import com.asdf123.as3f.resource.token.VPNManager;
import com.asdf123.as3f.service.Util;
import com.asdf123.as3f.service.as3fService;
import com.asdf123.as3f.ui.activity.abs.AbstractActivity;
import com.asdf123.as3f.ui.activity.nav.Navigator;
import com.asdf123.as3f.utils.AnimationImageUtil;
import com.asdf123.as3f.utils.RTXTraffic;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.Bind;

public class MainActivity extends AbstractActivity implements VPNManager.VPNListener, RTXTraffic.RTXTrafficListener {

    public static final int VPN_REQUEST_CODE = 101;

    @Inject
    Navigator navigator;

    @Inject
    VPNManager vpnManager;

    @Inject
    RTXTraffic rtxTraffic;

    @Bind(R.id.btnStart)
    FloatingActionButton btnStart;

    @Bind(R.id.idTextView)
    TextView idTextView;

    @Bind(R.id.networkReleaseText)
    TextView networkReleaseText;

    @Bind(R.id.consumoTextView)
    TextView consumoTextView;

    @Bind(R.id.imgCognito)
    ImageView logo;

    @Bind(R.id.switchM)
    Switch switchM;

    @Bind(R.id.switchC)
    Switch switchC;

    @Bind(R.id.switchCompress)
    Switch switchCompress;

    @Bind(R.id.imgConsumo)
    ImageView imgConsumo;

    private AnimationImageUtil animationImageUtil;

    private SharedPreferences sharedPref;

    public static MainActivity myMainActivity;

    protected DataUpdateReceiver dataUpdateReceiver;

    private MenuItem menuItemDatosUsuario;

    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(as3fService.REFRESH_STATUS_INTENT)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.updateStatus(as3fService.current_status);
                    }
                });
            }
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        myMainActivity = this;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        animationImageUtil = new AnimationImageUtil(this, btnStart, 200,
                R.drawable.load1,
                R.drawable.load2,
                R.drawable.load3,
                R.drawable.load4);
        this.initTask();
    }

    private void onPrepareFiles(){
        if (as3fService.toState == Util.STATUS_DISCONNECT) {
            as3fService.toState = Util.STATUS_INIT; // Init
            Intent intent = new Intent(this, as3fService.class);
            this.startService(intent);
        }

        this.vpnManager.setVpnListener(this);
        this.rtxTraffic.setRtxTrafficListener(this);
        this.loadEvent();
    }

    private void loadEvent() {
        idTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(idTextView.getText(), idTextView.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Serial Client copied to clipboard", Toast.LENGTH_LONG).show();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        switchC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("server_port", isChecked?"80":"443");
                editor.commit();

                switchM.setChecked(!isChecked);
                btnStart.setEnabled(true);
            }
        });

        switchM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("server_port", isChecked?"443":"80");
                editor.commit();
                switchC.setChecked(!isChecked);
                btnStart.setEnabled(true);
            }
        });

        switchCompress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("compress_switch", isChecked);
                editor.commit();
            }
        });

        switchC.setChecked(sharedPref.getString("server_port", "").equals("80"));
        switchM.setChecked(sharedPref.getString("server_port", "").equals("443"));
        switchCompress.setChecked(sharedPref.getBoolean("compress_switch", false));

        if(as3fService.current_status == Util.STATUS_DISCONNECT || as3fService.current_status == Util.STATUS_SOCKS){
            btnStart.setEnabled(switchC.isChecked() || switchM.isChecked());
        }
    }

    private void connect() {
        vpnManager.connect(networkReleaseText.getText().toString());
    }

    private void initTask() {
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!((new File(Util.BASE + Util.BASE_BIN)).exists())) {
                            Util.CopyAssets(getApplicationContext().getAssets(), getPackageName());
                        }

                        if (!sharedPref.getBoolean("uuid_init", false)) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("uuid_init", true);
                            //editor.putString("server_port", BuildConfig.DEFAULT_PORT);
                            editor.putString("user_text", BuildConfig.TEST_MODE ? BuildConfig.TEST_USER : vpnManager.nextSerial());
                            editor.putString("password_text", BuildConfig.TEST_MODE ? BuildConfig.TEST_PASSW : BuildConfig.PASSW);
                            editor.commit();
                        }

                        renderPreferences();
                        updateStatus(as3fService.current_status);
                        onPrepareFiles();
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 0);
    }

    private void renderPreferences() {
        this.idTextView.setText(sharedPref.getString("user_text", "null"));
        this.networkReleaseText.setText(sharedPref.getString("server_serialip", BuildConfig.TEST_MODE ? BuildConfig.TEST_NETWORK : ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        //menuItemDatosUsuario = menu.findItem(R.id.datos_usuario);
        //this.menuItemDatosUsuario.setVisible(sharedPref.getBoolean("user_auth", false));
        return true;
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the current status
        updateStatus(as3fService.current_status);

        // Re-register to Service Updates
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(as3fService.REFRESH_STATUS_INTENT);
        registerReceiver(dataUpdateReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(rtxTraffic != null){
            rtxTraffic.stop();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_facebook:
                this.navigator.showFaceBookActivity();
                break;
           /* case R.id.datos_usuario:
                this.navigator.showDatosUsuario();
                break;
            case R.id.log:
                this.navigator.showLogActivity();
                break;*/
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case VPN_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    this.startVPNService();
                }
                break;
        }
    }

    @Override
    public void startVPNService() {
        Intent intent = new Intent(myMainActivity, as3fService.class);
        if (as3fService.current_status == Util.STATUS_DISCONNECT) {
            MyLog.i(Util.TAG, "Checking Network Status");
            as3fService.current_status = Util.STATUS_CONNECTING;
            updateStatus(as3fService.current_status);
            as3fService.toState = Util.STATUS_SOCKS;
        } else if (as3fService.current_status == Util.STATUS_CONNECTING || as3fService.current_status == Util.STATUS_SOCKS) {
            as3fService.toState = Util.STATUS_DISCONNECT;
        }
        this.startService(intent);
    }

    @Override
    public boolean requiredPermission() {
        Intent intentVpn = VpnService.prepare(this);
        if (intentVpn != null) {
            this.startActivityForResult(intentVpn, VPN_REQUEST_CODE);
            return true;
        }
        return false;
    }

    public static void updateStatus(int status) {
        switch (status) {
            case Util.STATUS_DISCONNECT:
                onDisconnect();
                break;
            case Util.STATUS_INIT:
                onInitializing();
                break;
            case Util.STATUS_CONNECTING:
                onConnecting();
                break;
            case Util.STATUS_SOCKS:
                onSocks();
                break;
        }
    }

    private static void visibleConsumo(boolean visible){
        myMainActivity.getConsumoTextView().setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        myMainActivity.getImgConsumo().setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private static void enableSwicth(boolean enable){
        myMainActivity.getSwitchC().setEnabled(enable);
        myMainActivity.getSwitchM().setEnabled(enable);
        myMainActivity.getSwitchCompress().setEnabled(enable);
    }

    private static void onDisconnect() {
        if (myMainActivity != null) {
            //myMainActivity.getConsumoTextView().setText(R.string.text_status_disconnected);
            //myMainActivity.getBtnStart().setText("START SESSION");
            myMainActivity.getAnimationImageUtil().stop();
            myMainActivity.getBtnStart().setImageResource(R.drawable.disconnected);
            myMainActivity.getBtnStart().setEnabled(true);
            myMainActivity.getNetworkReleaseText().setEnabled(true);
            enableSwicth(true);
            visibleConsumo(false);
            myMainActivity.getRtxTraffic().stop();
        }
    }

    private static void onInitializing() {
        if (myMainActivity != null) {
            //myMainActivity.getConsumoTextView().setText(R.string.text_status_initializing);
            //myMainActivity.getBtnStart().setText("START SESSION");
            myMainActivity.getAnimationImageUtil().start();
            //myMainActivity.getBtnStart().setImageResource(R.drawable.cognito);
            myMainActivity.getBtnStart().setEnabled(false);
            myMainActivity.getNetworkReleaseText().setEnabled(false);
            enableSwicth(false);
            visibleConsumo(false);
        }
    }

    private static void onConnecting() {
        if (myMainActivity != null) {
            //myMainActivity.getConsumoTextView().setText(R.string.text_status_connecting);
            //myMainActivity.getBtnStart().setText("STARTING...");
            myMainActivity.getAnimationImageUtil().start();
            myMainActivity.getBtnStart().setEnabled(false);
            myMainActivity.getNetworkReleaseText().setEnabled(false);
            enableSwicth(false);
            visibleConsumo(false);
        }
    }

    private static void onSocks(){
        if (myMainActivity != null) {
            //myMainActivity.getConsumoTextView().setText(R.string.text_status_connected);
            //myMainActivity.getBtnStart().setText("CLOSE SESSION");
            myMainActivity.getAnimationImageUtil().stop();
            myMainActivity.getBtnStart().setImageResource(R.drawable.connected);
            myMainActivity.getBtnStart().setEnabled(true);
            myMainActivity.getNetworkReleaseText().setEnabled(false);
            enableSwicth(false);
            visibleConsumo(true);
            myMainActivity.getRtxTraffic().start(myMainActivity);

            /*if(myMainActivity.getMenuItemDatosUsuario() != null && !myMainActivity.getMenuItemDatosUsuario().isVisible()){
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(myMainActivity);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("user_auth", true);
                editor.commit();

                myMainActivity.getMenuItemDatosUsuario().setVisible(true);
            }*/
        }
    }

    @Override
    public void trafficReport(long traffic) {
        consumoTextView.setText(this.rtxTraffic.format(traffic));
    }

    public FloatingActionButton getBtnStart() {
        return btnStart;
    }

    public TextView getNetworkReleaseText() {
        return networkReleaseText;
    }

    public TextView getConsumoTextView() {
        return consumoTextView;
    }

    public MenuItem getMenuItemDatosUsuario() {
        return menuItemDatosUsuario;
    }

    public Switch getSwitchCompress() {
        return switchCompress;
    }

    public Switch getSwitchM() {
        return switchM;
    }

    public Switch getSwitchC() {
        return switchC;
    }

    public ImageView getImgConsumo() {
        return imgConsumo;
    }

    public AnimationImageUtil getAnimationImageUtil() {
        return animationImageUtil;
    }

    public RTXTraffic getRtxTraffic() {
        return rtxTraffic;
    }
}
