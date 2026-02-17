package com.hostfully.bookingapi.dto;

import com.hostfully.bookingapi.enums.BookingStatus;
import java.time.LocalDate;

public class BookingResponse {

    private Long id;
    private String propertyId;
    private String guestName;
    private String guestEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private BookingStatus status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}
