package com.yoctopuce.examples;

import com.yoctopuce.YoctoAPI.YAPI_Exception;

/**
 * Hello world!
 */
public class App
{
    public static void main(String[] args)
    {
        MQTTBridge mqttBridge = null;
        try {
            mqttBridge = new MQTTBridge("172.17.17.104", "tcp://192.168.1.82:1883");
            mqttBridge.run();
        } catch (YAPI_Exception e) {
            e.printStackTrace();
        }
    }
}
