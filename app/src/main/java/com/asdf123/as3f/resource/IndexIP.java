package com.asdf123.as3f.resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by l2 on 7/01/17.
 */

public class IndexIP {

    public static final Map<String, String> indexMap = new HashMap<>();

    static {
        indexMap.put("cb3a211733208b87a25030d21789ffee","192.157.238.5");
        indexMap.put("a66858290252e98c45d1daa51800b4d8","23.88.103.98");
        indexMap.put("73ced95c95ebf947ed048ba4d793e784","23.88.107.100");
        indexMap.put("fa88106f06539feaba2215082217f949","23.252.110.216");
        indexMap.put("136f86e16b7c60c618abad194769b814", "23.252.107.213");
        indexMap.put("bf6a8cf9f66484e20ac301e2a0495fd4", "23.252.111.228");
    }

    public static boolean hasIP(String networkKey){
        return indexMap.containsKey(networkKey);
    }

    public static String getIP(String networkKey){
        return indexMap.get(networkKey);
    }
}
