package com.sumit.eventbooking.model.auth;

import com.sumit.eventbooking.model.user.Role;

public record AuthResponse(String token, Long userId, String name, Role role) {}