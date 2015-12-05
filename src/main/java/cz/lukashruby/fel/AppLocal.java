package cz.lukashruby.fel;

import cz.blahami2.cardashboardadapter.SpeedRpmStruct;
import cz.blahami2.cardashboardadapter.StructReader;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Class that implements an SPP Server which accepts single line of
 * message from an SPP client and sends a single line of response to the client.
 */
public class AppLocal {

    interface CommandResponse {
        String get();
    }

    static HashMap<String, CommandResponse> responces = new HashMap<>();

    private boolean lineFeed = true;
    private boolean echo = true;
    private SpeedRpmStruct speedRpmStruct;


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
            responces.put("01 0C", () -> "" + parseRpm(speedRpmStruct.rpm));
            responces.put("01 0D", () -> "" + parseSpeed(speedRpmStruct.speed));

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

            speedRpmStruct = new SpeedRpmStruct(speedRpmStruct.speed+4f, speedRpmStruct.rpm+5f);
            Thread.sleep(100);


        }
        outStream.close();
        streamConnNotifier.close();


    }

    synchronized public void setSpeedRpmStruct(SpeedRpmStruct speedRpmStruct) {
        this.speedRpmStruct = speedRpmStruct;
    }

    synchronized public SpeedRpmStruct getSpeedRpmStruct() {
        return speedRpmStruct;
    }


    private String parseSpeed(float speed) {
        return String.format("%02X", (int) speed);
    }

    private String parseRpm(float rpm) {
        rpm *= 4;
        return String.format("%02X %02X", ((int) rpm) >> 8, ((int) rpm) & 0xFF);
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


        AppLocal sampleSPPServer = new AppLocal();
        SpeedRpmStruct speedRpmStruct = new SpeedRpmStruct(10, 100);
        sampleSPPServer.setSpeedRpmStruct(speedRpmStruct);
        sampleSPPServer.startServer();




    }

}
