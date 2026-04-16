package com.aurum.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/v1/user/profile")
    public String getProfile() {
        return "This is protected data 🔐";
    }
}