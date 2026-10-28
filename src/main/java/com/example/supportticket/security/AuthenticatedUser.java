package com.example.supportticket.security;

import com.example.supportticket.enums.Role;

public record AuthenticatedUser(Long id, String email, Role role) {
}