package udp.server;

import udp.config.MulticastConfig;
import udp.message.Message;
import udp.message.MessageSerializer;

import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.util.*;

// ChatServer lắng nghe tất cả message trên multicast group, ghi history và quản lý user list.
// Lưu ý: trong môi trường thực tế, server thường dùng unicast TCP/UDP riêng để quản lý user,
// hoặc dùng centralized storage. Ở đây server là "sniffer" đơn giản trên multicast để demo.
public class ChatServer {
    private MulticastConfig config;   // cấu hình server: address, port, buffer
    private MulticastSocket socket;   // socket UDP multicast
    private InetAddress group;        // địa chỉ multicast group
    private volatile boolean running = false; // trạng thái server

    // Lưu lịch sử đơn giản trong memory (có thể dump ra file để persistent)
    private final List<Message> history = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> users = Collections.synchronizedSet(new HashSet<>());

    public ChatServer(MulticastConfig config) throws IOException {
        this.config = config;
        this.group = InetAddress.getByName(config.getAddress());
        this.socket = new MulticastSocket(config.getPort());
        socket.joinGroup(group); // server join vào group để nghe mọi message
    }

    // Bắt đầu lắng nghe
    public void start() {
        running = true;
        Thread t = new Thread(() -> {
            byte[] buf = new byte[config.getBufferSize()];
            while (running) {
                try {
                    // Tạo packet nhận dữ liệu
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    socket.receive(p); // blocking nhận packet

                    // --- Dùng MessageSerializer.receiveMessage thay cho fromBytes ---
                    Message m = MessageSerializer.receiveMessage(p);

                    // Thêm vào lịch sử
                    history.add(m);

                    // Cập nhật user list nếu là SYSTEM message
                    if (m.getType() == Message.Type.SYSTEM) {
                        if (m.getContent().contains("has joined")) users.add(m.getSender());
                        if (m.getContent().contains("has left")) users.remove(m.getSender());
                    }

                    // Log ra console để giảng viên/QA xem
                    logToConsole(m);

                } catch (IOException | ClassNotFoundException e) {
                    if (running) e.printStackTrace(); // chỉ in lỗi khi server chưa stop
                }
            }
        }, "server-listener");

        t.setDaemon(true); // không chặn JVM shutdown
        t.start();
    }

    // Dừng server
    public void stop() throws IOException {
        running = false;
        socket.leaveGroup(group);
        socket.close();
    }

    // In message ra console
    private void logToConsole(Message m) {
        System.out.printf("[%s] %s: %s\n",
                m.getTimestamp() == null ? "" : m.getTimestamp().toString(),
                m.getSender(),
                m.getContent());
    }

    // Trả về lịch sử chat (unmodifiable để chỉ đọc)
    public List<Message> getHistory() { return Collections.unmodifiableList(history); }

    // Trả về danh sách user đang online (theo log server)
    public Set<String> getUsers() { return Collections.unmodifiableSet(users); }
}
