package com.asdf123.as3f.di;

import android.content.Context;

import com.asdf123.as3f.AnonymousVpnApplication;

import dagger.Module;
import dagger.Provides;


@Module(
    includes = {
        //ConsentirService.class,
    },
    injects = {
        AnonymousVpnApplication.class
    }, library = true)
public final class RootModule {

  private final Context context;

  public RootModule(Context context) {
    this.context = context;
  }

  @Provides
  Context provideApplicationContext() {
    return context;
  }
}