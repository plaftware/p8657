package com.asdf123.as3f.di;

import com.asdf123.as3f.service.UserService;
import com.asdf123.as3f.service.impl.UserServiceImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by l2 on 17/01/17.
 */

@Module(library = true, complete = false)
public final class AnonymousService {

    @Singleton
    @Provides
    UserService provideUserService(UserServiceImpl userService){
        return userService;
    }

}
