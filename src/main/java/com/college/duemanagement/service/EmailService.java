package com.college.duemanagement.service;

import com.college.duemanagement.entity.Due;
import com.college.duemanagement.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendUserCredentials(User user, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(user.getEmail());
            helper.setSubject("Your College Due Management System Credentials");
            
            Context context = new Context();
            context.setVariable("name", user.getFirstName() + " " + user.getLastName());
            context.setVariable("username", user.getUsername());
            context.setVariable("uniqueCode", user.getUniqueCode());
            context.setVariable("password", password);
            
            String content = templateEngine.process("email/credentials", context);
            helper.setText(content, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            // Log the error
            e.printStackTrace();
        }
    }

    @Async
    public void sendPasswordResetLink(User user, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(user.getEmail());
            helper.setSubject("Password Reset Request");
            
            Context context = new Context();
            context.setVariable("name", user.getFirstName() + " " + user.getLastName());
            context.setVariable("resetLink", "http://localhost:3000/reset-password?token=" + resetToken);
            
            String content = templateEngine.process("email/password-reset", context);
            helper.setText(content, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            // Log the error
            e.printStackTrace();
        }
    }

    @Async
    public void sendDueNotification(User student, Due due) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(student.getEmail());
            helper.setSubject("New Due Added: " + due.getDepartment());
            
            Context context = new Context();
            context.setVariable("name", student.getFirstName() + " " + student.getLastName());
            context.setVariable("department", due.getDepartment());
            context.setVariable("description", due.getDescription());
            context.setVariable("amount", due.getAmount());
            context.setVariable("dueDate", due.getDueDate());
            
            String content = templateEngine.process("email/due-notification", context);
            helper.setText(content, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            // Log the error
            e.printStackTrace();
        }
    }

    @Async
    public void sendDueApprovalNotification(User student, Due due) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(student.getEmail());
            helper.setSubject("Due Payment Approved: " + due.getDepartment());
            
            Context context = new Context();
            context.setVariable("name", student.getFirstName() + " " + student.getLastName());
            context.setVariable("department", due.getDepartment());
            context.setVariable("description", due.getDescription());
            context.setVariable("amount", due.getAmount());
            
            String content = templateEngine.process("email/due-approval", context);
            helper.setText(content, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            // Log the error
            e.printStackTrace();
        }
    }

//    @Async
//    public void sendCertificateCompletionNotification(User student, NoDueCertificate certificate) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//            helper.setTo(student.getEmail());
//            helper.setSubject("No Due Certificate Completed");
//
//            Context context = new Context();
//            context.setVariable("name", student.getFirstName() + " " + student.getLastName());
//            context.setVariable("certificateNumber", certificate.getCertificateNumber());
//            context.setVariable("downloadLink", "http://localhost:3000/certificates/" + certificate.getId());
//
//            String content = templateEngine.process("email/certificate-completion", context);
//            helper.setText(content, true);
//
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            // Log the error
//            e.printStackTrace();
//        }
//    }
} 