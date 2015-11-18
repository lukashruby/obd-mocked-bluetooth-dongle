/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.cardashboardadapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 *
 * @author michael
 */
public class SimAdapter {

    private static final int BUFFER_SIZE = 1024;

    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final byte[] buffer = new byte[BUFFER_SIZE];

    public SimAdapter(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.packet = new DatagramPacket(buffer, buffer.length);
    }

    public DataEnum readRequest() throws IOException {
        socket.receive(packet);
        byte[] data = packet.getData();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.position(0);
        byte mode = byteBuffer.get();
        byte pid = byteBuffer.get();
        return DataEnum.fromPid(pid);
    }

}
