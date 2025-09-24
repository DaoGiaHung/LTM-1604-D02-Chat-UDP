package udpchat;  

import javax.swing.*;  
import java.awt.*;  
import java.net.*;  
import java.io.*;  
// Import các thư viện cần thiết:
// - javax.swing, java.awt: dùng để tạo giao diện GUI.
// - java.net: dùng cho lập trình mạng (MulticastSocket, InetAddress, DatagramPacket).
// - java.io: dùng để xử lý dữ liệu (ObjectInputStream, ByteArrayInputStream).

public class ServerGUI extends JFrame {  
    // ServerGUI kế thừa JFrame → đây là cửa sổ giao diện của server.

    private JTextArea logArea;  
    // Vùng hiển thị text (logArea) để in ra thông tin server và các tin nhắn nhận được.

    private MulticastSocket socket;  
    // Socket multicast để server tham gia vào group, nhận dữ liệu từ client gửi tới.

    private InetAddress group;  
    // Địa chỉ group multicast (ví dụ 230.0.0.0).

    public ServerGUI() {  
        // Hàm khởi tạo ServerGUI (constructor).
        setTitle("UDP Multicast Chat Server");  
        // Đặt tiêu đề cho cửa sổ.

        setSize(400, 300);  
        // Đặt kích thước cửa sổ (400x300 pixels).

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        // Khi bấm nút X sẽ tắt hẳn chương trình.

        setLayout(new BorderLayout());  
        // Dùng BorderLayout để sắp xếp các thành phần giao diện.

        logArea = new JTextArea();  
        // Tạo một vùng text để hiển thị log.

        logArea.setEditable(false);  
        // Chỉ cho đọc log, không cho gõ chữ vào.

        add(new JScrollPane(logArea), BorderLayout.CENTER);  
        // Thêm logArea vào cửa sổ, đặt ở giữa và cho phép scroll khi text nhiều.

        startServer();  
        // Gọi hàm khởi động server ngay khi mở giao diện.
    }

    private void startServer() {  
        // Hàm khởi động server.
        new Thread(() -> {  
            // Tạo một luồng (thread) riêng để server chạy nền, không làm treo giao diện.
            try {
                group = InetAddress.getByName(ChatServer.GROUP_ADDRESS);  
                // Lấy địa chỉ IP multicast (ví dụ: 230.0.0.0) từ class ChatServer.

                socket = new MulticastSocket(ChatServer.GROUP_PORT);  
                // Tạo socket multicast lắng nghe ở cổng (port) đã định (ví dụ: 4446).

                socket.joinGroup(group);  
                // Tham gia vào nhóm multicast, nhờ đó server có thể nhận tin từ group này.

                logArea.append("Server started at " 
                    + ChatServer.GROUP_ADDRESS + ":" + ChatServer.GROUP_PORT + "\n");  
                // Ghi log rằng server đã khởi động thành công.

                byte[] buffer = new byte[4096];  
                // Tạo buffer để chứa dữ liệu nhận được (tối đa 4096 byte).

                while (true) {  
                    // Vòng lặp vô hạn → server luôn chạy để nhận tin.
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);  
                    socket.receive(packet);  
                    // Nhận một gói tin UDP từ client gửi tới.

                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());  
                    ObjectInputStream ois = new ObjectInputStream(bais);  
                    // Chuyển dữ liệu byte thành luồng để đọc đối tượng.

                    Message msg = (Message) ois.readObject();  
                    // Giải nén dữ liệu thành đối tượng Message (đã Serializable).

                    logArea.append(msg.toString() + "\n");  
                    // In tin nhắn ra cửa sổ log.
                }
            } catch (Exception e) {  
                // Nếu có lỗi thì ghi log lỗi.
                logArea.append("Server error: " + e.getMessage() + "\n");  
            }
        }).start();  
        // Kết thúc thread → server bắt đầu chạy song song với GUI.
    }
}



