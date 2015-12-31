package cz.lukashruby.fel;

import cz.blahami2.cardashboardadapter.SpeedRpmStruct;
import cz.blahami2.cardashboardadapter.StructReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.bluetooth.*;
import javax.microedition.io.*;

public class AppTorque {

    interface CommandResponse {
        String get();
    }

    static HashMap<String, CommandResponse> responces = new HashMap<>();

    private boolean lineFeed = true;
    private boolean echo = true;
    private SpeedRpmStruct speedRpmStruct;



    private boolean sendResponse(OutputStream os, String command, String message) throws IOException {
        if (echo) {
            os.write(command.getBytes());
        }
        if (lineFeed) {
            os.write("\r\n".getBytes());
        } else {
            os.write("\r".getBytes());
        }
        os.write((message).getBytes());
        if (lineFeed) {
            os.write("\r\n".getBytes());
        } else {
            os.write("\r".getBytes());
        }
        os.write(">".getBytes());
        System.out.println("\t " + message);
        return true;
    }

    static Thread structReaderThread;

    private void connectToSimulator(String[] args) throws SocketException, UnknownHostException {
        String address = null;
        if (args.length > 1) {
            address = args[1];
        }
        int port = 5500;
        structReaderThread = new Thread(new StructReaderRunnable(this, port));
        structReaderThread.setDaemon(true);
        structReaderThread.start();
    }

    private void startServer() throws Exception {

        //Create a UUID for SPP
        UUID uuid = new UUID("1101", true);
        //Create the servicve url
        String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";

        //open server url
        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);

        //Wait for client connection
        System.out.println("\nServer Started. Waiting for clients to connect...");
        StreamConnection connection = streamConnNotifier.acceptAndOpen();

        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: " + dev.getBluetoothAddress());
        System.out.println("Remote device name: " + dev.getFriendlyName(true));


        InputStream inStream = connection.openInputStream();
        BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
        String lineRead = null;
        OutputStream outStream = connection.openOutputStream();


        responces.clear();

        responces.put("ATD", () -> "OK");
        responces.put("ATZ", () -> "ELM327 v1.3a OBD GPSLogger");
        responces.put("ATE0", () -> "OK");
        responces.put("ATM0", () -> "?");
        responces.put("ATL0", () -> "OK");
        responces.put("ATS0", () -> "OK");
        responces.put("ATH0", () -> "OK");
        responces.put("AT AT1", () -> "OK");
        responces.put("AT STFE", () -> "OK");
        responces.put("ATSP0", () -> "OK");
        responces.put("0100", () -> "410000180000FB");


        Scanner scanner = new Scanner(System.in);
        while ((lineRead = bReader.readLine()) != null) {
            System.out.println(lineRead);
            System.out.println(speedRpmStruct);
            responces.put("010c", () -> "410C" + parseRpm(speedRpmStruct.rpm));
            responces.put("010d", () -> "410D" + parseSpeed(speedRpmStruct.speed));

            if (lineRead.contains("ATL0")) {
                lineFeed = false;
            }
            if (lineRead.contains("ATE0")) {
                echo = false;
            }


            Map.Entry<String, CommandResponse> r = get(responces, lineRead);

            if (r == null) {
                sendResponse(outStream, lineRead, "NO DATA");
            } else {
                sendResponse(outStream, r.getKey(), r.getValue().get());
            }

            outStream.flush();
            Thread.sleep(100);


        }
        outStream.close();
        streamConnNotifier.close();


    }

    synchronized public void setSpeedRpmStruct(SpeedRpmStruct speedRpmStruct){
        this.speedRpmStruct = speedRpmStruct;
    }

    synchronized public SpeedRpmStruct getSpeedRpmStruct(){
        return speedRpmStruct;
    }


    private String parseSpeed(float speed) {
        return String.format("%02X", (int) speed);
    }

    private String parseRpm(float rpm) {
        rpm *= 4;
        return String.format("%02X%02X", ((int) rpm) >> 8, ((int) rpm) & 0xFF);
    }


    private static Map.Entry<String, CommandResponse> get(Map<String, CommandResponse> map, String key) {
        for (Map.Entry<String, CommandResponse> entry : map.entrySet()) {
            if (key.contains(entry.getKey())) {
                return entry;
            }
        }
        return null;
    }


    public static void main(String[] args) throws Exception {

        //display local device address and name
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: " + localDevice.getBluetoothAddress());
        System.out.println("Name: " + localDevice.getFriendlyName());

        AppTorque sampleSPPServer = new AppTorque();
        sampleSPPServer.connectToSimulator(args);
        sampleSPPServer.startServer();

    }

    static class StructReaderRunnable implements Runnable {

        final StructReader structReader;
        final AppTorque app;

        public StructReaderRunnable(AppTorque app, int port) throws SocketException, UnknownHostException {
            structReader = new StructReader(port, null);
            this.app = app;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    app.setSpeedRpmStruct(structReader.read());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
