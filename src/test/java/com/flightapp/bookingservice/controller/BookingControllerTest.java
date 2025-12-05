package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.model.Booking;
import com.flightapp.bookingservice.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBook() {
        BookingRequest request = new BookingRequest();
        Booking booking = new Booking();
        when(bookingService.bookFlight(request)).thenReturn(booking);

        Booking result = bookingController.book(request);

        assertThat(result).isEqualTo(booking);
        verify(bookingService, times(1)).bookFlight(request);
    }

    @Test
    void testGetTicket() {
        Booking booking = new Booking();
        when(bookingService.getByPnr("PNR123")).thenReturn(booking);

        Booking result = bookingController.getTicket("PNR123");

        assertThat(result).isEqualTo(booking);
        verify(bookingService, times(1)).getByPnr("PNR123");
    }

    @Test
    void testHistory() {
        List<Booking> bookings = Collections.singletonList(new Booking());
        when(bookingService.historyByEmail("test@example.com")).thenReturn(bookings);

        List<Booking> result = bookingController.bookingHistory("test@example.com");

        assertThat(result).isEqualTo(bookings);
        verify(bookingService, times(1)).historyByEmail("test@example.com");
    }

    @Test
    void testCancel() {
        Booking booking = new Booking();
        when(bookingService.cancelByPnr("PNR123")).thenReturn(booking);

        Booking result = bookingController.cancel("PNR123");

        assertThat(result).isEqualTo(booking);
        verify(bookingService, times(1)).cancelByPnr("PNR123");
    }

    @Test
    void testAllBookings() {
        List<Booking> bookings = Collections.singletonList(new Booking());
        when(bookingService.allBookings()).thenReturn(bookings);

        List<Booking> result = bookingController.allBookings();

        assertThat(result).isEqualTo(bookings);
        verify(bookingService, times(1)).allBookings();
    }
}
