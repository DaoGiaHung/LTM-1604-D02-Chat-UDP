package udp.client;

import udp.config.MulticastConfig;
import udp.message.Message;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatClient {
    // Socket UDP multicast để tham gia group chat
    private MulticastSocket socket;
    // Địa chỉ multicast (ví dụ: 230.0.0.1)
    private InetAddress group;
    // Cấu hình (port, buffer size…)
    private MulticastConfig config;
    // Nickname (tên hiển thị của user trong phòng chat)
    private String nickname;
    // Biến cờ kiểm soát vòng lặp lắng nghe, đảm bảo thread listener biết trạng thái
    private volatile boolean running = false;

    // Bảng danh sách user online: key = nickname, value = thời gian cuối cùng thấy user
    private final Map<String, Instant> onlineUsers = new ConcurrentHashMap<>();
    // Danh sách listener (observer pattern) để GUI hoặc app khác đăng ký nhận sự kiện
    private final List<ChatEventListener> listeners = Collections.synchronizedList(new ArrayList<>());

    // --- Constructor ---
    public ChatClient(String nickname, MulticastConfig config) throws IOException {
        this.nickname = nickname;
        this.config = config;
        // Lấy InetAddress từ địa chỉ multicast trong config
        this.group = InetAddress.getByName(config.getAddress());
        // Tạo socket multicast ở port cấu hình
        this.socket = new MulticastSocket(config.getPort());
        // TTL = 4 nghĩa là gói multicast sẽ không vượt quá 4 router
        this.socket.setTimeToLive(4);
        // Timeout = 0 (blocking, không timeout khi receive)
        this.socket.setSoTimeout(0);
    }

    // --- Join group ---
    public void join() throws IOException {
        socket.joinGroup(group);             // Tham gia multicast group
        running = true;                      // Đánh dấu client đang chạy
        sendSystem(nickname + " has joined the chat"); // Gửi tin hệ thống báo có user mới
        listenAsync();                       // Khởi động thread listener lắng nghe tin nhắn
    }

    // --- Leave group ---
    public void leave() throws IOException {
        running = false;                     // Ngừng vòng lặp lắng nghe
        sendSystem(nickname + " has left the chat"); // Gửi tin hệ thống báo user thoát
        try { 
            socket.leaveGroup(group);        // Thoát khỏi group
        } catch (IOException ignored) {}
        socket.close();                      // Đóng socket
    }

    // --- Gửi tin nhắn thường ---
    public void sendText(String text) throws IOException {
        Message m = new Message(nickname, Instant.now(), text, Message.Type.NORMAL);
        sendMessage(m);
    }

    // --- Gửi tin nhắn riêng (private message) ---
    public void sendPrivate(String target, String text) throws IOException {
        Message m = new Message(nickname, Instant.now(), text, Message.Type.PRIVATE);
        m.setTarget(target); // đặt người nhận cụ thể
        sendMessage(m);
    }

    // --- Gửi tin nhắn hệ thống (system message: join/leave, thông báo…) ---
    private void sendSystem(String text) throws IOException {
        Message m = new Message(nickname, Instant.now(), text, Message.Type.SYSTEM);
        sendMessage(m);
    }

    // --- Hàm gửi chung (serialization object -> byte[] -> DatagramPacket) ---
    private void sendMessage(Message m) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(m);                  // Ghi đối tượng Message thành bytes
        oos.flush();
        byte[] bytes = bos.toByteArray();    // Lấy mảng byte

        // Tạo packet UDP và gửi đi
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, config.getPort());
        socket.send(packet);
    }

    // --- Thread listener chạy bất đồng bộ ---
    private void listenAsync() {
        Thread listener = new Thread(() -> {
            while (running && !socket.isClosed()) {
                try {
                    byte[] buf = new byte[config.getBufferSize()];   // tạo buffer
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);                          // chờ nhận packet

                    // Giải tuần tự (deserialize) object Message từ byte[]
                    ObjectInputStream ois = new ObjectInputStream(
                            new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                    Message m = (Message) ois.readObject();

                    // Xử lý tin nhắn nhận được
                    handleIncoming(m);
                } catch (Exception e) {
                    if (!running) break; // nếu client đã stop thì thoát
                    e.printStackTrace();
                }
            }
        }, "udp-listener");
        listener.setDaemon(true); // thread nền, JVM tự dừng khi main thread kết thúc
        listener.start();
    }

    // --- Xử lý tin nhắn nhận vào ---
    private void handleIncoming(Message m) {
        if (m == null) return;

        // Nếu là tin riêng nhưng target != mình -> bỏ qua
        if (m.getType() == Message.Type.PRIVATE && m.getTarget() != null &&
                !m.getTarget().equals(this.nickname) && !m.getTarget().equals("ALL")) return;

        // Nếu là SYSTEM thì cập nhật danh sách online
        if (m.getType() == Message.Type.SYSTEM) {
            if (m.getContent().contains("has joined"))
                onlineUsers.put(m.getSender(), m.getTimestamp());
            else if (m.getContent().contains("has left"))
                onlineUsers.remove(m.getSender());
        } else {
            // Với NORMAL hoặc PRIVATE thì cũng cập nhật activity của user
            onlineUsers.put(m.getSender(), m.getTimestamp());
        }

        // Báo cho tất cả listener đã đăng ký (GUI, console…) biết có tin nhắn mới
        for (ChatEventListener l : listeners) l.onMessage(m);
    }

    // --- Interface listener để GUI/app khác đăng ký nhận tin ---
    public interface ChatEventListener { 
        void onMessage(Message m); 
    }
    public void addListener(ChatEventListener l) { listeners.add(l); }
    public void removeListener(ChatEventListener l) { listeners.remove(l); }

    // --- Lấy danh sách user online ---
    public Collection<String> getOnlineUsers() { 
        return Collections.unmodifiableSet(onlineUsers.keySet()); 
    }
    public String getNickname() { return nickname; }
}
