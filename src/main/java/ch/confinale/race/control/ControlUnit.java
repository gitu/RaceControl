package ch.confinale.race.control;

import gnu.io.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class ControlUnit implements AutoCloseable{
    SerialPort serialPort;
    /**
     * A BufferedReader which will be fed by a InputStreamReader
     * converting the bytes into characters
     * making the displayed results codepage independent
     */
    private DataInputStream input;
    /** The output stream to the port */
    private OutputStream output;
    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;
    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 19200;
    private String portName;
    private Scanner inputScanner;

    public ControlUnit(String portName){
        this.portName = portName;
    }

    public synchronized void initialize() throws PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException, NoSuchPortException {
        // the next line is for Raspberry Pi and
        // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
        //System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

        serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(portName).open(portName, TIME_OUT);

        // set port parameters
        serialPort.setSerialPortParams(DATA_RATE,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        input = new DataInputStream(serialPort.getInputStream());
        output = serialPort.getOutputStream();
    }


    public synchronized byte[] sendCommand(byte[] command, byte readTill) throws IOException, InterruptedException {

        output.write(command);
        output.flush();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            byte current;
            while ((current = input.readByte()) != 1 && current != readTill) {
                buffer.write(current);
            }
        } catch (EOFException eof) {}
        buffer.flush();
        return buffer.toByteArray();
    }

    public synchronized byte[] sendCommand(String command) throws IOException, InterruptedException {
        return sendCommand(("\"" + command).getBytes(StandardCharsets.US_ASCII), (byte) '$');
    }
    public byte[] sendCommand(String command, byte delimiter) throws IOException, InterruptedException {
        return sendCommand(("\"" + command).getBytes(StandardCharsets.US_ASCII), delimiter);

    }

    @Override
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }


}
