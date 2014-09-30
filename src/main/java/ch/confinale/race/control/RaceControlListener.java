package ch.confinale.race.control;

import ch.confinale.race.control.results.NewTimeResult;
import ch.confinale.race.control.results.TrackStatusResult;
import com.firebase.client.Firebase;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.*;

public class RaceControlListener implements Runnable{
    private final ControlUnit controlUnit;
    private final Firebase firebase;

    public RaceControlListener(ControlUnit controlUnit, Firebase firebase) {
        this.controlUnit = controlUnit;
        this.firebase = firebase;
    }

    @Override
    public void run() {

    }
}
