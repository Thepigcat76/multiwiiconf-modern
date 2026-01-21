package settings;

import config.Config;

public class Settings {
    public static final Integer[] BAUD_RATES = new Integer[]{
            9600, 14400, 19200, 28800, 38400, 57600, 115200
    };
    private int serialPort;
    private String[] serialCommPorts;
    private Config config;

    public void init() {
        this.serialPort = BAUD_RATES[0];
        this.serialCommPorts = new String[0];
        this.config = Config.EMPTY;
    }

    public void setSerialPort(int serialPort) {
        this.serialPort = serialPort;
    }

    public int getSerialPort() {
        return serialPort;
    }

    public void setSerialCommPorts(String[] serialCommPorts) {
        this.serialCommPorts = serialCommPorts;
    }

    public String[] getSerialCommPorts() {
        return serialCommPorts;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }
}
