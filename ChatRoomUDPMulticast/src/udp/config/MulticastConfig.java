package udp.config;

//Lớp cấu hình cho Multicast: chứa địa chỉ nhóm, port và kích thước buffer.
//Dùng để gom tất cả các tham số mạng vào một chỗ, dễ cấu hình khi cần thay đổi.
public class MulticastConfig {
	// Địa chỉ multicast mặc định. Phạm vi 224.0.0.0 - 239.255.255.255 dành cho multicast.
	public static final String DEFAULT_ADDRESS = "230.0.0.0";

	// Port mặc định cho dịch vụ chat multicast.
	public static final int DEFAULT_PORT = 4446;

	// Kích thước buffer (bytes) để nhận gói. 64KB đủ cho hầu hết thông điệp text + chunk file nhỏ.
	public static final int DEFAULT_BUFFER = 64 * 1024; // 64 KB

	// Biến instance để client/server có thể cấu hình khác khi cần.
	private String address;
	private int port;
	private int bufferSize;

	// Constructor mặc định dùng giá trị DEFAULT_
	public MulticastConfig() {
	this(DEFAULT_ADDRESS, DEFAULT_PORT, DEFAULT_BUFFER);
	}

	// Constructor cho phép set thủ công
	public MulticastConfig(String address, int port, int bufferSize) {
	    this.address = address;
	    this.port = port;
	    this.bufferSize = bufferSize;
	}

	// Getter / Setter
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public int getBufferSize() { return bufferSize; }

	public void setAddress(String address) { this.address = address; }
	public void setPort(int port) { this.port = port; }
	public void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }

}
