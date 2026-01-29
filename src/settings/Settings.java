package settings;

import config.Config;

public class Settings {
    public static final Integer[] BAUD_RATES = new Integer[]{
            9600, 14400, 19200, 28800, 38400, 57600, 115200
    };
    private int baudRate;
    private String[] serialCommPorts;
    private Config config;

    public void init() {
        this.baudRate = BAUD_RATES[0];
        this.serialCommPorts = new String[0];
        this.config = Config.EMPTY;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public int getBaudRate() {
        return baudRate;
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
