package com.example.demo.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ChatRoomImpl;
import com.example.demo.service.NotificationImpl;

@RestController
public class AdminController {

    @Autowired
    private ChatRoomImpl chatRoom;

    @Autowired
    private NotificationImpl notification;

    @GetMapping("/notification/push")
    public void pushNotification() {
        notification.push();
    }

    @GetMapping("/chat/hi_to_all")
    public void sayHiToAllUsers() {
        chatRoom.sayHiToAllUsers();
    }
}
