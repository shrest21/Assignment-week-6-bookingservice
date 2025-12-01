package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.feign.BookingInterface;
import com.flightapp.bookingservice.model.Booking;
import com.flightapp.bookingservice.model.BookingStatus;
import com.flightapp.bookingservice.repository.BookingRepository;
import com.flightapp.bookingservice.util.PnrGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepo;
    private final BookingInterface flightClient;   // Feign client

    public Mono<Booking> bookFlight(BookingRequest request) {

        int seatCount = (request.getPassengers() != null && !request.getPassengers().isEmpty())
                ? request.getPassengers().size()
                : request.getSeats();

        String flightJson = flightClient.getFlight(request.getFlightId());

        if (flightJson == null || flightJson.isEmpty()) {
            return Mono.error(new RuntimeException("Flight not found"));
        }

        flightClient.reserve(request.getFlightId(), seatCount);

        Booking booking = new Booking();
        booking.setPnr(PnrGenerator.generate(6));
        booking.setFlightId(request.getFlightId());
        booking.setSeats(seatCount);
        booking.setName(request.getName());
        booking.setEmail(request.getEmail());
        booking.setMealType(request.getMealType());
        booking.setPassengers(request.getPassengers());
        booking.setStatus(BookingStatus.BOOKED);
        booking.setBookingDate(LocalDateTime.now().toString());
        booking.setJourneyDate(LocalDateTime.now().toString());
        booking.setTotalPrice(0);

        return bookingRepo.save(booking);
    }

    public Mono<Booking> getByPnr(String pnr) {
        return bookingRepo.findByPnr(pnr);
    }

    public Flux<Booking> historyByEmail(String email) {
        return bookingRepo.findByEmail(email);
    }

    public Flux<Booking> allBookings() {
        return bookingRepo.findAll();
    }

    public Mono<Booking> cancelByPnr(String pnr) {
        return bookingRepo.findByPnr(pnr)
                .flatMap(booking -> {

                    // Release seats
                    flightClient.release(booking.getFlightId(), booking.getSeats());

                    booking.setStatus(BookingStatus.CANCELLED);
                    booking.setBookingDate(LocalDateTime.now().toString());

                    return bookingRepo.save(booking);
                });
    }
}
