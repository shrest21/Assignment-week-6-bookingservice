package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.feign.BookingInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingInterface bookingInterface;

    public void reserveSeat(String flightId, int seats) {
        bookingInterface.reserve(flightId, seats);
    }

    public void releaseSeat(String flightId, int seats) {
        bookingInterface.release(flightId, seats);
    }

    public String fetchFlightDetails(String flightId) {
        return bookingInterface.getFlight(flightId);
    }
}