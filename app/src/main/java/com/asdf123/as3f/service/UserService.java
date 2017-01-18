package com.asdf123.as3f.service;

import java.util.Map;

/**
 * Created by l2 on 17/01/17.
 */

public interface UserService {

    interface Callback{

        void onFindByUserAndNetworkKey(Map<String, Object> result);

    }

    /**
     * Este metodo nos permite registrar el callback
     * */
    void callback(Callback callback);

    /**
     * Este metodo me permite consultar todos los catalogos disponibles
     * */
    void findByUserAndNetworkKey(String userName, String networkKey);

}
