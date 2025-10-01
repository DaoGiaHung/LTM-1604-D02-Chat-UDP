package com.chatroom.network;

import com.chatroom.db.DatabaseManager;
import com.chatroom.model.Room;
import com.chatroom.model.User;
import com.chatroom.utils.EncryptionUtil;
import com.chatroom.utils.FileHandler;
import java.io.File;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class NetworkManager {
    private String nickname;
    private String localIp;
    private MulticastSocket discoverySocket;
    private InetAddress discoveryGroup;
    private final String DISCOVERY_IP = "239.0.0.0";
    private final int DISCOVERY_PORT = 1234;
    private final int UNICAST_PORT = 1235;  // dành cho private message
    private Map<Room, MulticastSocket> joinedRooms = new HashMap<>();
    private Map<Room, Set<String>> roomMutedUsers = new HashMap<>();
    private DatabaseManager dbManager = new DatabaseManager();
    private Consumer<String> messageCallback;
    public Consumer<List<Room>> roomsCallback;
    public Consumer<List<User>> usersCallback;

    public NetworkManager(String nickname, Consumer<String> messageCallback, Consumer<List<Room>> roomsCallback, Consumer<List<User>> usersCallback) {
        this.nickname = nickname;
        this.messageCallback = messageCallback;
        this.roomsCallback = roomsCallback;
        this.usersCallback = usersCallback;
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
            discoveryGroup = InetAddress.getByName(DISCOVERY_IP);
            discoverySocket = new MulticastSocket(DISCOVERY_PORT);
            discoverySocket.joinGroup(discoveryGroup);
            dbManager.addUser(new User(nickname, localIp, true));
            sendDiscovery("JOIN:" + nickname + ":" + localIp);
            startDiscoveryReceiver();

            // Bổ sung thread nhận tin nhắn riêng
            startPrivateReceiver();

        } catch (Exception e) { e.printStackTrace(); }
    }

    public String getNickname() {
        return nickname;
    }
    
    public void sendMessage(Room room, String msg) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String encrypted = EncryptionUtil.encrypt(msg);
        sendToRoom(room, "MSG:" + nickname + ":" + sdf.format(new java.util.Date()) + ":" + encrypted);
        dbManager.saveChatLog(dbManager.getRoomId(room.getName()), nickname, msg, false);
    }

    private void sendDiscovery(String data) {
        try {
            byte[] buf = data.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, discoveryGroup, DISCOVERY_PORT);
            discoverySocket.send(packet);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startDiscoveryReceiver() {
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    discoverySocket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    handleDiscovery(received);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }).start();
    }

    private void handleDiscovery(String msg) {
        if (msg.startsWith("JOIN:")) {
            String[] parts = msg.split(":");
            dbManager.addUser(new User(parts[1], parts[2], true));
            usersCallback.accept(dbManager.getOnlineUsers());
        } else if (msg.startsWith("LEAVE:")) {
            String[] parts = msg.split(":");
            dbManager.updateUserOnline(parts[1], false);
            usersCallback.accept(dbManager.getOnlineUsers());
        } else if (msg.startsWith("NEW_ROOM:")) {
            String[] parts = msg.split(":");
            dbManager.addRoom(new Room(parts[1], parts[2], Integer.parseInt(parts[3]), parts[4]));
            roomsCallback.accept(dbManager.getAllRooms());
        }
    }

    public void createRoom(String name, String ip, int port, String adminNick) {
        Room room = new Room(name, ip, port, adminNick); // nickname chính là admin
        dbManager.addRoom(room);
        sendDiscovery("NEW_ROOM:" + name + ":" + ip + ":" + port + ":" + nickname);
    }

    public void joinRoom(Room room) {
        try {
            InetAddress group = InetAddress.getByName(room.getMulticastIp());
            MulticastSocket socket = new MulticastSocket(room.getPort());
            socket.joinGroup(group);
            joinedRooms.put(room, socket);
            roomMutedUsers.put(room, new HashSet<>());
            sendToRoom(room, "JOIN:" + nickname);
            startRoomReceiver(room);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void leaveRoom(Room room) {
        try {
            MulticastSocket socket = joinedRooms.get(room);
            if (socket != null) {
                sendToRoom(room, "LEAVE:" + nickname);
                socket.leaveGroup(InetAddress.getByName(room.getMulticastIp()));
                socket.close();
                joinedRooms.remove(room);
                roomMutedUsers.remove(room);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startRoomReceiver(Room room) {
        new Thread(() -> {
            MulticastSocket socket = joinedRooms.get(room);
            while (socket != null && !socket.isClosed()) {
                try {
                    byte[] buf = new byte[8192];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    handleRoomMessage(room, received);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }).start();
    }

    private void handleRoomMessage(Room room, String msg) {
        if (msg.startsWith("MSG:")) {
            String[] parts = msg.split(":", 4);
            String sender = parts[1];
            String time = parts[2];
            String decrypted = EncryptionUtil.decrypt(parts[3]);
            if (!sender.equals(nickname) && !roomMutedUsers.get(room).contains(sender)) {
                messageCallback.accept(sender + " (" + time + "): " + decrypted + "\n");
                dbManager.saveChatLog(dbManager.getRoomId(room.getName()), sender, decrypted, false);
            }
        } else if (msg.startsWith("JOIN:")) {
            messageCallback.accept(msg.substring(5) + " joined.\n");
            usersCallback.accept(dbManager.getOnlineUsers());
        } else if (msg.startsWith("LEAVE:")) {
            messageCallback.accept(msg.substring(6) + " left.\n");
            usersCallback.accept(dbManager.getOnlineUsers());
        } else if (msg.startsWith("FILE:")) {
            String[] parts = msg.split(":", 4);
            String sender = parts[1];
            String fileName = parts[2];
            String base64 = parts[3];
            try {
                FileHandler.decodeBase64ToFile(base64, fileName);
                messageCallback.accept(sender + " sent file: " + fileName + "\n");
            } catch (Exception e) { e.printStackTrace(); }
        } else if (msg.startsWith("KICK:")) {
            if (msg.substring(5).equals(nickname)) {
                leaveRoom(room);
                messageCallback.accept("You were kicked from " + room.getName() + "\n");
            }
        } else if (msg.startsWith("MUTE:")) {
            roomMutedUsers.get(room).add(msg.substring(5));
        } else if (msg.startsWith("UNMUTE:")) {
            roomMutedUsers.get(room).remove(msg.substring(7));
        } else if (msg.startsWith("SCREEN:")) {
            String[] parts = msg.split(":", 3);
            String sender = parts[1];
            String base64 = parts[2];
            try {
                FileHandler.decodeBase64ToFile(base64, "screen_" + sender + ".png");
                messageCallback.accept(sender + " shared screen: screen_" + sender + ".png\n");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void sendToRoom(Room room, String data) {
        try {
            MulticastSocket socket = joinedRooms.get(room);
            byte[] buf = data.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(room.getMulticastIp()), room.getPort());
            socket.send(packet);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Gửi tin nhắn riêng (private)
    public void sendPrivate(String targetNick, String msg) {
        String targetIp = dbManager.getUserIp(targetNick);
        if (targetIp != null) {
            try {
                DatagramSocket socket = new DatagramSocket();
                String encrypted = EncryptionUtil.encrypt(msg);
                byte[] buf = ("PRIVATE:" + nickname + ":" + encrypted).getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(targetIp), UNICAST_PORT);
                socket.send(packet);
                dbManager.saveChatLog(-1, nickname, msg, true);
                socket.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // Thread lắng nghe private message
    private void startPrivateReceiver() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(UNICAST_PORT)) {
                while (true) {
                    byte[] buf = new byte[8192];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    if (msg.startsWith("PRIVATE:")) {
                        String[] parts = msg.split(":", 3);
                        String sender = parts[1];
                        String decrypted = EncryptionUtil.decrypt(parts[2]);
                        messageCallback.accept("[Private] " + sender + ": " + decrypted + "\n");
                        dbManager.saveChatLog(-1, sender, decrypted, true);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // Kick user (chỉ admin mới có quyền)
    public void kickUser(Room room, String userNick) {
        if (nickname.equals(room.getAdminNick())) {
            sendToRoom(room, "KICK:" + userNick);
        }
    }

    // Mute user
    public void muteUser(Room room, String userNick) {
        if (nickname.equals(room.getAdminNick())) {
            sendToRoom(room, "MUTE:" + userNick);
            roomMutedUsers.get(room).add(userNick);
        }
    }

    // Unmute user
    public void unmuteUser(Room room, String userNick) {
        if (nickname.equals(room.getAdminNick())) {
            sendToRoom(room, "UNMUTE:" + userNick);
            roomMutedUsers.get(room).remove(userNick);
        }
    }

    public void sendFile(Room room, File file) {
        try {
            String base64 = FileHandler.encodeFileToBase64(file);
            sendToRoom(room, "FILE:" + nickname + ":" + file.getName() + ":" + base64);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void shareScreen(Room room) {
        try {
            java.awt.Robot robot = new java.awt.Robot();
            java.awt.Rectangle screenRect = new java.awt.Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
            java.awt.image.BufferedImage screen = robot.createScreenCapture(screenRect);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(screen, "png", baos);
            String base64 = java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
            sendToRoom(room, "SCREEN:" + nickname + ":" + base64);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void shutdown() {
        sendDiscovery("LEAVE:" + nickname);
        dbManager.updateUserOnline(nickname, false);
        for (Room room : joinedRooms.keySet()) {
            leaveRoom(room);
        }
        try {
            discoverySocket.leaveGroup(discoveryGroup);
            discoverySocket.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}