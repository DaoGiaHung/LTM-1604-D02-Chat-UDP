package udpchat;  

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
// Import các class cần dùng: Serializable (để object có thể gửi qua mạng),
// SimpleDateFormat và Date để định dạng thời gian.

public class Message implements Serializable {
// Lớp Message đại diện cho một tin nhắn trong chat.
// implements Serializable nghĩa là class này có thể được "biến thành bytes" 
// để gửi đi qua UDP multicast và sau đó giải nén lại.

    private String sender;   // Tên người gửi tin nhắn
    private String content;  // Nội dung tin nhắn
    private String time;     // Thời gian gửi (dạng HH:mm:ss)

    public Message(String sender, String content) {
        this.sender = sender;   // Gán tên người gửi
        this.content = content; // Gán nội dung tin nhắn
        this.time = new SimpleDateFormat("HH:mm:ss").format(new Date());  
        // Khi tạo Message mới thì tự động lấy thời gian hiện tại và format theo kiểu giờ:phút:giây
    }

    // Các getter để lấy thông tin (được dùng bởi Client/Server khi hiển thị)
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getTime() { return time; }

    @Override
    public String toString() {
        // Chuyển Message thành chuỗi hiển thị trong chat: [time] sender: content
        return "[" + time + "] " + sender + ": " + content;
    }
}


