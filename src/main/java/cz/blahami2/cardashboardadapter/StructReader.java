package cz.blahami2.cardashboardadapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;

/**
 *
 * @author Michael Blaha
 */
public class StructReader {

    private static final int BUFFER_SIZE = 1024;

    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final byte[] buffer = new byte[BUFFER_SIZE];

    public StructReader(int port, String address) throws SocketException, UnknownHostException {
        if (address != null) {
            System.out.println("address = " + address);
            this.socket = new DatagramSocket(port, InetAddress.getByName(address));
        } else {
            System.out.println("address is null");
            this.socket = new DatagramSocket(port);
        }
        System.out.println("port = " + port);
        this.packet = new DatagramPacket(buffer, buffer.length);
    }

    public SpeedRpmStruct read() throws IOException {
        //System.out.println("receiving packet");
        socket.receive(packet);
        byte[] data = packet.getData();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
	    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.position(0);
        return new SpeedRpmStruct(byteBuffer.getFloat(), byteBuffer.getFloat());
    }

}
