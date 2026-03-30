package com.sijan.barberReservation.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    @Qualifier("gmailMailSender")
    private final JavaMailSender mailSender;

    // --- Core Send Method ---

    @Async
    public void sendHtmlEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    // --- Generic Template Builder ---

    private String buildBaseTemplate(String title, String content) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #D4AF37 0%, #F4D03F 100%); padding: 30px; text-align: center; }
                    .header h1 { color: #000000; margin: 0; font-size: 24px; font-weight: 700; }
                    .body { padding: 40px 30px; color: #333333; line-height: 1.6; }
                    .body h2 { color: #000000; font-size: 20px; margin-bottom: 20px; }
                    .info-card { background-color: #f9fafb; border-left: 4px solid #D4AF37; padding: 20px; margin: 20px 0; border-radius: 0 8px 8px 0; }
                    .info-row { display: flex; justify-content: space-between; margin-bottom: 10px; }
                    .info-label { font-weight: 600; color: #6b7280; font-size: 14px; }
                    .info-value { font-weight: 600; color: #111827; font-size: 14px; }
                    .btn { display: inline-block; background-color: #000000; color: #D4AF37; padding: 12px 24px; text-decoration: none; border-radius: 8px; font-weight: 600; margin-top: 20px; }
                    .footer { background-color: #111827; padding: 20px; text-align: center; font-size: 12px; color: #9ca3af; }
                    .footer a { color: #D4AF37; text-decoration: none; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✂️ FadeBook</h1>
                    </div>
                    <div class="body">
                        <h2>""" + title + """
            </h2>
                        """ + content + """
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 FadeBook. All rights reserved.</p>
                        <p>Need help? <a href="mailto:support@fadebook.com">Contact Support</a></p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    // --- 1. AUTH & ACCOUNT ---

    @Async
    public void sendOtpEmail(String to, String otp) {
        String subject = "Your Verification Code";
        String content = """
            <p style="font-size: 16px;">Hello,</p>
            <p>Please use the code below to verify your email address:</p>
            <div style="text-align: center; margin: 30px 0;">
                <span style="font-size: 32px; font-weight: 800; letter-spacing: 5px; color: #D4AF37; background: #f3f4f6; padding: 15px 30px; border-radius: 8px;">
                    """ + otp + """
                </span>
            </div>
            <p style="color: #6b7280; font-size: 14px;">This code expires in <b>5 minutes</b>.</p>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("Verify Your Email", content));
    }

    @Async
    public void sendRegistrationConfirmation(String to, String userName) {
        String subject = "Welcome to FadeBook!";
        String content = """
            <p>Hello <b>""" + userName + """
            </b>,</p>
            <p>Welcome to FadeBook! Your account has been successfully created.</p>
            <a href="#" class="btn">Start Exploring</a>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("Welcome Aboard!", content));
    }

    // 3. PASSWORD RESET
    @Async
    public void sendPasswordResetEmail(String to, String userName, String resetLink) {
        String subject = "Reset Your Password";
        String content = """
            <p>Hello <b>""" + userName + """
            </b>,</p>
            <p>We received a request to reset your password. Click the button below to create a new one:</p>
            <div style="text-align: center; margin: 30px 0;">
                <a href=""" + resetLink + """
        " class="btn">Reset Password</a>
                </div>
                <p style="font-size: 12px; color: #888; margin-top: 20px;">If you did not request this, please ignore this email. This link will expire in 15 minutes.</p>
                """;
            sendHtmlEmail(to, subject, buildBaseTemplate("Password Reset Request", content));
        }
    
        // --- 2. APPLICATIONS ---
    
        @Async
        public void sendApplicationSubmissionEmail(String to, String entityName) {
            String subject = "Application Submitted: " + entityName;
            String content = """
                <p>Hello,</p>
                <p>Thank you for applying to join FadeBook as <b>""" + entityName + """
                </b>.</p>
                <p>Our team is reviewing your application. You will be notified once a decision is made.</p>
                <div class="info-card">
                <div class="info-row"><span class="info-label">Status:</span> <span class="info-value" style="color: #f59e0b;">Pending Review</span></div>
                </div>
                """;
            sendHtmlEmail(to, subject, buildBaseTemplate("Application Received", content));
        }
    
        @Async
        public void sendApplicationStatusEmail(String to, String entityName, String status) {
            String color = "APPROVED".equalsIgnoreCase(status) ? "#10b981" : "#ef4444";
            String message = "APPROVED".equalsIgnoreCase(status) 
                    ? "Congratulations! You can now log in to your dashboard." 
                    : "We regret to inform you that your application has been rejected.";
            
            String subject = "Application Update: " + status;
            String content = """
                <p>Hello,</p>
                <p>Update regarding your application for <b>""" + entityName + """
                </b>.</p>
                <div class="info-card">
                <div class="info-row"><span class="info-label">Status:</span> <span class="info-value" style="color:""" + color + """
            ;">""" + status + """
            </span></div>
            </div>
            <p>""" + message + """
            </p>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("Application Update", content));
    }

    // --- 3. APPOINTMENTS ---

    @Async
    public void sendAppointmentConfirmationCustomer(String to, String customerName, String barberName, String serviceName, String date, String time, String shopName) {
        String subject = "Booking Confirmed: " + serviceName;
        String content = """
            <p>Hello <b>""" + customerName + """
            </b>,</p>
            <p>Your appointment has been successfully booked!</p>
            <div class="info-card">
                <div class="info-row"><span class="info-label">Service:</span> <span class="info-value">""" + serviceName + """
            </span></div>
                <div class="info-row"><span class="info-label">Barber:</span> <span class="info-value">""" + barberName + """
            </span></div>
                <div class="info-row"><span class="info-label">Shop:</span> <span class="info-value">""" + shopName + """
            </span></div>
                <div class="info-row"><span class="info-label">Date:</span> <span class="info-value">""" + date + """
            </span></div>
                <div class="info-row"><span class="info-label">Time:</span> <span class="info-value">""" + time + """
            </span></div>
            </div>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("Appointment Confirmed", content));
    }

    // 2. NEW BOOKING ALERT (Instant for Barber/Admin)
    @Async
    public void sendNewBookingAlert(String to, String barberName, String customerName, String serviceName, String date, String time) {
        String subject = "🔔 New Booking: " + serviceName + " at " + time;
        String content = """
            <p>Hello <b>""" + barberName + """
            </b>,</p>
            <p>You have a new appointment booking!</p>
            <div class="info-card" style="border-left-color: #3b82f6;">
                <div class="info-row"><span class="info-label">Client:</span> <span class="info-value">""" + customerName + """
            </span></div>
                <div class="info-row"><span class="info-label">Service:</span> <span class="info-value">""" + serviceName + """
            </span></div>
                <div class="info-row"><span class="info-label">Date:</span> <span class="info-value">""" + date + """
            </span></div>
                <div class="info-row"><span class="info-label">Time:</span> <span class="info-value">""" + time + """
            </span></div>
            </div>
            <p style="font-size: 12px; color: #888;">Check your dashboard for more details.</p>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("New Appointment", content));
    }

    // 1. 24-HOUR REMINDER
    @Async
    public void sendAppointmentReminder(String to, String customerName, String barberName, String serviceName, String time, String shopName) {
        String subject = "Reminder: Appointment Tomorrow at " + time;
        String content = """
            <p>Hello <b>""" + customerName + """
            </b>,</p>
            <p>This is a friendly reminder that you have an upcoming appointment <b>tomorrow</b>.</p>
            <div class="info-card" style="background-color: #ecfdf5; border-left-color: #10b981;">
                <div class="info-row"><span class="info-label">Service:</span> <span class="info-value">""" + serviceName + """
            </span></div>
                <div class="info-row"><span class="info-label">Barber:</span> <span class="info-value">""" + barberName + """
            </span></div>
                <div class="info-row"><span class="info-label">Time:</span> <span class="info-value">""" + time + """
            </span></div>
                <div class="info-row"><span class="info-label">Location:</span> <span class="info-value">""" + shopName + """
            </span></div>
            </div>
            <p style="color: #6b7280; font-size: 14px;">Please arrive 5 minutes early. See you soon!</p>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("Upcoming Appointment", content));
    }

    @Async
    public void sendAppointmentCancellation(String to, String name, String serviceName, String date, String cancelledBy) {
        String subject = "Appointment Cancelled";
        String content = """
            <p>Hello <b>""" + name + """
            </b>,</p>
            <p>An appointment has been cancelled by <b>""" + cancelledBy + """
            </b>.</p>
            <div class="info-card" style="border-left-color: #ef4444;">
                <div class="info-row"><span class="info-label">Service:</span> <span class="info-value">""" + serviceName + """
            </span></div>
                <div class="info-row"><span class="info-label">Date:</span> <span class="info-value">""" + date + """
            </span></div>
            </div>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("Cancellation Notice", content));
    }

    @Async
    public void sendAppointmentReschedule(String to, String name, String serviceName, String oldDate, String newDate, String newTime) {
        String subject = "Appointment Rescheduled";
        String content = """
            <p>Hello <b>""" + name + """
            </b>,</p>
            <p>An appointment has been rescheduled.</p>
            <div class="info-card">
                <div class="info-row"><span class="info-label">Service:</span> <span class="info-value">""" + serviceName + """
            </span></div>
                <div class="info-row"><span class="info-label">Old Date:</span> <span class="info-value" style="text-decoration: line-through; color:#9ca3af;">""" + oldDate + """
            </span></div>
                <div class="info-row"><span class="info-label">New Date:</span> <span class="info-value" style="color: #10b981;">""" + newDate + """
             at """ + newTime + """
            </span></div>
            </div>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("Reschedule Notice", content));
    }

    // --- 4. REVIEWS ---

    @Async
    public void sendNewReviewNotification(String to, String barberName, String customerName, int rating, String comment) {
        String subject = "New Review Received ⭐";
        String stars = "⭐".repeat(rating);
        String content = """
            <p>Hello <b>""" + barberName + """
            </b>,</p>
            <p>You have received a new review!</p>
            <div class="info-card" style="text-align: center;">
                <p style="font-size: 20px; margin: 0;">""" + stars + """
            </p>
                <p style="font-style: italic; margin-top: 10px; color: #374151;">".""" + comment + """
            "</p>
                <p style="font-size: 12px; color: #6b7280; margin-top: 10px;">- """ + customerName + """
            </p>
            </div>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("New Feedback", content));
    }

    // --- 5. PAYMENTS & PAYOUTS ---

    @Async
    public void sendPaymentConfirmation(String to, String customerName, String serviceName, String amount, String date) {
        String subject = "Payment Successful";
        String content = """
            <p>Hello <b>""" + customerName + """
            </b>,</p>
            <p>Your payment was processed successfully.</p>
            <div class="info-card">
                <div class="info-row"><span class="info-label">Service:</span> <span class="info-value">""" + serviceName + """
            </span></div>
                <div class="info-row"><span class="info-label">Amount:</span> <span class="info-value">""" + amount + """
            </span></div>
                <div class="info-row"><span class="info-label">Date:</span> <span class="info-value">""" + date + """
            </span></div>
            </div>
            <p>Thank you for your business!</p>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("Receipt", content));
    }

    // 6. PAYOUT NOTIFICATION (For Barbers/Admins)
    @Async
    public void sendPayoutConfirmation(String to, String userName, String amount, String bankName, String transactionId) {
        String subject = "💰 Payout Processed: " + amount;
        String content = """
            <p>Hello <b>""" + userName + """
            </b>,</p>
            <p>Great news! Your earnings have been sent to your bank account.</p>
            <div class="info-card" style="background-color: #ecfdf5; border-left-color: #10b981;">
                <div class="info-row"><span class="info-label">Amount:</span> <span class="info-value" style="color: #10b981;">""" + amount + """
            </span></div>
                <div class="info-row"><span class="info-label">Bank:</span> <span class="info-value">""" + bankName + """
            </span></div>
                <div class="info-row"><span class="info-label">Transaction ID:</span> <span class="info-value" style="font-size: 12px;">""" + transactionId + """
            </span></div>
            </div>
            <p style="font-size: 12px; color: #6b7280;">Please allow 1-3 business days for the funds to reflect in your account.</p>
            """;
        sendHtmlEmail(to, subject, buildBaseTemplate("Payout Confirmation", content));
    }
    // --- 6. LEAVE MANAGEMENT ---

    @Async
    public void sendLeaveRequestNotificationAdmin(String adminEmail, String barberName, String startDate, String endDate, String reason) {
        String subject = "📅 New Leave Request: " + barberName;
        String content = """
            <p>Hello Admin,</p>
            <p>A new leave request has been submitted by <b>""" + barberName + """
            </b>.</p>
            <div class="info-card">
                <div class="info-row"><span class="info-label">From:</span> <span class="info-value">""" + startDate + """
        </span></div>
                <div class="info-row"><span class="info-label">To:</span> <span class="info-value">""" + endDate + """
        </span></div>
                <div class="info-row"><span class="info-label">Reason:</span> <span class="info-value">""" + reason + """
        </span></div>
            </div>
            <p>Please review and take action in your dashboard.</p>
            """;
        sendHtmlEmail(adminEmail, subject, buildBaseTemplate("Leave Request Pending", content));
    }

    @Async
    public void sendLeaveApprovalNotification(String barberEmail, String barberName, String startDate, String endDate) {
        String subject = "✅ Leave Request Approved";
        String content = """
            <p>Hello <b>""" + barberName + """
            </b>,</p>
            <p>Good news! Your leave request has been <b style="color: #10b981;">APPROVED</b>.</p>
            <div class="info-card" style="border-left-color: #10b981;">
                <div class="info-row"><span class="info-label">Start Date:</span> <span class="info-value">""" + startDate + """
        </span></div>
                <div class="info-row"><span class="info-label">End Date:</span> <span class="info-value">""" + endDate + """
        </span></div>
            </div>
            <p>Enjoy your time off!</p>
            """;
        sendHtmlEmail(barberEmail, subject, buildBaseTemplate("Leave Approved", content));
    }

    @Async
    public void sendLeaveRejectionNotification(String barberEmail, String barberName, String startDate, String endDate) {
        String subject = "❌ Leave Request Rejected";
        String content = """
            <p>Hello <b>""" + barberName + """
            </b>,</p>
            <p>We regret to inform you that your leave request has been <b style="color: #ef4444;">REJECTED</b>.</p>
            <div class="info-card" style="border-left-color: #ef4444;">
                <div class="info-row"><span class="info-label">Start Date:</span> <span class="info-value">""" + startDate + """
        </span></div>
                <div class="info-row"><span class="info-label">End Date:</span> <span class="info-value">""" + endDate + """
        </span></div>
            </div>
            <p>Please contact your shop administrator for more details.</p>
            """;
        sendHtmlEmail(barberEmail, subject, buildBaseTemplate("Leave Rejected", content));
    }
}