package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.model.Booking;
import com.flightapp.bookingservice.service.BookingService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/booking")
public class BookingController {

    private final BookingService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Booking> book(@RequestBody BookingRequest request) {

        return service.bookFlight(request);
    }

    @GetMapping("/ticket/{pnr}")
    public Mono<Booking> getTicket(@PathVariable String pnr) {

        return service.getByPnr(pnr);
    }

    @GetMapping("/history/{email}")
    public Flux<Booking> bookingHistory(@PathVariable String email) {

        return service.historyByEmail(email);
    }

    @DeleteMapping("/cancel/{pnr}")
    public Mono<Booking> cancel(@PathVariable String pnr) {

        return service.cancelByPnr(pnr);
    }

    @GetMapping
    public Flux<Booking> allBookings() {
        return service.allBookings();
    }
}
