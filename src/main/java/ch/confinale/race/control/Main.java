package ch.confinale.race.control;

import ch.confinale.race.control.results.NewTimeResult;
import ch.confinale.race.control.results.TrackStatusResult;
import ch.confinale.race.control.utils.RXTXLoader;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

        Firebase firebase = new Firebase("https://" + config.getFirebaseName() + ".firebaseio.com/");
        firebase.authWithPassword(config.getUsername(), config.getPassword(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                System.out.println(authData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                System.err.println(firebaseError);
                System.exit(1);
            }
        });

        ControlUnit controlUnit = new ControlUnit(config.getComPort());
        controlUnit.initialize();

        System.out.println(new String(controlUnit.sendCommand("0")));
        Thread.sleep(500);
        controlUnit.sendCommand("=", (byte) '=');
        Thread.sleep(500);

        int count = 0;
        while(controlUnit.sendCommand("?")[1]!=':' || count++<=100) {
            Thread.sleep(30);
        }

        while(true) {
            startRace(firebase, controlUnit);
        }
    }



    private static void startRace(Firebase firebase, ControlUnit controlUnit) throws IOException, InterruptedException {
        Map<Integer, List<NewTimeResult>> timeResults = new HashMap<>();
        for (int i = 0; i <= 9; i++) {
            timeResults.put(i, new LinkedList<>());
        }

        String raceId = UUID.randomUUID().toString();
        firebase.child("lastQualifying").setValue(raceId);
        Firebase fireRace = firebase.child("races").child(raceId);

        fireRace.child("time").child("qualifying").setValue(LocalDateTime.now().toString());
        fireRace.child("status").setValue("qualifying");
        boolean raceEnded = false;
        boolean qualifying = true;

        TrackStatusResult lastTrackStatusResult = null;
        while (!raceEnded) {
            Thread.sleep(20);
            byte[] commandResult = controlUnit.sendCommand("?");

            if (commandResult[0] == '?' && commandResult[1] == ':') {
                TrackStatusResult trackStatusResult = new TrackStatusResult(commandResult);
                if (!trackStatusResult.equals(lastTrackStatusResult)) {
                    System.out.println(trackStatusResult);
                    lastTrackStatusResult = trackStatusResult;
                    firebase.child("trackState").setValue(trackStatusResult);
                    System.out.println(LocalDateTime.now().toString());
                    fireRace.child("trackStates").child(LocalDateTime.now().toString().replace('.','-')).setValue(trackStatusResult);

                    if (trackStatusResult.getStartLamp()==1) {
                        firebase.child("lastRace").setValue(raceId);
                        fireRace.child("status").setValue("race-start");
                    }

                    if (trackStatusResult.getStartLamp()==2) {
                        qualifying = false;
                        for (int i = 0; i <= 9; i++) {
                            timeResults.put(i, new LinkedList<>());
                        }
                    }
                    if (trackStatusResult.getStartLamp()==0 && lastTrackStatusResult.getStartLamp() != 0) {
                        fireRace.child("status").setValue("race");
                        fireRace.child("time").child("race").setValue(LocalDateTime.now().toString());
                    }
                }
            } else {
                NewTimeResult newResult = new NewTimeResult(commandResult);
                List<NewTimeResult> newTimeResults = timeResults.get(newResult.getCarNr());
                NewTimeResult lastResult = null;
                if (newTimeResults.size()>0) {
                    lastResult = newTimeResults.get(newTimeResults.size()-1);
                }
                fireRace
                        .child("cars").child(((Integer) newResult.getCarNr()).toString())
                        .child("raw")
                        .child("round").child(newResult.getCreateTime().toString().replace('.','-'))
                        .setValue(newResult);
                if (!newResult.equals(lastResult)) {
                    newTimeResults.add(newResult);
                    System.out.println(newResult);
                    if (newTimeResults.size() >= 2) {
                        long roundTime = newResult.getTime() - newTimeResults.get(newTimeResults.size() - 2).getTime();
                        System.out.println("Time for car: " + newResult.getCarNr() + ", lap: " + (newTimeResults.size() - 1) + " - "
                                + ((double) roundTime) / 1000.0 + "s");

                        if (qualifying) {
                            fireRace
                                    .child("qualifying/rounds").child(((Integer) (newTimeResults.size() - 2)).toString())
                                    .child("cars").child(((Integer) newResult.getCarNr()).toString())
                                    .setValue(roundTime);
                        } else {
                            fireRace
                                    .child("race/rounds").child(((Integer) (newTimeResults.size() - 2)).toString())
                                    .child("cars").child(((Integer) newResult.getCarNr()).toString())
                                    .setValue(roundTime);

                            if (newTimeResults.size()>=21) {
                                raceEnded = true;
                                fireRace.child("time").child("end").setValue(LocalDateTime.now().toString());
                            }
                        }
                    }
                }
            }
        }
    }
}
