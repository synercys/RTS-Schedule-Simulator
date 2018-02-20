package synercys.util.connect;

import java.util.ArrayList;
import java.util.Collections;

// the following packages are from rxtx lib.
//import gnu.io.UnsupportedCommOperationException;
//import gnu.io.CommPortIdentifier;
//import gnu.io.SerialPort;
import jssc.*;

public class SerialConnection extends Connection implements SerialPortEventListener {
    public static final int SERIAL_BAUD_RATE = 115200;
    private static final int SERIAL_TIMEOUT = 2000;

    private String portName = "";
    private SerialPort serial;

    private ArrayList<String> receivedStringList = new ArrayList<>();
    private String incomingStringBuffer = "";

    public String getPortName() {
        return portName;
    }

    public SerialConnection(String inPortName) throws Exception {
        super();
        portName = inPortName;
        open(); // This function throws exception.
    }

    private void open() throws Exception {
        serial = new SerialPort(portName);
        serial.openPort();
        serial.setParams(SERIAL_BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, true, true);
        serial.addEventListener(this);
        flush();
    }

    public Boolean reopen() {
        if (portName.equals("")) {
            return false;
        }

        try {
            open();
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (serial != null) {
                if (serial.isOpened()) {
                    serial.closePort();
                }
            }
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
        //Log.sysPutLine("Detach from the serial port: " + portName);
    }

    public Boolean isStringReady() {
        int receivedStringListSize;
        synchronized (receivedStringList) {
            receivedStringListSize = receivedStringList.size();
        }
        if (receivedStringListSize > 0) {
            return true;
        } else {
            return false;
        }
    }

    public String readNextString() {
        String resultString;
        synchronized (receivedStringList) {
            if (receivedStringList.size() > 0) {
                resultString = receivedStringList.get(0);
                receivedStringList.remove(0);
            } else {
                resultString = null;
            }
        }
        return resultString;
    }

//    @Override
//    public String read() {
//        try {
//            incomingStringBuffer += serial.readString();
//
//
//            if (incomingStringBuffer.contains(System.lineSeparator())) {
//                incomingStringBuffer.split(System.lineSeparator());
//            } else {
//                return null;
//            }
//            return serial.readString();
//        } catch (SerialPortException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public void flush() {
        //TODO: need to use synchronized (receivedStringList).
        incomingStringBuffer = "";
        receivedStringList.clear();
    }


    /**
     * Reads data from the serial port. RXTX SerialPortEventListener method.
     */
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
//        if (inputBuffer == null) {
//            inputBuffer = new StringBuilder();
//        }

        try {
            byte[] buf = serial.readBytes();
            if (buf != null && buf.length > 0) {
                String s = new String(buf, 0, buf.length);
                incomingStringBuffer += s;
                // Check for line terminator and split out command(s).
                if (incomingStringBuffer.contains(System.lineSeparator())) {
                    // Split with the -1 option will give an empty string at
                    // the end if there is a terminator there as well.
                    String []splitStrings = incomingStringBuffer.split(System.lineSeparator(), -1);

                    for (int i=0; i<splitStrings.length; i++) {
                        if (i == (splitStrings.length-1)) {
                            // do nothing here for the last string.
                            // it will be handled outside of this loop.
                        } else {
                            synchronized (receivedStringList) {
                                receivedStringList.add(splitStrings[i]);
                            }
                        }
                    }

                    //if (splitStrings[splitStrings.length-1].length() > 0) {
                        incomingStringBuffer = splitStrings[splitStrings.length-1];
                    //}

                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            //System.exit(-1);
        }
    }

    public static ArrayList<String> getSerialPorts() {
        ArrayList<String> portNameList = new ArrayList<String>();
        Collections.addAll(portNameList, SerialPortList.getPortNames());
        return portNameList;
    }

}