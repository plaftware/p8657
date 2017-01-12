package com.asdf123.as3f.di;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module(library = true) public final class ActivityModule {

  private final Activity activity;

  public ActivityModule(Activity activity) {
    this.activity = activity;
  }

  @ActivityContext @Provides
  Context provideActivityContext() {
    return activity;
  }
}
