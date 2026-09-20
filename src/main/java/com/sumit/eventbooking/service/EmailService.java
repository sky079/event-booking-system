package com.sumit.eventbooking.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendEventUpdateEmail(String toEmail, String eventName, String changes);
}
