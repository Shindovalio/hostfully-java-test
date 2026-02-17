package com.hostfully.bookingapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostfully.bookingapi.dto.BookingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingRequest createBookingRequest(String propertyId, LocalDate start, LocalDate end) {
        BookingRequest request = new BookingRequest();
        request.setPropertyId(propertyId);
        request.setGuestName("John Doe");
        request.setGuestEmail("john@example.com");
        request.setStartDate(start);
        request.setEndDate(end);
        return request;
    }

    @Test
    void shouldCreateBooking() throws Exception {
        BookingRequest request = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value("prop-1"))
                .andExpect(jsonPath("$.guestName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldGetBookingById() throws Exception {
        BookingRequest request = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/bookings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestName").value("John Doe"));
    }

    @Test
    void shouldReturn404ForNonExistentBooking() throws Exception {
        mockMvc.perform(get("/api/bookings/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateBooking() throws Exception {
        BookingRequest request = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        BookingRequest updateRequest = createBookingRequest("prop-1",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(6));
        updateRequest.setGuestName("Jane Doe");
        updateRequest.setGuestEmail("jane@example.com");

        mockMvc.perform(put("/api/bookings/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestName").value("Jane Doe"))
                .andExpect(jsonPath("$.guestEmail").value("jane@example.com"));
    }

    @Test
    void shouldCancelBooking() throws Exception {
        BookingRequest request = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/bookings/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldRebookCanceledBooking() throws Exception {
        BookingRequest request = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/bookings/{id}/cancel", id));

        mockMvc.perform(patch("/api/bookings/{id}/rebook", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldDeleteBooking() throws Exception {
        BookingRequest request = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/bookings/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/bookings/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectOverlappingBookings() throws Exception {
        BookingRequest request1 = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        BookingRequest request2 = createBookingRequest("prop-1",
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldAllowBookingOnDifferentProperty() throws Exception {
        BookingRequest request1 = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        BookingRequest request2 = createBookingRequest("prop-2",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectInvalidDates() throws Exception {
        BookingRequest request = createBookingRequest("prop-1",
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingFields() throws Exception {
        BookingRequest request = new BookingRequest();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowBookingAfterCancelOverlapping() throws Exception {
        BookingRequest request1 = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/bookings/{id}/cancel", id));

        BookingRequest request2 = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
    }
}
