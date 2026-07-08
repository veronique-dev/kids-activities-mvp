package com.kidsactivities.gateway.controller;

import com.kidsactivities.gateway.dto.AdminDashboardResponse;
import com.kidsactivities.gateway.service.AdminDashboardAggregator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminDashboardAggregator adminDashboardAggregator;

    @GetMapping("/dashboard")
    public Mono<AdminDashboardResponse> getDashboard() {
        return adminDashboardAggregator.getDashboard();
    }
}
