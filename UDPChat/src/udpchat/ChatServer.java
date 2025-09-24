package udpchat;

import java.net.*;
import javax.swing.*;
// Import các thư viện cần: java.net (mạng), javax.swing (giao diện).

public class ChatServer {

    public static final String GROUP_ADDRESS = "230.0.0.0";
    // Địa chỉ multicast (multicast IP range: 224.0.0.0 - 239.255.255.255).
    // Ở đây server và client sẽ cùng "tham gia" vào nhóm này để gửi/nhận dữ liệu.

    public static final int GROUP_PORT = 4446;
    // Port mà cả server và client dùng chung để truyền nhận dữ liệu UDP.

    public static void main(String[] args) {
        // Hàm main để chạy chương trình.

        SwingUtilities.invokeLater(() -> {
            // Đảm bảo tạo GUI chạy trên luồng Event Dispatch Thread (EDT) 
            // -> đây là best practice khi dùng Swing.

            new ServerGUI().setVisible(true);
            // Tạo cửa sổ GUI của server (ServerGUI) và hiển thị nó lên màn hình.
            // Khi GUI khởi tạo, nó sẽ tự gọi startServer() để join vào group multicast 
            // và bắt đầu lắng nghe tin nhắn.
        });
    }
}

