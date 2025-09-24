package udpchat;

import javax.swing.*;

public class ChatClient {

    public static final String GROUP_ADDRESS = "230.0.0.0";
    // Địa chỉ multicast mà client sẽ tham gia cùng với server.
    // Phải trùng với GROUP_ADDRESS của server thì mới "nói chuyện" được với nhau.

    public static final int GROUP_PORT = 4446;
    // Cổng multicast cũng phải trùng với bên server.

    public static void main(String[] args) {
        // Hàm main để chạy client.

        SwingUtilities.invokeLater(() -> {
            // Tương tự như server: tạo GUI trên Event Dispatch Thread để tránh lỗi khi dùng Swing.

            new ClientLoginGUI().setVisible(true);
            // Mở giao diện đăng nhập cho client.
            // Người dùng sẽ nhập tên (username) để tham gia vào phòng chat.
            // Sau khi login thành công -> chương trình sẽ mở tiếp ChatClientGUI (cửa sổ chat chính).
        });
    }
}

