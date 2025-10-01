/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.chatroom.gui;
import com.chatroom.db.DatabaseManager;
import com.chatroom.model.Room;
import com.chatroom.model.User;
import com.chatroom.network.NetworkManager;
import com.chatroom.utils.ThemeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author chun
 */
public class MainForm extends javax.swing.JFrame {
    private NetworkManager networkManager;
    private DatabaseManager dbManager = new DatabaseManager();
    private Room selectedRoom;
    private Map<String, Room> roomMap = new HashMap<>();
    private Map<String, User> userMap = new HashMap<>();
    private String lastChatLog; 
    private Map<String, String> lastRoomLogs = new HashMap<>();

    /**
     * Creates new form MainForm
     */
    public MainForm(String nickname) {
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Chat Room - " + nickname);

        networkManager = new NetworkManager(
            nickname,
            msg -> {
                SwingUtilities.invokeLater(() -> {
                    txtChatArea.append(msg);
                    txtChatArea.setCaretPosition(txtChatArea.getDocument().getLength());
                });
            },
            rooms -> {
                SwingUtilities.invokeLater(() -> {
                    roomMap.clear();
                    DefaultListModel<String> roomModel = new DefaultListModel<>();
                    Set<String> uniqueRooms = new HashSet<>(); 
                    for (Room room : rooms) {
                        if (uniqueRooms.add(room.getName())) { 
                            roomMap.put(room.getName(), room);
                            roomModel.addElement(room.getName());
                            // Khởi tạo lastRoomLogs để tránh popup khi mới khởi động
                            int roomId = dbManager.getRoomId(room.getName());
                            String chatLogs = dbManager.getChatLogs(roomId);
                            lastRoomLogs.put(room.getName(), chatLogs == null ? "" : chatLogs);
                        }
                    }
                    listRooms.setModel(roomModel);
                });
            },
            users -> {
                SwingUtilities.invokeLater(() -> {
                    userMap.clear();
                    DefaultListModel<String> userModel = new DefaultListModel<>();
                    Set<String> uniqueUsers = new HashSet<>(); 
                    for (User user : users) {
                        if (uniqueUsers.add(user.getNickname())) { 
                            userMap.put(user.getNickname(), user);
                            userModel.addElement(user.getNickname());
                        }
                    }
                    listUsers.setModel(userModel);
                });
            }
        );

        updateRooms();
        updateUsers();

        Timer refreshTimer = new Timer(1000, e -> refreshChatArea());
        refreshTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                networkManager.shutdown();
                refreshTimer.stop(); 
            }
        });
    }

    private void updateRooms() {
        networkManager.roomsCallback.accept(dbManager.getAllRooms());
    }

    private void updateUsers() {
        networkManager.usersCallback.accept(dbManager.getOnlineUsers());
    }

    // Hàm cập nhật txtChatArea
    private void refreshChatArea() {
    for (String roomName : roomMap.keySet()) {
        int roomId = dbManager.getRoomId(roomName);
        String chatLogs = dbManager.getChatLogs(roomId);

        // Dùng containsKey thay vì getOrDefault (tương thích Java cũ hơn)
        String oldLogs = lastRoomLogs.containsKey(roomName) ? lastRoomLogs.get(roomName) : "";
        if (!chatLogs.equals(oldLogs)) {
            lastRoomLogs.put(roomName, chatLogs);

            if (selectedRoom != null && roomName.equals(selectedRoom.getName())) {
                // Phòng đang mở → cập nhật chat area
                SwingUtilities.invokeLater(() -> {
                    txtChatArea.setText(chatLogs);
                    txtChatArea.setCaretPosition(txtChatArea.getDocument().getLength());
                    lastChatLog = chatLogs;
                });
            } else {
                // Phòng khác → gắn badge số tin nhắn chưa đọc vào tên phòng
                SwingUtilities.invokeLater(() -> {
                    DefaultListModel<String> model = (DefaultListModel<String>) listRooms.getModel();
                	for (int i = 0; i < model.size(); i++) {
                	    String name = model.getElementAt(i);
                	        if (name.equals(roomName)) {
                	            if (!name.contains("(new)")) {
                	                model.set(i, name + " (new)");
                	            }
                	            break;
                	        }
                	}
                });
            }
        }
    }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        listRooms = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        listUsers = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtChatArea = new javax.swing.JTextArea();
        txtMessage = new javax.swing.JTextField();
        btnSend = new javax.swing.JButton();
        btnCreateRoom = new javax.swing.JButton();
        btnJoinRoom = new javax.swing.JButton();
        btnLeaveRoom = new javax.swing.JButton();
        btnSendPrivate = new javax.swing.JButton();
        btnKick = new javax.swing.JButton();
        btnMute = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        listRooms.setBackground(new java.awt.Color(0, 102, 102));
        listRooms.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        listRooms.setForeground(new java.awt.Color(255, 255, 255));
        listRooms.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Room" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        listRooms.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listRoomsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listRooms);

        listUsers.setBackground(new java.awt.Color(0, 102, 102));
        listUsers.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        listUsers.setForeground(new java.awt.Color(255, 255, 255));
        listUsers.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "User" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        listUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listUsersMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(listUsers);

        txtChatArea.setColumns(20);
        txtChatArea.setFont(new java.awt.Font("Helvetica Neue", 0, 24)); // NOI18N
        txtChatArea.setRows(5);
        jScrollPane3.setViewportView(txtChatArea);

        btnSend.setBackground(new java.awt.Color(0, 102, 102));
        btnSend.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        btnSend.setForeground(new java.awt.Color(255, 255, 255));
        btnSend.setText("Send");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        btnCreateRoom.setBackground(new java.awt.Color(0, 102, 102));
        btnCreateRoom.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        btnCreateRoom.setForeground(new java.awt.Color(255, 255, 255));
        btnCreateRoom.setText("Create Room");
        btnCreateRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateRoomActionPerformed(evt);
            }
        });

        btnJoinRoom.setBackground(new java.awt.Color(0, 102, 102));
        btnJoinRoom.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        btnJoinRoom.setForeground(new java.awt.Color(255, 255, 255));
        btnJoinRoom.setText("Join Room");
        btnJoinRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJoinRoomActionPerformed(evt);
            }
        });

        btnLeaveRoom.setBackground(new java.awt.Color(0, 102, 102));
        btnLeaveRoom.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        btnLeaveRoom.setForeground(new java.awt.Color(255, 255, 255));
        btnLeaveRoom.setText("Leave Room");
        btnLeaveRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLeaveRoomActionPerformed(evt);
            }
        });

        btnSendPrivate.setBackground(new java.awt.Color(0, 102, 102));
        btnSendPrivate.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        btnSendPrivate.setForeground(new java.awt.Color(255, 255, 255));
        btnSendPrivate.setText("Send Private");
        btnSendPrivate.setPreferredSize(new java.awt.Dimension(48, 29));
        btnSendPrivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendPrivateActionPerformed(evt);
            }
        });

        btnKick.setBackground(new java.awt.Color(0, 102, 102));
        btnKick.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        btnKick.setForeground(new java.awt.Color(255, 255, 255));
        btnKick.setText("Kick");
        btnKick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKickActionPerformed(evt);
            }
        });

        btnMute.setBackground(new java.awt.Color(0, 102, 102));
        btnMute.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        btnMute.setForeground(new java.awt.Color(255, 255, 255));
        btnMute.setText("Mute");
        btnMute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMuteActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Impact", 0, 14)); // NOI18N
        jLabel1.setText("Room list:");

        jLabel2.setFont(new java.awt.Font("Impact", 0, 14)); // NOI18N
        jLabel2.setText("Online user:");

        jLabel3.setFont(new java.awt.Font("Impact", 0, 32)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 102, 102));
        jLabel3.setText("Chat Room");

        jLabel4.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel4.setText("Type a message:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnCreateRoom, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                        .addComponent(btnLeaveRoom, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnJoinRoom, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(325, 325, 325)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnSendPrivate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnKick, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnMute, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(236, 236, 236)
                .addComponent(jLabel4)
                .addGap(6, 6, 6)
                .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(257, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnCreateRoom, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(9, 9, 9)
                                .addComponent(btnSendPrivate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnKick)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnMute))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnJoinRoom)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnLeaveRoom)))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jLabel4))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private boolean updatingRoom = false;
    private void listRoomsValueChanged(javax.swing.event.ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting() && !updatingRoom) {
            updatingRoom = true;
            try {
                String selectedName = listRooms.getSelectedValue();
                if (selectedName != null) {
                    // Bỏ nhãn (new) khi click vào phòng
                    String cleanName = selectedName.replace(" (new)", "");
                    selectedRoom = roomMap.get(cleanName);

                    if (selectedRoom != null) {
                        int roomId = dbManager.getRoomId(selectedRoom.getName());
                        String chatLogs = dbManager.getChatLogs(roomId);
                        txtChatArea.setText(chatLogs);
                        lastChatLog = chatLogs;
                        updateUsers();

                        // Cập nhật lại tên phòng trên listRooms (xóa (new))
                        DefaultListModel<String> model = (DefaultListModel<String>) listRooms.getModel();
                        for (int i = 0; i < model.size(); i++) {
                            String name = model.getElementAt(i);
                            if (name.equals(selectedName)) {
                                model.set(i, cleanName);
                                break;
                            }
                        }
                    }
                }
            } finally {
                updatingRoom = false;
            }
        }
    }

    private void listUsersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-First:event_listUsersMouseClicked
        if (evt.getClickCount() == 2) {
            String nick = listUsers.getSelectedValue();
            User target = userMap.get(nick);
            if (target != null && !txtMessage.getText().trim().isEmpty()) {
                networkManager.sendPrivate(target.getNickname(), txtMessage.getText());
                txtChatArea.append("[Private to " + target.getNickname() + "]: " + txtMessage.getText() + "\n");
                txtMessage.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user and enter a message");
            }
        }
    }                                      

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        if (selectedRoom != null && !txtMessage.getText().trim().isEmpty()) {
            String message = txtMessage.getText();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String formattedMessage = networkManager.getNickname() + " (" + sdf.format(new java.util.Date()) + "): " + message + "\n";
            txtChatArea.append(formattedMessage);
            txtChatArea.setCaretPosition(txtChatArea.getDocument().getLength());
            lastChatLog = txtChatArea.getText(); 

            networkManager.sendMessage(selectedRoom, message);
            txtMessage.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Please select a room and enter a message");
        }
    }//GEN-LAST:event_btnSendActionPerformed

    private void btnCreateRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateRoomActionPerformed
        CreateRoomDialog frame = new CreateRoomDialog(room -> {
            room.setAdminNick(networkManager.getNickname());
            networkManager.createRoom(room.getName(), room.getMulticastIp(), room.getPort(), networkManager.getNickname());
            updateRooms();
        });
        frame.setVisible(true);
    }//GEN-LAST:event_btnCreateRoomActionPerformed

    private void btnJoinRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-First:event_btnJoinRoomActionPerformed
        if (selectedRoom != null) {
            networkManager.joinRoom(selectedRoom);
            txtChatArea.append("Joined " + selectedRoom.getName() + "\n");
            lastChatLog = txtChatArea.getText(); 
        } else {
            JOptionPane.showMessageDialog(this, "Please select a room");
        }
    }                                           

    private void btnLeaveRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLeaveRoomActionPerformed
        if (selectedRoom != null) {
            networkManager.leaveRoom(selectedRoom);
            txtChatArea.append("Left " + selectedRoom.getName() + "\n");
            selectedRoom = null;
            txtChatArea.setText("");
            lastChatLog = ""; 
            updateUsers();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a room");
        }
    }//GEN-LAST:event_btnLeaveRoomActionPerformed

    private void btnSendPrivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendPrivateActionPerformed
        String nick = listUsers.getSelectedValue();
        User target = userMap.get(nick);
        if (target != null && !txtMessage.getText().trim().isEmpty()) {
            networkManager.sendPrivate(target.getNickname(), txtMessage.getText());
            txtChatArea.append("[Private to " + target.getNickname() + "]: " + txtMessage.getText() + "\n");
            lastChatLog = txtChatArea.getText(); 
            txtMessage.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user and enter a message");
        }
    }//GEN-LAST:event_btnSendPrivateActionPerformed

    private void btnKickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-First:event_btnKickActionPerformed
        if (selectedRoom != null && selectedRoom.getAdminNick().equals(networkManager.getNickname())) {
            String nick = listUsers.getSelectedValue();
            User user = userMap.get(nick);
            if (user != null && !user.getNickname().equals(networkManager.getNickname())) {
                networkManager.kickUser(selectedRoom, user.getNickname());
                txtChatArea.append("Kicked " + user.getNickname() + "\n");
                lastChatLog = txtChatArea.getText(); 
            } else {
                JOptionPane.showMessageDialog(this, "Please select a valid user");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Only room admin can kick users");
        }
    }                                       

    private void btnMuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-First:event_btnMuteActionPerformed
        if (selectedRoom != null && selectedRoom.getAdminNick().equals(networkManager.getNickname())) {
            String nick = listUsers.getSelectedValue();
            User user = userMap.get(nick);
            if (user != null && !user.getNickname().equals(networkManager.getNickname())) {
                networkManager.muteUser(selectedRoom, user.getNickname());
                txtChatArea.append("Muted " + user.getNickname() + "\n");
                lastChatLog = txtChatArea.getText(); 
            } else {
                JOptionPane.showMessageDialog(this, "Please select a valid user");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Only room admin can mute users");
        }
    }                                       
    
    public String getNickname() {
        return networkManager.getNickname();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new LoginForm());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCreateRoom;
    private javax.swing.JButton btnJoinRoom;
    private javax.swing.JButton btnKick;
    private javax.swing.JButton btnLeaveRoom;
    private javax.swing.JButton btnMute;
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnSendPrivate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<String> listRooms;
    private javax.swing.JList<String> listUsers;
    private javax.swing.JTextArea txtChatArea;
    private javax.swing.JTextField txtMessage;
    // End of variables declaration//GEN-END:variables
}