package com.codeforces.analyzer.controller;

import java.util.List;

import com.codeforces.analyzer.model.Platform;
import com.codeforces.analyzer.model.User;
import com.codeforces.analyzer.service.UserService;

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public User addUser(String handle, Platform platform) {
        return userService.addUser(handle, platform);
    }

    public void updateUser(long id, String handle, Platform platform) {
        userService.updateUser(id, handle, platform);
    }

    public void deleteUser(long id) {
        userService.deleteUser(id);
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public List<User> searchUsers(String keyword) {
        return userService.searchUsers(keyword);
    }
}
