package com.college.duemanagement.response;


import lombok.Builder;
import lombok.Getter;

/**
 * @param success Getters
 */
@Builder
public record ApiResponse<T>(boolean success, String message, T data) {

}
