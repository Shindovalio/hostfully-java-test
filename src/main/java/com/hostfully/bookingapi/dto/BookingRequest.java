package com.hostfully.bookingapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class BookingRequest {

    @NotBlank(message = "propertyId is required")
    private String propertyId;

    @NotBlank(message = "guestName is required")
    private String guestName;

    @NotBlank(message = "guestEmail is required")
    @Email(message = "guestEmail must be a valid email")
    private String guestEmail;

    @NotNull(message = "startDate is required")
    @FutureOrPresent(message = "startDate must not be in the past")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    private LocalDate endDate;

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
