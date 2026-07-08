package com.kidsactivities.common.dto;

import com.kidsactivities.common.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSnapshot {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
