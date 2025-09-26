CREATE DATABASE ChatDB;
USE ChatDB;

CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(50),
    ip VARCHAR(20),
    online BOOLEAN
);

CREATE TABLE Rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(50),
    multicast_ip VARCHAR(20),
    port INT,
    admin_nick VARCHAR(50)
);

CREATE TABLE ChatLogs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id INT,
    sender VARCHAR(50),
    message TEXT,
    timestamp DATETIME,
    is_private BOOLEAN,
    FOREIGN KEY (room_id) REFERENCES Rooms(id) ON DELETE CASCADE
);

CREATE TABLE Files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chatlog_id INT,
    file_name VARCHAR(100),
    file_data LONGBLOB,
    FOREIGN KEY (chatlog_id) REFERENCES ChatLogs(id) ON DELETE CASCADE
);