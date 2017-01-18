package com.asdf123.as3f.service.impl;

import android.os.AsyncTask;

import com.asdf123.as3f.resource.IndexIP;
import com.asdf123.as3f.service.UserService;
import com.asdf123.as3f.service.abs.AbstractServiceEndpoint;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by l2 on 17/01/17.
 */

public class UserServiceImpl extends AbstractServiceEndpoint implements UserService {

    private Callback callback;

    @Inject
    public UserServiceImpl(){

    }

    @Override
    public void callback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void findByUserAndNetworkKey(String userName, String networkKey) {
        AsyncTask<String, Void, Map> asyncTask = new AsyncTask<String, Void, Map>() {

            @Override
            protected Map doInBackground(String... params) {
                try {
                    String URL = String.format("http://%s:8580/getInfoSystem?sysKey=%s&nameUserServer=%s",
                            IndexIP.getIP(params[1]), params[1], params[0]);
                    ResponseEntity<HashMap> responseEntity = restTemplate.getForEntity(URL, HashMap.class);
                    return responseEntity.getBody();
                }catch (Exception e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Map result) {
               callback.onFindByUserAndNetworkKey(result);
            }
        };
        asyncTask.execute(userName, networkKey);
    }
}
