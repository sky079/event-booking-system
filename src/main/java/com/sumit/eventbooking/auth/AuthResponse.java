package com.sumit.eventbooking.auth;

import com.sumit.eventbooking.user.Role;

public record AuthResponse(String token, Long userId, String name, Role role) {}