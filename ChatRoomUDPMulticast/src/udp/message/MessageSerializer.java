package udp.message;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

// Lớp helper để gửi và nhận Message qua UDP
// Không còn toBytes() / fromBytes() nữa, mà gom vào sendMessage() và receiveMessage()
public class MessageSerializer {

    // Hàm gửi Message qua UDP multicast
    public static void sendMessage(MulticastSocket socket, InetAddress group, int port, Message m) throws IOException {
        // Tạo luồng ghi object vào mảng byte
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(m);  // ghi object Message vào stream
        oos.flush();

        // Lấy dữ liệu byte[] từ stream
        byte[] bytes = bos.toByteArray();

        // Đóng stream để giải phóng tài nguyên
        oos.close();
        bos.close();

        // Tạo packet UDP và gửi đi
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, port);
        socket.send(packet);
    }

    // Hàm đọc Message từ DatagramPacket
    public static Message receiveMessage(DatagramPacket packet) throws IOException, ClassNotFoundException {
        // Tạo luồng đọc object từ dữ liệu byte[]
        ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Ép kiểu về Message
        Message m = (Message) ois.readObject();

        // Đóng stream
        ois.close();
        bais.close();

        return m;
    }
}

