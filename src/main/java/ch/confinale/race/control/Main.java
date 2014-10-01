package ch.confinale.race.control;

import ch.confinale.race.control.results.NewTimeResult;
import ch.confinale.race.control.results.TrackStatusResult;
import ch.confinale.race.control.utils.RXTXLoader;
import com.firebase.client.Firebase;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, TooManyListenersException, PortInUseException, UnsupportedCommOperationException, NoSuchPortException {
        if( args.length != 1 ) {
            System.out.println( "Usage: <file.yml>" );
            return;
        }

        Yaml yaml = new Yaml();
        Configuration config;
        try( InputStream in = Files.newInputStream(Paths.get(args[0])) ) {
            config = yaml.loadAs( in, Configuration.class );
            System.out.println( config.toString() );
        }

        RXTXLoader.load();

        start(config);
    }

    public static void start(Configuration config) throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, TooManyListenersException, IOException, InterruptedException {

        String raceId = UUID.randomUUID().toString();
        Firebase firebase = new Firebase("https://" + config.getFirebaseName() + ".firebaseio.com/");
        firebase.child("lastRace").setValue(raceId);


        ControlUnit controlUnit = new ControlUnit(config.getComPort());
        controlUnit.initialize();

        System.out.println(new String(controlUnit.sendCommand("0")));
        Thread.sleep(500);
        controlUnit.sendCommand("=", (byte) '=');
        Thread.sleep(500);


        Map<Integer, List<NewTimeResult>> timeResults = new HashMap<>();
        long fastestRound = Long.MAX_VALUE;
        long[] fastestCarRound = new long[10];
        for (int i = 0; i <= 9; i++) {
            timeResults.put(i, new LinkedList<>());
            fastestCarRound[i] = Long.MAX_VALUE;
        }

        int count = 0;
        while(controlUnit.sendCommand("?")[1]!=':' || count++<=100) {
            Thread.sleep(30);
        }


        TrackStatusResult lastTrackStatusResult = null;
        while (true) {
            Thread.sleep(100);
            byte[] commandResult = controlUnit.sendCommand("?");

            if (commandResult[0] == '?' && commandResult[1] == ':') {
                TrackStatusResult trackStatusResult = new TrackStatusResult(commandResult);
                if (!trackStatusResult.equals(lastTrackStatusResult)) {
                    System.out.println(trackStatusResult);
                    lastTrackStatusResult = trackStatusResult;
                }

            } else {
                NewTimeResult newResult = new NewTimeResult(commandResult);
                List<NewTimeResult> newTimeResults = timeResults.get(newResult.getCarNr());
                NewTimeResult lastResult = null;
                if (newTimeResults.size()>0) {
                    lastResult = newTimeResults.get(newTimeResults.size()-1);
                }
                firebase.child("race").child(raceId)
                        .child("car").child(((Integer) newResult.getCarNr()).toString())
                        .child("raw")
                        .child("round").child(((Integer)(newTimeResults.size() - 1)).toString())
                        .setValue(newResult);
                if (!newResult.equals(lastResult)) {
                    newTimeResults.add(newResult);
                    System.out.println(newResult);
                    if (newTimeResults.size() >= 2) {
                        long roundTime = newResult.getTime() - newTimeResults.get(newTimeResults.size() - 2).getTime();
                        System.out.println("Time for car: " + newResult.getCarNr() + ", lap: " + (newTimeResults.size() - 1) + " - "
                                + ((double) roundTime) / 1000.0 + "s");

                        firebase.child("race").child(raceId)
                                .child("car").child(((Integer) newResult.getCarNr()).toString())
                                .child("round").child(((Integer)(newTimeResults.size() - 1)).toString())
                                .setValue(roundTime);

                        if (fastestRound>roundTime) {
                            fastestRound = roundTime;
                            firebase.child("race").child(raceId)
                                    .child("fastest")
                                    .setValue(roundTime);
                        }
                        if (fastestCarRound[newResult.getCarNr()]>roundTime) {
                            firebase.child("race").child(raceId)
                                    .child("car").child(((Integer) newResult.getCarNr()).toString())
                                    .child("fastest")
                                    .setValue(roundTime);
                        }
                    }
                }
            }
        }

    }
}
