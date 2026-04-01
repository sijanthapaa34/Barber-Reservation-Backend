package com.sijan.barberReservation.exception.auth;


import com.sijan.barberReservation.exception.appointment.AppointmentAlreadyCancelledException;
import com.sijan.barberReservation.exception.appointment.AppointmentNotFoundException;
import com.sijan.barberReservation.exception.appointment.AppointmentSlotUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password.", request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password.", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> handleDisabledException(DisabledException ex, WebRequest request) {
        return error(HttpStatus.FORBIDDEN, "Your account is disabled.", request);
    }

    @ExceptionHandler(AppointmentAlreadyCancelledException.class)
    public ResponseEntity<Object> handleAlreadyCancelled(AppointmentAlreadyCancelledException ex, WebRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(AppointmentNotFoundException ex, WebRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(AppointmentSlotUnavailableException.class)
    public ResponseEntity<Object> handleSlotUnavailable(AppointmentSlotUnavailableException ex, WebRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex, WebRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<Object> error(HttpStatus status, String message, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, status);
    }
}