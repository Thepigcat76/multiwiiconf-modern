package utils;

import com.fazecast.jSerialComm.SerialPort;
import settings.Settings;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class SerialPortListener {
    public static String[] refreshPorts(Settings settings) {
        String[] ports1 = Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).toArray(String[]::new);
        if (!Arrays.equals(ports1, settings.getSerialCommPorts())) {
            return ports1;
        }
        return settings.getSerialCommPorts();
    }
}
