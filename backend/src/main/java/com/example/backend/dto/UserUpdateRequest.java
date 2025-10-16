package com.example.backend.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String fullName;
    private String bio;
}
