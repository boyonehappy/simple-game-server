package org.kelvmiao.sgs.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by kelvin on 2/12/14.
 */
public class Config {
    Properties properties;
    private static Config instance;
    private static Config getInstance(){
        if(instance == null)
            instance = new Config();
        return instance;
    }
    private Config(){
        try {
            properties = new Properties();
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key){
        return getInstance().properties.getProperty(key);
    }
}
