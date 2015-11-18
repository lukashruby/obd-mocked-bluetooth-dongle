package cz.lukashruby.fel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

import javax.bluetooth.*;
import javax.microedition.io.*;

/**
 * Class that implements an SPP Server which accepts single line of
 * message from an SPP client and sends a single line of response to the client.
 */
public class App {

    interface CommandResponse {
        String get();
    }

    static HashMap<String, CommandResponse> responces = new HashMap<>();

    static boolean lineFeed = true;
    static boolean echo = true;
    static float sSpeed = 0;
    static float sRpm = 0;


    //start server

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
        responces.put("ATZ",  () -> "ELM327 v1.3a OBD GPSLogger");
        responces.put("ATE0",  () -> "OK");
        responces.put("ATM0",  () -> "?");
        responces.put("ATL0",  () -> "OK");
        responces.put("ATS0",  () -> "OK");
        responces.put("ATH0",  () -> "OK");
        responces.put("AT AT1",  () -> "OK");
        responces.put("AT STFE", () ->  "OK");
        responces.put("ATSP0", () ->  "OK");
        responces.put("0100",  () -> "410000180000FB");
        responces.put("010c",  () -> "410C" + parseRpm(sRpm+=15 ));
        responces.put("010d", () -> "410D" + parseSpeed(sSpeed+=0.5));

        Scanner scanner = new Scanner(System.in);
        while ((lineRead = bReader.readLine()) != null) {
            System.out.println(lineRead);


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

    private String parseSpeed(float speed) {
        return String.format("%02X", (int) speed);
    }

    private String parseRpm(float rpm) {
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

        App sampleSPPServer = new App();
        sampleSPPServer.startServer();

    }
}
