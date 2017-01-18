package com.asdf123.as3f.ui.activity.di;

import com.asdf123.as3f.ui.activity.ProfileActivity;
import com.asdf123.as3f.ui.activity.LogActivity;
import com.asdf123.as3f.ui.activity.MainActivity;

import dagger.Module;

/**
 * Created by l2 on 7/01/17.
 */
@Module(complete = false,
        injects = {
                MainActivity.class,
                ProfileActivity.class,
                LogActivity.class
        })
public class ModuleActivity {
}
