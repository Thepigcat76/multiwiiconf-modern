package utils;

import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Implementation of the MSP (Multiwii Serial Protocol)
 */
public class Protocol {
    private static final String MSP_HEADER = "$M<";

    private State state;
    private int offset;

    public Protocol() {
        this.state = State.IDLE;
    }

    /*
    [0]  '$'          — Start character (0x24)
    [1]  'M'          — Protocol marker
    [2]  '<' or '>'   — Direction
    [3]  Size         — Payload length (0–255)
    [4]  Command      — MSP command ID
    [5..n] Payload    — Optional data (binary)
    [last] Checksum   — XOR of Size, Command, and all payload bytes
     */
    public List<Byte> encodeRequest(Command command, byte[] payload) {
        List<Byte> bytebuf = new ArrayList<>();
        for (byte b : MSP_HEADER.getBytes()) {
            bytebuf.add(b);
        }

        byte payload_size = payload != null ? (byte) payload.length : 0;
        bytebuf.add(payload_size);
        bytebuf.add(command.getId());

        byte checksum = 0;
        checksum ^= payload_size;
        checksum ^= command.getId();

        if (payload != null) {
            for (byte b : payload) {
                bytebuf.add(b);
                checksum ^= b;
            }
        }

        bytebuf.add(checksum);
        return bytebuf;
    }

    public void sendRequest() {

    }

    public Data readResponse(SerialPort comPort) {
        Data data = new Data();
        outer:
        while (comPort.bytesAvailable() > 0) {
            byte[] bytes = new byte[1024];
            int size = comPort.readBytes(bytes, bytes.length);
            for (int i = 0; i < size; i++) {
                if (this.decodeResponseByte(data, bytes[i])) {
                    break outer;
                }
            }
        }
        return data;
    }

    protected boolean decodeResponseByte(Data data, byte b) {
        switch (this.state) {
            case IDLE -> this.check(b, '$', State.HEADER_START);
            case HEADER_START -> this.check(b, 'M', State.HEADER_M);
            case HEADER_M -> this.check(b, b0 -> switch (b0) {
                case '>' -> State.HEADER_ARROW;
                case '!' -> State.HEADER_ERROR;
                default -> State.IDLE;
            });
            case HEADER_ARROW, HEADER_ERROR -> {
                data.error = this.state == State.HEADER_ERROR;
                data.size = (byte) (b & 0xFF);
                data.checksum ^= (byte) (b & 0xFF);
                this.state = State.HEADER_SIZE;
            }
            case HEADER_SIZE -> {
                data.command = Command.byId((byte) (b & 0xFF));
                data.checksum ^= (byte) (b & 0xFF);
                this.state = State.HEADER_CMD;
            }
            case HEADER_CMD -> {
                if (this.offset < data.size) {
                    data.checksum ^= (byte) (b & 0xFF);
                    data.payload[this.offset++] = (byte) (b & 0xFF);
                } else {
                    // Process data
                    if ((data.checksum & 0xFF) == (b & 0xFF)) {
                        if (data.error) {
                            System.err.println("Copter did not understand request type, " + b);
                        } else {
                            this.handleResponse(data);
                            this.offset = 0;
                            this.state = State.IDLE;
                            return true;
                        }
                    } else {
                        System.out.println("invalid checksum for command " + ((int) (data.command.getId() & 0xFF)) + ": " + (data.checksum & 0xFF) + " expected, got " + (int) (b & 0xFF));
                        System.out.print("<" + (data.command.getId() & 0xFF) + " " + (data.size & 0xFF) + "> {");
                        for (int i = 0; i < data.size; i++) {
                            if (i != 0) {
                                System.err.print(' ');
                            }
                            System.out.print((data.payload[i] & 0xFF));
                        }
                        System.out.println("} [" + b + "]");
                        System.out.println(new String(data.payload, 0, data.size));
                    }
                }
                this.offset = 0;
                this.state = State.IDLE;
            }
        }
        return false;
    }

    private void check(byte b, Function<Byte, State> newStateFunction) {
        this.state = newStateFunction.apply(b);
    }

    private void check(byte b, char c, State newState) {
        if (b == c) {
            this.state = newState;
        } else {
            this.state = State.IDLE;
        }
    }

    public void handleResponse(Data data) {

    }

    public static class Data {
        private Command command;
        private boolean error;
        private byte size;
        private byte checksum;
        private byte[] payload;

        public Data() {
            this.payload = new byte[256];
        }
    }

    public enum Command {
        IDENT(100),
        STATUS(101),
        RAW_IMU(102),
        SERVO(103),
        MOTOR(104),
        RC(105),
        RAW_GPS(106),
        COMP_GPS(107),
        ATTITUDE(108),
        ALTITUDE(109),
        ANALOG(110),
        RC_TUNING(111),
        PID(112),
        BOX(113),
        MISC(114),
        MOTOR_PINS(115),
        BOXNAMES(116),
        PIDNAMES(117),
        SERVO_CONF(120),
        SET_RAW_RC(200),
        SET_RAW_GPS(201),
        SET_PID(202),
        SET_BOX(203),
        SET_RC_TUNING(204),
        ACC_CALIBRATION(205),
        MAG_CALIBRATION(206),
        SET_MISC(207),
        RESET_CONF(208),
        SELECT_SETTING(210),
        SET_HEAD(211),
        SET_SERVO_CONF(212),
        SET_MOTOR(214),
        BIND(240),
        EEPROM_WRITE(250),
        DEBUG_MSG(253),
        DEBUG(254);

        private final byte id;

        Command(int id) {
            this((byte) id);
        }

        Command(byte id) {
            this.id = id;
        }

        public byte getId() {
            return id;
        }

        public static Command byId(byte id) {
            for (Command value : Command.values()) {
                if (value.getId() == id) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Invalid id " + id + ", does not have a command");
        }

    }

    public enum State {
        IDLE,
        HEADER_START,
        HEADER_M,
        HEADER_ARROW,
        HEADER_SIZE,
        HEADER_CMD,
        HEADER_ERROR;

        private final byte id;

        State() {
            this.id = (byte) this.ordinal();
        }

        public byte getId() {
            return id;
        }
    }

}
