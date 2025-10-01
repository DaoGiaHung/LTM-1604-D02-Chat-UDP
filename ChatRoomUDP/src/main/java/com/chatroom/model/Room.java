package com.chatroom.model;

public class Room {
    private String name;
    private String multicastIp;
    private int port;
    private String adminNick;

    public Room(String name, String multicastIp, int port, String adminNick) {
        this.name = name;
        this.multicastIp = multicastIp;
        this.port = port;
        this.adminNick = adminNick;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMulticastIp() { return multicastIp; }
    public void setMulticastIp(String multicastIp) { this.multicastIp = multicastIp; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getAdminNick() { return adminNick; }
    public void setAdminNick(String adminNick) { this.adminNick = adminNick; }

    @Override
    public String toString() {
        return name + " (" + multicastIp + ":" + port + ")";
    }
}