package com.hostfully.bookingapi.dto;

import java.time.LocalDate;

public class BlockResponse {

    private Long id;
    private String propertyId;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
