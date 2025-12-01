package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.model.Booking;
import com.flightapp.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/booking")
public class BookingController {

    private final BookingService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking book(@RequestBody BookingRequest request) {
        return service.bookFlight(request);
    }

    @GetMapping("/ticket/{pnr}")
    public Booking getTicket(@PathVariable String pnr) {
        return service.getByPnr(pnr);
    }

    @GetMapping("/history/{email}")
    public List<Booking> bookingHistory(@PathVariable String email) {
        return service.historyByEmail(email);
    }

    @DeleteMapping("/cancel/{pnr}")
    public Booking cancel(@PathVariable String pnr) {
        return service.cancelByPnr(pnr);
    }

    @GetMapping
    public List<Booking> allBookings() {
        return service.allBookings();
    }
}
