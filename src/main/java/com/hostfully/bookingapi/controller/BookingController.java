package com.hostfully.bookingapi.controller;

import com.hostfully.bookingapi.dto.BookingRequest;
import com.hostfully.bookingapi.dto.BookingResponse;
import com.hostfully.bookingapi.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.update(id, request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancel(id));
    }

    @PatchMapping("/{id}/rebook")
    public ResponseEntity<BookingResponse> rebook(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.rebook(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
