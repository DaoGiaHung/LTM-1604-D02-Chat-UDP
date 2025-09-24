package udpchat;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ClientChatGUI extends JFrame {
// Đây là giao diện chính của client sau khi đăng nhập.
// Người dùng có thể xem tin nhắn, nhập và gửi tin.

    private String username;
    // Lưu tên người dùng (được nhập ở màn hình login).

    private JTextArea chatArea;
    // Khu vực hiển thị tất cả tin nhắn chat.

    private JTextField inputField;
    // Ô nhập nội dung tin nhắn.

    private MulticastSocket socket;
    // Socket để nhận tin nhắn multicast từ group.

    private InetAddress group;
    // Địa chỉ multicast (ví dụ: 230.0.0.0).

    private DatagramSocket sendSocket;
    // Socket riêng để gửi tin nhắn đi.

    public ClientChatGUI(String username) {
        // Constructor, nhận vào username từ màn hình login.
        this.username = username;

        setTitle("Chat Room - Người dùng: " + username);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        // Không cho chỉnh sửa trực tiếp chatArea, chỉ để hiển thị.

        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        // Thêm vào giữa cửa sổ, cuộn được khi nhiều tin nhắn.

        inputField = new JTextField();
        JButton sendBtn = new JButton("Gửi");

        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        // Gắn sự kiện: nhấn nút hoặc Enter thì gửi tin nhắn.

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendBtn, BorderLayout.EAST);
        // Thanh nhập liệu ở dưới, ô text bên trái, nút Gửi bên phải.

        add(bottom, BorderLayout.SOUTH);

        connect();
        // Sau khi tạo giao diện, gọi connect() để kết nối vào group.
    }

    private void connect() {
        try {
            socket = new MulticastSocket(ChatClient.GROUP_PORT);
            // Mở multicast socket để lắng nghe ở cổng chung.

            group = InetAddress.getByName(ChatClient.GROUP_ADDRESS);
            socket.joinGroup(group);
            // Tham gia nhóm multicast.

            sendSocket = new DatagramSocket();
            // Socket riêng cho việc gửi dữ liệu.

            new Thread(() -> {
                // Tạo thread riêng để nhận tin nhắn liên tục (song song với GUI).
                try {
                    byte[] buffer = new byte[4096];
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        // Chờ nhận gói tin từ group.

                        ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                        ObjectInputStream ois = new ObjectInputStream(bais);
                        Message msg = (Message) ois.readObject();
                        // Giải tuần tự hóa (deserialize) gói tin thành đối tượng Message.

                        chatArea.append(msg.toString() + "\n");
                        // Thêm tin nhắn vào khung chat.
                    }
                } catch (Exception e) {
                    chatArea.append("Error receiving message\n");
                }
            }).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không kết nối được server!");
        }
    }

    private void sendMessage() {
        try {
            String text = inputField.getText().trim();
            if (text.isEmpty()) return;
            // Nếu chưa nhập gì thì không gửi.

            Message msg = new Message(username, text);
            // Tạo đối tượng tin nhắn (chứa username, nội dung, thời gian).

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(msg);
            oos.flush();
            // Tuần tự hóa (serialize) tin nhắn thành mảng byte.

            byte[] data = baos.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length, group, ChatClient.GROUP_PORT);
            sendSocket.send(packet);
            // Đóng gói thành DatagramPacket và gửi đến group multicast.

            inputField.setText("");
            // Xóa ô nhập sau khi gửi.
        } catch (Exception e) {
            chatArea.append("Error sending message\n");
        }
    }
}
