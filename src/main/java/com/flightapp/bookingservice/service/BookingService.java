package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.model.Booking;
import com.flightapp.bookingservice.model.BookingStatus;
import com.flightapp.bookingservice.repository.BookingRepository;
import com.flightapp.bookingservice.util.PnrGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepo;

    // WebClient to call FLIGHT SERVICE
    private final WebClient flightClient = WebClient.builder()
            .baseUrl("http://localhost:8081/flights")
            .build();

    // DTO for flight-service response
    public record FlightResponse(
            String id,
            String airline,
            String fromPlace,
            String toPlace,
            String departureTime,
            String arrivalTime,
            String flightDate,
            int totalSeats,
            int price
    ) {}



    /** BOOK A FLIGHT **/
    public Mono<Booking> bookFlight(BookingRequest request) {

        int seatsRequested = request.getPassengers() != null
                ? request.getPassengers().size()
                : request.getSeats();


        // STEP 1 — Fetch flight details
        return flightClient.get()
                .uri("/{id}", request.getFlightId())
                .retrieve()
                .bodyToMono(FlightResponse.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Flight not found")))
                .flatMap(flight -> {

                    if (flight.totalSeats() < seatsRequested) {
                        return Mono.error(new IllegalStateException("Not enough seats available"));
                    }

                    // Journey datetime
                    LocalDate date = LocalDate.parse(flight.flightDate());
                    LocalTime time = LocalTime.parse(flight.departureTime());

                    ZonedDateTime journeyDate = ZonedDateTime.of(date, time, ZoneId.of("Asia/Kolkata"));


                    // STEP 2 — Reserve seats
                    return flightClient.post()
                            .uri("/{id}/reserve?seats=" + seatsRequested, flight.id())
                            .retrieve()
                            .bodyToMono(Void.class)
                            .then(Mono.defer(() -> {

                                // STEP 3 — Save booking
                                Booking booking = new Booking();

                                booking.setFlightId(flight.id());
                                booking.setEmail(request.getEmail());
                                booking.setName(request.getName());
                                booking.setSeats(seatsRequested);
                                booking.setMealType(request.getMealType());
                                booking.setPassengers(request.getPassengers());

                                booking.setBookingDate(ZonedDateTime.now().toString());
                                booking.setJourneyDate(journeyDate.toString());
                                booking.setTotalPrice(seatsRequested * flight.price());
                                booking.setStatus(BookingStatus.BOOKED);

                                booking.setPnr(PnrGenerator.generate(8));

                                return bookingRepo.save(booking);
                            }));
                });
    }



    /** GET BOOKING BY PNR **/
    public Mono<Booking> getByPnr(String pnr) {
        return bookingRepo.findByPnr(pnr);
    }



    /** GET BOOKING HISTORY **/
    public Flux<Booking> historyByEmail(String email) {
        return bookingRepo.findByEmail(email);
    }



    /** CANCEL A BOOKING **/
    public Mono<Booking> cancelByPnr(String pnr) {

        return bookingRepo.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("PNR not found")))
                .flatMap(booking -> {

                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
                    ZonedDateTime journey = ZonedDateTime.parse(booking.getJourneyDate());

                    if (Duration.between(now, journey).toHours() < 24) {
                        return Mono.error(new IllegalStateException("Cannot cancel within 24 hours."));
                    }

                    booking.setStatus(BookingStatus.CANCELLED);


                    // STEP 1 — Release seats
                    return flightClient.post()
                            .uri("/{id}/release?seats=" + booking.getSeats(), booking.getFlightId())
                            .retrieve()
                            .bodyToMono(Void.class)
                            .then(bookingRepo.save(booking));
                });
    }



    /** GET ALL BOOKINGS **/
    public Flux<Booking> allBookings() {
        return bookingRepo.findAll();
    }
}
