package utils;

import config.Config;
import gui.tabs.DefaultTab;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PIDTable {
    private final List<PIDOption> options;
    private float throttleMid;
    private float throttleExpo;
    private float rate;
    private float expo;

    public PIDTable(List<PIDOption> options, float throttleMid, float throttleExpo, float rate, float expo) {
        this.options = options;
        this.throttleMid = throttleMid;
        this.throttleExpo = throttleExpo;
        this.rate = rate;
        this.expo = expo;
    }

    public PIDTable() {
        this(new ArrayList<>(DefaultTab.PIDS.length), 0, 0, 0, 0);
        for (String pid : DefaultTab.PIDS) {
            this.options().add(new PIDOption(pid, 0, 0, 0));
        }
    }

    public static PIDTable fromConfig(Config config) {
        PIDTable table = new PIDTable();
        for (Config.Entry entry : config.entries().values()) {
            table.setOptionFromConfigEntry(entry);
        }
        return table;
    }

    public void setOptionFromConfigEntry(Config.Entry entry) {
        String key = entry.key();
        String[] split = key.split("\\.");
        if (split.length == 0) {
            System.err.println("Failed to set pid value from config entry, because of illegal key: " + key);
            return;
        }
        String type = split[0];
        if (type.equals("pid")) {
            int index = Integer.parseInt(split[1]);
            PIDOption option = this.options().get(index);
            String pid = split[2];
            switch (pid.charAt(0)) {
                case 'p' -> option.p = entry.value();
                case 'i' -> option.i = entry.value();
                case 'd' -> option.d = entry.value();
                default -> System.err.println("Invalid pid character: " + pid.charAt(0));
            }
        } else if (type.equals("rc")) {
            String category = split[1];
            switch (category) {
                case "throttle" -> {
                    switch (split[2]) {
                        case "mid" -> this.throttleMid = entry.value();
                        case "expo" -> this.throttleExpo = entry.value();
                        case "rate" -> {}
                        default -> throw new IllegalStateException("Unexpected value: " + split[2]);
                    }
                }
                case "expo" -> this.expo = entry.value();
                case "rate" -> this.rate = entry.value();
            }
        }
    }

    public List<PIDOption> options() {
        return options;
    }

    public float getThrottleMid() {
        return throttleMid;
    }

    public float getThrottleExpo() {
        return throttleExpo;
    }

    public float getRate() {
        return rate;
    }

    public float getExpo() {
        return expo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PIDTable) obj;
        return Objects.equals(this.options, that.options) &&
                Float.floatToIntBits(this.throttleMid) == Float.floatToIntBits(that.throttleMid) &&
                Float.floatToIntBits(this.throttleExpo) == Float.floatToIntBits(that.throttleExpo) &&
                Float.floatToIntBits(this.rate) == Float.floatToIntBits(that.rate) &&
                Float.floatToIntBits(this.expo) == Float.floatToIntBits(that.expo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(options, throttleMid, throttleExpo, rate, expo);
    }

    @Override
    public String toString() {
        return "PIDTable[" +
                "options=" + options + ", " +
                "throttleMid=" + throttleMid + ", " +
                "throttleExpo=" + throttleExpo + ", " +
                "rate=" + rate + ", " +
                "expo=" + expo + ']';
    }


    public static final class PIDOption {
        private final String key;
        private float p;
        private float i;
        private float d;

        PIDOption(String key, float p, float i, float d) {
            this.key = key;
            this.p = p;
            this.i = i;
            this.d = d;
        }

        public String getKey() {
            return key;
        }

        public float getP() {
            return p;
        }

        public float getI() {
            return i;
        }

        public float getD() {
            return d;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (PIDOption) obj;
            return Objects.equals(this.key, that.key) &&
                    Float.floatToIntBits(this.p) == Float.floatToIntBits(that.p) &&
                    Float.floatToIntBits(this.i) == Float.floatToIntBits(that.i) &&
                    Float.floatToIntBits(this.d) == Float.floatToIntBits(that.d);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, p, i, d);
        }

        @Override
        public String toString() {
            return "PIDOption[" +
                    "key=" + key + ", " +
                    "p=" + p + ", " +
                    "i=" + i + ", " +
                    "d=" + d + ']';
        }

    }
}
