package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.email.EmailRequest;
import com.sijan.barberReservation.DTO.email.EmailResponse;
import com.sijan.barberReservation.DTO.email.OtpRequest;
import com.sijan.barberReservation.DTO.email.OtpVerificationRequest;
import com.sijan.barberReservation.service.EmailService;
import com.sijan.barberReservation.service.OtpService;
import com.sijan.barberReservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final OtpService otpService;
    private final UserService userService;

    // 1. Send OTP (Used during Registration)
    @PostMapping("/send-otp")
    public ResponseEntity<EmailResponse> sendOtp(@RequestBody OtpRequest request) {
        // Check if email already exists
        if (userService.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.badRequest()
                    .body(new EmailResponse("Email is already registered. Please login instead.", false));
        }
        
        String otp = otpService.generateOtp(request.getEmail());
        emailService.sendOtpEmail(request.getEmail(), otp);
        return ResponseEntity.ok(new EmailResponse("OTP sent successfully to " + request.getEmail(), true));
    }

    // 2. Verify OTP (Used during Registration)
    @PostMapping("/verify-otp")
    public ResponseEntity<EmailResponse> verifyOtp(@RequestBody OtpVerificationRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (isValid) {
            return ResponseEntity.ok(new EmailResponse("Email verified successfully.", true));
        } else {
            return ResponseEntity.badRequest().body(new EmailResponse("Invalid or expired OTP.", false));
        }
    }

    // 3. Send Registration Confirmation
    @PostMapping("/registration-success")
    public ResponseEntity<EmailResponse> sendRegistrationSuccess(@RequestBody EmailRequest request) {
        emailService.sendRegistrationConfirmation(request.getTo(), request.getUserName());
        return ResponseEntity.ok(new EmailResponse("Registration email sent.", true));
    }

    // 4. Send Application Submission Notification
    @PostMapping("/application-submitted")
    public ResponseEntity<EmailResponse> sendApplicationSubmitted(@RequestBody EmailRequest request) {
        emailService.sendApplicationSubmissionEmail(request.getTo(), request.getShopName());
        return ResponseEntity.ok(new EmailResponse("Application submission email sent.", true));
    }

    // 5. Send Application Status Update (Approved/Rejected)
    @PostMapping("/application-status")
    public ResponseEntity<EmailResponse> sendApplicationStatus(@RequestBody EmailRequest request) {
        emailService.sendApplicationStatusEmail(request.getTo(), request.getShopName(), request.getStatus());
        return ResponseEntity.ok(new EmailResponse("Application status email sent.", true));
    }
}
