package com.AttendPulse.attend_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.io.UnsupportedEncodingException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String mailUsername;

    public void sendAttendanceWarning(String toEmail,
                                      String studentName,
                                      String subjectName,
                                      double percentage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setFrom(mailUsername, "AttendPulse");
            message.setSubject("⚠️ Low Attendance Warning - " + subjectName);
            message.setText(
                    "Dear " + studentName + ",\n\n" +
                            "Your attendance in " + subjectName + " has dropped to " +
                            percentage + "%.\n\n" +
                            "Minimum required attendance is 75%.\n\n" +
                            "Please attend classes regularly to avoid issues.\n\n" +
                            "Regards,\nAttendPulse System"
            );
            mailSender.send(message);
        }catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }

    public void sendApprovalNotification(String toEmail, String studentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setFrom(mailUsername, "AttendPulse");
            message.setSubject("Account Approved - AttendPulse");
            message.setText(
                    "Dear " + studentName + ",\n\n" +
                            "Your AttendPulse account has been approved by your teacher!\n\n" +
                            "You can now login at: http://localhost:5173\n\n" +
                            "Regards,\nAttendPulse System"
            );
            mailSender.send(message);
        }catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }

    public void sendRejectionNotification(String toEmail, String studentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setFrom(mailUsername, "AttendPulse");
            message.setSubject("Account Rejected - AttendPulse");
            message.setText(
                    "Dear " + studentName + ",\n\n" +
                            "Your AttendPulse account registration has been rejected.\n\n" +
                            "Please contact your teacher for more information.\n\n" +
                            "Regards,\nAttendPulse System"
            );
            mailSender.send(message);
        }catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }
    }

    public void sendOtpEmail(String toEmail, String studentName, String otp, Long sessionId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setFrom(mailUsername, "AttendPulse");
            helper.setSubject("Attendance OTP - AttendPulse");
            helper.setText("Dear " + studentName + ",\n\nSession ID: " + sessionId + "\nYour OTP is: " + otp + "\nValid for 10 minutes.", false);
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("OTP Email failed: " + e.getMessage());
        }
    }

    public void sendRegistrationPending(String toEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setFrom(mailUsername, "AttendPulse");
            message.setSubject("Registration Pending Approval");
            message.setText(
                    "Dear " + name + ",\n\n" +
                            "Your account is pending approval.\n\n" +
                            "Regards,\nAttendPulse"
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Registration Email failed: " + e.getMessage());
        }
    }
}