package com.kidsactivities.auth.controller;

import com.kidsactivities.auth.service.UserService;
import com.kidsactivities.common.dto.UserSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/users/{id}")
    public UserSnapshot getUserById(@PathVariable Long id) {
        return userService.getUserSnapshot(id);
    }

    @GetMapping("/stats/count")
    public long getUserCount() {
        return userService.countUsers();
    }
}
