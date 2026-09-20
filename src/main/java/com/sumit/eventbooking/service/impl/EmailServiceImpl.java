package com.sumit.eventbooking.service.impl;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.sumit.eventbooking.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private final Resend resend;

    @Value("${resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    public EmailServiceImpl(@Value("${resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    public void sendEmail(
            String toEmail,
            String eventName,
            String bookingId) {

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Booking Confirmed - " + eventName)
                .html(
                        "<h2>Booking Confirmed</h2>" +
                                "<p>Your booking has been successfully confirmed.</p>" +
                                "<p><strong>Event:</strong> " + eventName + "</p>" +
                                "<p><strong>Booking ID:</strong> " + bookingId + "</p>"
                )
                .build();

        try {
            resend.emails().send(params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send booking confirmation email", e);
        }
    }
}
