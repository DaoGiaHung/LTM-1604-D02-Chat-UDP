CREATE DATABASE ChatDB;
Go
USE ChatDB;
Go
CREATE TABLE Users (id INT PRIMARY KEY IDENTITY, nickname VARCHAR(50), ip VARCHAR(20), online BIT);
CREATE TABLE Rooms (id INT PRIMARY KEY IDENTITY, room_name VARCHAR(50), multicast_ip VARCHAR(20), port INT, admin_nick VARCHAR(50));
CREATE TABLE ChatLogs (id INT PRIMARY KEY IDENTITY, room_id INT, sender VARCHAR(50), message TEXT, timestamp DATETIME, is_private BIT);
CREATE TABLE Files (id INT PRIMARY KEY IDENTITY, chatlog_id INT, file_name VARCHAR(100), file_data VARBINARY(MAX));