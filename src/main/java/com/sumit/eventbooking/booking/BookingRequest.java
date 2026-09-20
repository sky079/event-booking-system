package com.sumit.eventbooking.booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record BookingRequest(@Min(1) @Max(10) int quantity) {}