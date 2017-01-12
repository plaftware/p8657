package com.asdf123.as3f;

import android.app.Application;

import com.asdf123.as3f.di.RootModule;

import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by l2 on 7/01/17.
 */

public class AnonymousVpnApplication extends Application{

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        this.initializeDependencyInjector();
    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }

    public ObjectGraph plus(List<Object> modules) {
        if (modules == null) {
            throw new IllegalArgumentException(
                    "You can't plus a null module, review your getModules() implementation");
        }
        return objectGraph.plus(modules.toArray());
    }

    private void initializeDependencyInjector() {
        objectGraph = ObjectGraph.create(new RootModule(getApplicationContext()));
        objectGraph.inject(this);
        objectGraph.injectStatics();
    }
}
