package com.hostfully.bookingapi.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class BlockRequest {

    @NotBlank(message = "propertyId is required")
    private String propertyId;

    @NotBlank(message = "reason is required")
    private String reason;

    @NotNull(message = "startDate is required")
    @FutureOrPresent(message = "startDate must not be in the past")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    private LocalDate endDate;

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
