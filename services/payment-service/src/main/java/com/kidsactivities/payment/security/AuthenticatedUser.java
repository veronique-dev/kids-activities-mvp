package com.kidsactivities.payment.security;

import com.kidsactivities.common.model.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthenticatedUser {
    private final Long id;
    private final String email;
    private final Role role;
}
