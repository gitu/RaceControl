package ch.confinale.race.control.results;

import java.time.LocalDateTime;
import java.util.Arrays;

public class NewTimeResult implements TimeReadResult {
    private final int carNr;
    private final long time;
    private final int group;
    private final LocalDateTime createTime;

    public NewTimeResult(byte[] command) {
        assert command[0]=='?';
        assert command[1]!=':';

        this.carNr = command[1]-48;
        this.group = command[10]-48;

        long result = 0;
        byte[] byteTime = Arrays.copyOfRange(command, 2, 10);

        result += (byteTime[1] & 0xF) << 28;
        result += (byteTime[0] & 0xF) << 24;
        result += (byteTime[3] & 0xF) << 20;
        result += (byteTime[2] & 0xF) << 16;
        result += (byteTime[5] & 0xF) << 12;
        result += (byteTime[4] & 0xF) << 8;
        result += (byteTime[7] & 0xF) << 4;
        result += (byteTime[6] & 0xF);

        time = result;

        createTime = LocalDateTime.now();
    }

    public static int swap(byte b) {
        return ((b & 0x01) << 3) + ((b & 0x02) << 2) + ((b & 0x04) >> 1) + ((b & 0x08) >> 2);
    }


    public int getCarNr() {
        return carNr;
    }

    public long getTime() {
        return time;
    }

    public int getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "NewTimeResult{" +
                "carNr=" + carNr +
                ", time='" + time + '\'' +
                ", group=" + group +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewTimeResult that = (NewTimeResult) o;

        if (carNr != that.carNr) return false;
        if (group != that.group) return false;
        if (time != that.time) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = carNr;
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + group;
        return result;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
}
