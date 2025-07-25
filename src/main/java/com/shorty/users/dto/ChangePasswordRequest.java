package com.shorty.users.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) {}
