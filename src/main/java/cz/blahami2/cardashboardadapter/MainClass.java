package cz.blahami2.cardashboardadapter;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Blaha
 */
public class MainClass {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String address = null;
            if (args.length > 1) {
                address = args[1];
            }
            int port = Integer.parseInt(args[0]);
            StructReader reader = new StructReader(port, address);
            while (true) {
                SpeedRpmStruct speedRpmStruct = reader.read();
                System.out.println("speed = " + speedRpmStruct.speed);
                System.out.println("rpm = " + speedRpmStruct.rpm);
            }
        } catch (SocketException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
