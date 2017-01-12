package com.asdf123.as3f.ui.activity.abs;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.asdf123.as3f.AnonymousVpnApplication;
import com.asdf123.as3f.di.ActivityModule;
import com.asdf123.as3f.ui.activity.di.ModuleActivity;

import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import dagger.ObjectGraph;

/**
 * Created by l2 on 7/01/17.
 */

public abstract class AbstractActivity extends AppCompatActivity {

    private ObjectGraph activityScopeGraph;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(this.getLayoutResource());
        this.injectDependencies();
        this.injectViews();
    }

    private void injectDependencies() {
        AnonymousVpnApplication consentirApplication = (AnonymousVpnApplication) getApplication();
        //List<Object> activityScopeModules = getModules();
        List<Object> activityScopeModules = new LinkedList<>();
        activityScopeModules.add(new ModuleActivity());
        activityScopeModules.add(new ActivityModule(this));
        activityScopeGraph = consentirApplication.plus(activityScopeModules);
        inject(this);
    }

    protected abstract @LayoutRes
    int getLayoutResource();

    private void injectViews() {
        ButterKnife.bind(this);
    }

    public void inject(Object object) {
        activityScopeGraph.inject(object);
    }


}
