package com.bezkoder.spring.thymeleaf.tool;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class SerialPortListener implements SerialPortDataListener {

    private final DataAvailableListener mDataAvailableListener;

    public SerialPortListener(DataAvailableListener mDataAvailableListener) {
        this.mDataAvailableListener = mDataAvailableListener;
    }

    // //必须是return这个才会开启串口工具的监听
    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    /**
     * 系列活动
     *
     * @param serialPortEvent 串口事件
     */
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (mDataAvailableListener != null) {
            mDataAvailableListener.dataAvailable();
        }
    }
}
