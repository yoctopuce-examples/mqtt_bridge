package com.yoctopuce.examples;

import com.yoctopuce.YoctoAPI.YAPI_Exception;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/mqtt_bridge")
public class MqttWebsocketEndpoint
{


    @OnOpen
    public void onOpen(Session session)
    {
        try {
            MQTTBridge bridge = new MQTTBridge(session, "", "", "tcp://www.example.com:1883");
            Thread thread = new Thread(bridge);
            thread.start();
        } catch (YAPI_Exception e) {
            e.printStackTrace();
        }
    }

}
