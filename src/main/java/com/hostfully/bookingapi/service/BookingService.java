package com.hostfully.bookingapi.service;

import com.hostfully.bookingapi.dto.BookingRequest;
import com.hostfully.bookingapi.dto.BookingResponse;
import com.hostfully.bookingapi.entity.Booking;
import com.hostfully.bookingapi.enums.BookingStatus;
import com.hostfully.bookingapi.exception.OverlapException;
import com.hostfully.bookingapi.exception.ResourceNotFoundException;
import com.hostfully.bookingapi.repository.BlockRepository;
import com.hostfully.bookingapi.repository.BookingRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BlockRepository blockRepository;

    public BookingService(BookingRepository bookingRepository, BlockRepository blockRepository) {
        this.bookingRepository = bookingRepository;
        this.blockRepository = blockRepository;
    }

    public BookingResponse create(BookingRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        checkForOverlaps(request.getPropertyId(), request.getStartDate(), request.getEndDate(), null);

        Booking reserva = new Booking();
        reserva.setPropertyId(request.getPropertyId());
        reserva.setGuestName(request.getGuestName());
        reserva.setGuestEmail(request.getGuestEmail());
        reserva.setStartDate(request.getStartDate());
        reserva.setEndDate(request.getEndDate());
        reserva.setStatus(BookingStatus.ACTIVE);

        return toResponse(bookingRepository.save(reserva));
    }

    public BookingResponse getById(Long id) {
        return toResponse(findBookingOrThrow(id));
    }

    public BookingResponse update(Long id, BookingRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        Booking booking = findBookingOrThrow(id);

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new IllegalArgumentException("Cannot update a canceled booking");
        }

        checkForOverlaps(request.getPropertyId(), request.getStartDate(), request.getEndDate(), id);

        booking.setPropertyId(request.getPropertyId());
        booking.setGuestName(request.getGuestName());
        booking.setGuestEmail(request.getGuestEmail());
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());

        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponse cancel(Long id) {
        Booking booking = findBookingOrThrow(id);

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new IllegalArgumentException("Booking is already canceled");
        }

        booking.setStatus(BookingStatus.CANCELED);
        return toResponse(bookingRepository.save(booking));
    }

    public BookingResponse rebook(Long id) {
        Booking booking = findBookingOrThrow(id);

        if (booking.getStatus() != BookingStatus.CANCELED) {
            throw new IllegalArgumentException("Only canceled bookings can be rebooked");
        }

        checkForOverlaps(booking.getPropertyId(), booking.getStartDate(), booking.getEndDate(), id);

        booking.setStatus(BookingStatus.ACTIVE);
        return toResponse(bookingRepository.save(booking));
    }

    public void delete(Long id) {
        Booking booking = findBookingOrThrow(id);
        bookingRepository.delete(booking);
    }

    private void checkForOverlaps(String propertyId, java.time.LocalDate startDate,
                                  java.time.LocalDate endDate, Long excludeBookingId) {
        List<Booking> overlappingBookings = bookingRepository.findOverlapping(
                propertyId, startDate, endDate, BookingStatus.ACTIVE);

        if (excludeBookingId != null) {
            overlappingBookings = overlappingBookings.stream()
                    .filter(b -> !b.getId().equals(excludeBookingId))
                    .toList();
        }

        if (!overlappingBookings.isEmpty()) {
            throw new OverlapException("Booking overlaps with an existing booking");
        }

        if (!blockRepository.findOverlapping(propertyId, startDate, endDate).isEmpty()) {
            throw new OverlapException("Booking overlaps with an existing block");
        }
    }

    private void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
    }

    private Booking findBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    private BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setPropertyId(booking.getPropertyId());
        response.setGuestName(booking.getGuestName());
        response.setGuestEmail(booking.getGuestEmail());
        response.setStartDate(booking.getStartDate());
        response.setEndDate(booking.getEndDate());
        response.setStatus(booking.getStatus());
        return response;
    }
}
