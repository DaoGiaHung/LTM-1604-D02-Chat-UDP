package udpchat;

import javax.swing.*;
import java.awt.*;

public class ClientLoginGUI extends JFrame {

    private JTextField usernameField;
    // Ô nhập tên người dùng.

    public ClientLoginGUI() {
        // Constructor -> khởi tạo giao diện login.

        setTitle("Đăng nhập Chat Room");
        // Tiêu đề cửa sổ.

        setSize(300, 150);
        // Kích thước cửa sổ.

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Khi bấm nút X thì thoát hẳn chương trình.

        setLayout(new BorderLayout());
        // Layout chính là BorderLayout.

        JPanel panel = new JPanel(new GridLayout(2,1));
        // Panel phụ có 2 hàng (1 label + 1 text field).

        usernameField = new JTextField();
        // Trường nhập username.

        panel.add(new JLabel("Nhập tên người dùng:"));
        // Hàng đầu tiên: label gợi ý nhập tên.

        panel.add(usernameField);
        // Hàng thứ hai: ô nhập tên.

        JButton loginBtn = new JButton("Đăng nhập");
        // Tạo nút đăng nhập.

        loginBtn.addActionListener(e -> doLogin());
        // Gắn sự kiện khi bấm nút sẽ gọi hàm doLogin().

        add(panel, BorderLayout.CENTER);
        add(loginBtn, BorderLayout.SOUTH);
        // Panel nhập liệu ở giữa, nút login ở dưới.
    }

    private void doLogin() {
        // Xử lý đăng nhập khi người dùng nhấn nút.

        String username = usernameField.getText().trim();
        // Lấy nội dung từ ô nhập, bỏ khoảng trắng đầu/cuối.

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên người dùng không được để trống");
            // Nếu chưa nhập gì -> hiện thông báo lỗi.
            return;
        }

        new ClientChatGUI(username).setVisible(true);
        // Nếu nhập hợp lệ -> mở cửa sổ chat chính với username đó.

        this.dispose();
        // Đóng cửa sổ login.
    }
}


