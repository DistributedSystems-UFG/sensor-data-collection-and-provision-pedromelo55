package com.example.samplesensorproviderapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TemperatureSensorAccess implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor mTemperature;
    private TextView textViewTemperature;

    // Adicione variáveis relacionadas ao MQTT
    private Mqtt5BlockingClient mqttClient;
    private String mqttBrokerURI = "34.234.31.226"; // Substitua pelo URI do seu broker

    public TemperatureSensorAccess(SensorManager sm, TextView tv) {
        sensorManager = sm;
        textViewTemperature = tv;

        // Configurar o cliente MQTT (ajuste conforme necessário)
        mqttClient = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(mqttBrokerURI)
                .buildBlocking();

        // Obter o sensor de temperatura
        mTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        // Registrar ouvinte do sensor de temperatura
        if (mTemperature != null) {
            sensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // Trate o caso em que o sensor de temperatura não está disponível no dispositivo.
            textViewTemperature.setText("Temperature sensor not available on this device");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Não é necessário neste exemplo
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Atualizar o campo de texto com o valor do sensor
        float temperatureValue = event.values[0];
        textViewTemperature.setText(temperatureValue + " °C");

        publishToMQTT("sensor_temperatura", String.valueOf(temperatureValue));
    }

    private void publishToMQTT(String topic, String message) {
        try {
            mqttClient.connect();
            mqttClient.publishWith()
                    .topic(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(message.getBytes(StandardCharsets.UTF_8))
                    .send();
            mqttClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterSensorListener() {
        sensorManager.unregisterListener(this);
    }
}