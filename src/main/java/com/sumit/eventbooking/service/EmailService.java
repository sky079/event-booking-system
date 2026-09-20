package com.sumit.eventbooking.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
