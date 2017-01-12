package com.asdf123.as3f.ui.activity.nav;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.asdf123.as3f.di.ActivityContext;
import com.asdf123.as3f.ui.activity.DatosUsuarioActivity;
import com.asdf123.as3f.ui.activity.LogActivity;

import javax.inject.Inject;

/**
 * Created by l2 on 7/01/17.
 */

public class Navigator {

    private final Context activityContext;

    @Inject
    public Navigator(@ActivityContext Context activityContext) {
        this.activityContext = activityContext;
    }

    public void showFaceBookActivity(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/Anonymous-VPN-1638009543129333/"));
        this.activityContext.startActivity(browserIntent);
    }

    public void showDatosUsuario(){
        this.showActivity(DatosUsuarioActivity.class);
    }

    public void showLogActivity(){
        this.showActivity(LogActivity.class);
    }

    private void showActivity(Class aClass){
        Intent intent = new Intent(activityContext, aClass);
        this.activityContext.startActivity(intent);
    }

}
