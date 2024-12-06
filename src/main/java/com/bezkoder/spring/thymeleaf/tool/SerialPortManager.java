package com.bezkoder.spring.thymeleaf.tool;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortManager {
    public static SerialPort openPort(String portName, Integer baudRate) {
        SerialPort serialPort = SerialPort.getCommPort(portName);
        if (baudRate != null) {
            serialPort.setBaudRate(baudRate);
        }
        //开启串口
        if (!serialPort.isOpen()) {
            serialPort.openPort(1000);
        } else {
            return serialPort;
        }
        // 设置一下串口的波特率等参数
        // 数据位：8
        // 停止位：1
        // 校验位：None
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        if (null != baudRate) {
            serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        }
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 1000, 1000);
        return serialPort;
    }

    public static void closePort(SerialPort serialPort) {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }

    public static void addListener(SerialPort serialPort, DataAvailableListener listener) {
        try {
            // 给串口添加监听器
            serialPort.addDataListener(new SerialPortListener(listener));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendToPort(SerialPort serialPort, byte[] content) {
        if (!serialPort.isOpen()) {
            return;
        }
        serialPort.writeBytes(content, content.length);
    }

    public static byte[] readFromPort(SerialPort serialPort) {
        byte[] reslutData = null;
        try {
            if (!serialPort.isOpen()) {
                return null;
            }
            int i = 0;
            while (serialPort.bytesAvailable() > 0 && i++ < 5) Thread.sleep(100);
            byte[] readBuffer = new byte[serialPort.bytesAvailable()];
            int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
            if (numRead > 0) {
                reslutData = readBuffer;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return reslutData;
    }
}
