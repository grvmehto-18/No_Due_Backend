package com.college.duemanagement.payload.response;

import com.college.duemanagement.entity.Role;
import lombok.Getter;

import java.util.Set;

/**
 * @param token Getters
 */
public record JwtResponse(String token, Long id, String username, String email, String firstName, String lastName,
                          Set<Role> roles, String department, String uniqueCode) {

}