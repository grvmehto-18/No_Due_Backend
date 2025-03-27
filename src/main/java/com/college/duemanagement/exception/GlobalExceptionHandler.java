package com.college.duemanagement.exception;

import com.college.duemanagement.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUsernameNotFoundException(Exception ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.builder().
                        success(false).
                        message(ex.getMessage()).
                        data("Username and password does not match").
                        build()
        );
    }

//    @ExceptionHandler(Aut.class)
//    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {
//        return ResponseEntity.badRequest().body(
//                ApiResponse.builder().
//                        success(false).
//                        message(ex.getMessage()).
//                        data("Username and password does not match").
//                        build()
//        );
//    }
}
