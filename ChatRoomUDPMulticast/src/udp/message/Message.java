package udp.message;

import java.io.Serializable;
import java.time.Instant;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;

	// Các loại message hỗ trợ
	public enum Type { NORMAL, PRIVATE, SYSTEM, FILE }

	// Nickname người gửi (sender) — dùng để hiển thị và phân biệt user
	private String sender;

	// Thời điểm gửi tin (UTC-based Instant), giúp hiển thị timestamp chính xác
	private Instant timestamp;

	// Nội dung: với NORMAL/SYSTEM/PRIVATE dùng text; với FILE có thể là metadata (filename, chunk index...)
	private String content;

	// Loại message
	private Type type;

	// Nếu là PRIVATE, target lưu nickname người nhận
	private String target;

	// Constructor rỗng để serialization/deserialization
	public Message() {}

	// Constructor tiện lợi khi tạo message text
	public Message(String sender, Instant timestamp, String content, Type type) {
	    this.sender = sender;
	    this.timestamp = timestamp;
	    this.content = content;
	    this.type = type;
	}

	// Getter / Setter
	public String getSender() { return sender; }
	public void setSender(String sender) { this.sender = sender; }

	public Instant getTimestamp() { return timestamp; }
	public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }

	public Type getType() { return type; }
	public void setType(Type type) { this.type = type; }

	public String getTarget() { return target; }
	public void setTarget(String target) { this.target = target; }

	// toString hữu ích khi log
	@Override
	public String toString() {
	    return String.format("[%s] %s: %s", timestamp == null ? "" : timestamp.toString(), sender, content);
	}
}
