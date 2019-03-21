package com.example.demo.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.proto.notification.User;
import com.example.demo.service.ChatRoomImpl;
import com.example.demo.service.NotificationImpl;

@RestController
public class AdminController {

    @Autowired
    private ChatRoomImpl chatRoom;

    @Autowired
    private NotificationImpl notification;

    @GetMapping("/notification/push")
    public Map<String, Object> pushNotification() {
        return Collections.singletonMap("pushedUsersCount", notification.push());
    }

    @GetMapping("/notification/users")
    public Map<String, Object> getConnectedUsers() {

        Map<String, Object> json = new HashMap<>(2);

        List<String> users = notification.getConnectedUsers().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        json.put("totalUserCounts", users.size());
        json.put("users", users);

        return json;
    }

    @GetMapping("/notification/startPing")
    public Map<String, Object> startPing(@RequestParam(value = "interval", defaultValue = "10", required = false) int interval) {

        notification.startPing(interval);

        Map<String, Object> json = new HashMap<>(2);

        json.put("success", true);
        json.put("interval", interval);

        return json;
    }

    @GetMapping("/notification/stopPing")
    public Map<String, Object> stopPing() {
        notification.stopPing();
        return Collections.singletonMap("success", true);
    }

    @GetMapping("/chat/hi_to_all")
    public void sayHiToAllUsers() {
        chatRoom.sayHiToAllUsers();
    }
}
