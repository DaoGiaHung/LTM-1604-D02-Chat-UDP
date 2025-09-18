package udp.client;

import udp.config.MulticastConfig;
import udp.message.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// GUI đơn giản bằng Swing. Mục tiêu: có giao diện (không console) để join room, gửi/nhận tin.
// Giao diện có ô chat, danh sách user, ô nhập và nút gửi.

public class ChatClientGUI extends JFrame {
	private ChatClient client;

	// Thành phần UI
	private JTextArea chatArea = new JTextArea(); // hiển thị chat
	private JTextField inputField = new JTextField(); // nhập message
	private DefaultListModel<String> userListModel = new DefaultListModel<>(); // model cho user list
	private JList<String> userList = new JList<>(userListModel);

	// Formatter thời gian
	private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	
	public ChatClientGUI(String nickname) throws IOException {
		super("UDP Chat — " + nickname);
		// Dùng cấu hình mặc định; có thể load config từ file nếu muốn.
		MulticastConfig cfg = new MulticastConfig();
		client = new ChatClient(nickname, cfg);

		// Khởi tạo UI trước, sau đó đăng ký listener và join group
		initUI();
		client.addListener(this::onMessageReceived);
		client.join();
	}
	
	private void initUI() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(800, 500);
		setLocationRelativeTo(null);

		// Thiết lập chat area
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(true);

		JPanel main = new JPanel(new BorderLayout(8,8));
		main.setBorder(new EmptyBorder(8,8,8,8));

		JScrollPane chatScroll = new JScrollPane(chatArea);
		chatScroll.setPreferredSize(new Dimension(600,400));

		JPanel right = new JPanel(new BorderLayout());
		right.setPreferredSize(new Dimension(180,400));
		right.add(new JLabel("Users"), BorderLayout.NORTH);
		right.add(new JScrollPane(userList), BorderLayout.CENTER);

		JPanel bottom = new JPanel(new BorderLayout(6,6));
		bottom.add(inputField, BorderLayout.CENTER);
		JButton sendBtn = new JButton("Send");
		bottom.add(sendBtn, BorderLayout.EAST);

		main.add(chatScroll, BorderLayout.CENTER);
		main.add(right, BorderLayout.EAST);
		main.add(bottom, BorderLayout.SOUTH);

		setContentPane(main);

		// Gửi khi click hoặc nhấn Enter
		sendBtn.addActionListener(e -> sendInput());
		inputField.addActionListener(e -> sendInput());

		// Khi đóng cửa sổ: leave group để thông báo cho những người khác
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		        try { client.leave(); } catch (IOException ex) { /* ignore - closing anyway */ }
		    }
		});

		setVisible(true);
	}
	
	// Xử lý input: hỗ trợ private message với cú pháp: /w <nickname> <message>
	private void sendInput() {
	    String text = inputField.getText().trim();
	    if (text.isEmpty()) return;
	    try {
	        // Private message shortcut
	        if (text.startsWith("/w ")) {
	            // split thành 3 phần: /w, nickname, message
	            String[] parts = text.split(" ", 3);
	            if (parts.length >= 3) {
	                String target = parts[1];
	                String msg = parts[2];
	                client.sendPrivate(target, msg);
	                appendLocal(String.format("(to %s) %s", target, msg));
	            } else {
	                appendLocal("[ERROR] Invalid private message format. Use: /w nickname message");
	            }
	        } else {
	            // Gửi message public
	            client.sendText(text);
	        }
	    } catch (IOException e) {
	        appendLocal("[ERROR] Could not send message: " + e.getMessage());
	    }
	    inputField.setText("");
	}
	
	// Append tin nhắn do chính tôi gửi vào chat area
	private void appendLocal(String s) {
	    chatArea.append("[me] " + s + "");
	}
	
	// Callback khi client nhận message
	private void onMessageReceived(Message m) {
	    SwingUtilities.invokeLater(() -> {
	        String time = m.getTimestamp() == null ? "" : dtf.format(m.getTimestamp());
	        String display;

	        // Nếu là private message và dành cho tôi, hiển thị rõ ràng
	        if (m.getType() == Message.Type.PRIVATE) {
	            String tag = (m.getTarget() == null || m.getTarget().equals(client.getNickname()))
	                    ? "(private)" : "(private->other)";
	            display = String.format("%s %s %s: %s", time, tag, m.getSender(), m.getContent());
	        } else if (m.getType() == Message.Type.SYSTEM) {
	            display = String.format("%s [system] %s", time, m.getContent());
	        } else {
	            display = String.format("%s %s: %s", time, m.getSender(), m.getContent());
	        }

	        chatArea.append(display);

	        // Cập nhật user list khi có thay đổi hệ thống
	        if (m.getType() == Message.Type.SYSTEM) {
	            userListModel.clear();
	            for (String u : client.getOnlineUsers()) userListModel.addElement(u);
	        }
	    });  
	}
	
	// Entry point đơn giản: hỏi nickname qua JOptionPane, sau đó chạy GUI
	public static void main(String[] args) throws Exception {
	    String nick = JOptionPane.showInputDialog(null, "Enter your nickname:", "Nickname", JOptionPane.PLAIN_MESSAGE);
	    if (nick == null || nick.trim().isEmpty()) return; // user hủy
	    SwingUtilities.invokeLater(() -> {
	        try {
	            new ChatClientGUI(nick.trim());
	        } catch (Exception e) {
	            e.printStackTrace();
	            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
	        }
	    });
	}
}
