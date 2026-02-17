package com.hostfully.bookingapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostfully.bookingapi.dto.BlockRequest;
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
class BlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private BlockRequest createBlockRequest(String propertyId, LocalDate start, LocalDate end) {
        BlockRequest request = new BlockRequest();
        request.setPropertyId(propertyId);
        request.setReason("Maintenance");
        request.setStartDate(start);
        request.setEndDate(end);
        return request;
    }

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
    void shouldCreateBlock() throws Exception {
        BlockRequest request = createBlockRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.propertyId").value("prop-1"))
                .andExpect(jsonPath("$.reason").value("Maintenance"));
    }

    @Test
    void shouldUpdateBlock() throws Exception {
        BlockRequest request = createBlockRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        String response = mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        BlockRequest updateRequest = createBlockRequest("prop-1",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(6));
        updateRequest.setReason("Renovation");

        mockMvc.perform(put("/api/blocks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Renovation"));
    }

    @Test
    void shouldDeleteBlock() throws Exception {
        BlockRequest request = createBlockRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        String response = mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/blocks/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectOverlappingBlocks() throws Exception {
        BlockRequest request1 = createBlockRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        BlockRequest request2 = createBlockRequest("prop-1",
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectBlockOverlappingWithBooking() throws Exception {
        BookingRequest bookingRequest = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated());

        BlockRequest blockRequest = createBlockRequest("prop-1",
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blockRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectBookingOverlappingWithBlock() throws Exception {
        BlockRequest blockRequest = createBlockRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blockRequest)))
                .andExpect(status().isCreated());

        BookingRequest bookingRequest = createBookingRequest("prop-1",
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldAllowBookingAfterBlockDeleted() throws Exception {
        BlockRequest blockRequest = createBlockRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        String response = mockMvc.perform(post("/api/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blockRequest)))
                .andReturn().getResponse().getContentAsString();

        Long blockId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/blocks/{id}", blockId));

        BookingRequest bookingRequest = createBookingRequest("prop-1",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn404ForNonExistentBlock() throws Exception {
        mockMvc.perform(delete("/api/blocks/999"))
                .andExpect(status().isNotFound());
    }
}
