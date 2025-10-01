package com.chatroom.db;

import com.chatroom.model.Room;
import com.chatroom.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/ChatDB?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "123456789";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public void addUser(User user) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Users (nickname, ip, online) VALUES (?, ?, ?)");
            ps.setString(1, user.getNickname());
            ps.setString(2, user.getIp());
            ps.setBoolean(3, user.isOnline());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<User> getOnlineUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT nickname, ip FROM Users WHERE online = 1");
            while (rs.next()) {
                users.add(new User(rs.getString("nickname"), rs.getString("ip"), true));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public void updateUserOnline(String nickname, boolean online) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE Users SET online = ? WHERE nickname = ?");
            ps.setBoolean(1, online);
            ps.setString(2, nickname);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getUserIp(String nickname) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT ip FROM Users WHERE nickname = ?");
            ps.setString(1, nickname);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("ip");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void addRoom(Room room) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Rooms (room_name, multicast_ip, port, admin_nick) VALUES (?, ?, ?, ?)");
            ps.setString(1, room.getName());
            ps.setString(2, room.getMulticastIp());
            ps.setInt(3, room.getPort());
            ps.setString(4, room.getAdminNick());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT room_name, multicast_ip, port, admin_nick FROM Rooms");
            while (rs.next()) {
                rooms.add(new Room(rs.getString("room_name"), rs.getString("multicast_ip"), rs.getInt("port"), rs.getString("admin_nick")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return rooms;
    }

    public int getRoomId(String roomName) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM Rooms WHERE room_name = ?");
            ps.setString(1, roomName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public void saveChatLog(int roomId, String sender, String message, boolean isPrivate) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO ChatLogs (room_id, sender, message, timestamp, is_private) VALUES (?, ?, ?, NOW(), ?)");
            ps.setInt(1, roomId);
            ps.setString(2, sender);
            ps.setString(3, message);
            ps.setBoolean(4, isPrivate);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getChatLogs(int roomId) {
        StringBuilder logs = new StringBuilder();
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT sender, message, timestamp FROM ChatLogs WHERE room_id = ? ORDER BY timestamp");
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                logs.append(rs.getString("sender"))
                        .append(" (").append(rs.getTimestamp("timestamp")).append("): ")
                        .append(rs.getString("message")).append("\n");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return logs.toString();
    }

    public void saveFile(int chatLogId, String fileName, byte[] data) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Files (chatlog_id, file_name, file_data) VALUES (?, ?, ?)");
            ps.setInt(1, chatLogId);
            ps.setString(2, fileName);
            ps.setBytes(3, data);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public byte[] getFileData(int fileId) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT file_data FROM Files WHERE id = ?");
            ps.setInt(1, fileId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBytes("file_data");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
