package ch.confinale.race.control.results;

public class TrackStatusResult implements TimeReadResult {
    private final int gas1;
    private final int gas2;
    private final int gas3;
    private final int gas4;
    private final int gas5;
    private final int gas6;
    private final int startLamp;
    private final int positionMode;
    private final int pitLaneBitMask;
    private final int gasMode;

    public TrackStatusResult(byte[] command) {
        assert command[0]=='?';
        assert command[1] == ':';

        gas1 = command[2] & 0x0F;
        gas2 = command[3] & 0x0F;
        gas3 = command[4] & 0x0F;
        gas4 = command[5] & 0x0F;
        gas5 = command[6] & 0x0F;
        gas6 = command[7] & 0x0F;

        startLamp = command[10] & 0x0F;
        gasMode = command[11] & 0x0F;

        pitLaneBitMask = ((command[12] & 0x0F)<<1) + (command[13] & 0x0F);
        positionMode = command[14] & 0x0F;
    }

    public int getGas1() {
        return gas1;
    }

    public int getGas2() {
        return gas2;
    }

    public int getGas3() {
        return gas3;
    }

    public int getGas4() {
        return gas4;
    }

    public int getGas5() {
        return gas5;
    }

    public int getGas6() {
        return gas6;
    }

    public int getStartLamp() {
        return startLamp;
    }

    public int getPositionMode() {
        return positionMode;
    }

    public int getPitLaneBitMask() {
        return pitLaneBitMask;
    }

    public int getGasMode() {
        return gasMode;
    }

    @Override
    public String toString() {
        return "TrackStatusResult{" +
                "gas1=" + gas1 +
                ", gas2=" + gas2 +
                ", gas3=" + gas3 +
                ", gas4=" + gas4 +
                ", gas5=" + gas5 +
                ", gas6=" + gas6 +
                ", startLamp=" + startLamp +
                ", positionMode=" + positionMode +
                ", pitLaneBitMask=" + pitLaneBitMask +
                ", gasMode=" + gasMode +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackStatusResult that = (TrackStatusResult) o;

        if (gas1 != that.gas1) return false;
        if (gas2 != that.gas2) return false;
        if (gas3 != that.gas3) return false;
        if (gas4 != that.gas4) return false;
        if (gas5 != that.gas5) return false;
        if (gas6 != that.gas6) return false;
        if (gasMode != that.gasMode) return false;
        if (pitLaneBitMask != that.pitLaneBitMask) return false;
        if (positionMode != that.positionMode) return false;
        if (startLamp != that.startLamp) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = gas1;
        result = 31 * result + gas2;
        result = 31 * result + gas3;
        result = 31 * result + gas4;
        result = 31 * result + gas5;
        result = 31 * result + gas6;
        result = 31 * result + startLamp;
        result = 31 * result + positionMode;
        result = 31 * result + pitLaneBitMask;
        result = 31 * result + gasMode;
        return result;
    }
}
