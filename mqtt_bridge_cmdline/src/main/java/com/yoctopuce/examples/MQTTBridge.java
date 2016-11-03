package com.yoctopuce.examples;

import com.yoctopuce.YoctoAPI.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

public class MQTTBridge implements Runnable, MqttCallback, YTemperature.UpdateCallback
{

    private YAPIContext _yctx;
    private String _broker_url;
    private MqttClient _sampleClient;
    private YRelay _yrelay;
    private ArrayList<YColorLed> _yleds = new ArrayList<>();


    // Constructor used for traditional application
    public MQTTBridge(String hub_url, String broker_url) throws YAPI_Exception
    {
        _yctx = new YAPIContext();
        _yctx.RegisterHub(hub_url);
        _broker_url = broker_url;
    }

    public void run()
    {

        try {
            MemoryPersistence persistence = new MemoryPersistence();
            _sampleClient = new MqttClient(_broker_url, "MQTTBridge", persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            _sampleClient.connect(connOpts);
            System.out.println("Connected to broker: " + _broker_url);

            // subscribe to events form MQTT
            _sampleClient.setCallback(this);
            // subscribe to events form MQTT
            _sampleClient.subscribe("fan_control");
            _sampleClient.subscribe("leds_control");

            _yctx.UpdateDeviceList();
            _yrelay = YRelay.FirstRelayInContext(_yctx);
            if (_yrelay == null) {
                System.out.println("No relay");
                return;
            }
            YColorLed colorLed = YColorLed.FirstColorLedInContext(_yctx);
            while (colorLed != null) {
                _yleds.add(colorLed);
                colorLed = colorLed.nextColorLed();
            }
            YTemperature temperature = YTemperature.FirstTemperatureInContext(_yctx);
            if (temperature == null) {
                System.out.println("No temperature sensor");
                return;
            }
            temperature.registerValueCallback(this);
            while (true) {
                _yctx.Sleep(1000);
            }
        } catch (YAPI_Exception ex) {
            ex.printStackTrace();
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }

        try {
            _sampleClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        YAPI.FreeAPI();

    }


    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage)
    {
        byte[] payload = mqttMessage.getPayload();
        String message = new String(payload);
        try {
            switch (topic) {
                case "fan_control":
                    int output;
                    if ("on".equalsIgnoreCase(message)) {
                        output = YRelay.OUTPUT_ON;
                    } else {
                        output = YRelay.OUTPUT_OFF;
                    }
                    _yrelay.set_output(output);
                    break;
                case "leds_control":
                    int color = Integer.parseInt(message.replaceFirst("#", ""), 16);
                    for (YColorLed colorLed : _yleds) {
                        colorLed.set_rgbColor(color);
                    }
                    break;
            }
        } catch (YAPI_Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void connectionLost(Throwable throwable)
    {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {

    }

    @Override
    public void yNewValue(YTemperature temperature, String currentValue)
    {
        MqttMessage message = new MqttMessage(currentValue.getBytes());
        try {
            _sampleClient.publish("temperature_test", message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
