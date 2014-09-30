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
    final protected static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

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

        ControlUnit controlUnit = new ControlUnit(config.getComPort());
        controlUnit.initialize();

        System.out.println(new String(controlUnit.sendCommand("0")));
        Thread.sleep(500);
        controlUnit.sendCommand("=", (byte) '=');


        Firebase firebase = new Firebase("https://" + config.getFirebaseName() + ".firebaseio.com/");

        Map<Integer, List<NewTimeResult>> timeResults = new HashMap<>();
        for (int i = 0; i <= 9; i++) {
            timeResults.put(i, new LinkedList<>());
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
                    newTimeResults.get(newTimeResults.size()-1);
                }
                if (!newResult.equals(lastResult)) {
                    newTimeResults.add(newResult);
                    System.out.println(newResult);
                    if (newTimeResults.size() >= 2) {
                        System.out.println("Time for car: " + newResult.getCarNr() + ", lap: " + (newTimeResults.size() - 1) + " - "
                                + ((double) (newResult.getTime() - newTimeResults.get(newTimeResults.size() - 2).getTime())) / 1000.0 + "s");
                    }
                }
            }
        }

    }
}
