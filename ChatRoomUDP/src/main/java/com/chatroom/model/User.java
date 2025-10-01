package com.chatroom.model;

public class User {
    private String nickname;
    private String ip;
    private boolean online;

    public User(String nickname, String ip, boolean online) {
        this.nickname = nickname;
        this.ip = ip;
        this.online = online;
    }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    @Override
    public String toString() {
        return nickname + " (" + ip + ")";
    }
}