package com.flightapp.bookingservice.service;

import com.flightapp.flightbookingwebflux.dto.BookingRequest;
import com.flightapp.flightbookingwebflux.model.Booking;
import com.flightapp.flightbookingwebflux.model.BookingStatus;
import com.flightapp.flightbookingwebflux.repository.BookingRepository;
import com.flightapp.flightbookingwebflux.util.PnrGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepo;
    private final FlightService flightService;

    public Mono<Booking> bookFlight(BookingRequest request) {

        int seatsRequested = request.getPassengers() != null ?
                request.getPassengers().size() : request.getSeats();

        return flightService.getFlightById(request.getFlightId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Flight not found for id: " + request.getFlightId()
                )))
                .flatMap(flight -> {

                    if (flight.getTotalSeats() < seatsRequested) {
                        return Mono.error(new IllegalStateException("Not enough seats available"));
                    }

                    // Build journey datetime
                    LocalDate date = LocalDate.parse(flight.getFlightDate());
                    LocalTime time = LocalTime.parse(flight.getDepartureTime());

                    ZonedDateTime journeyDate = ZonedDateTime.of(
                            date,
                            time,
                            ZoneId.of("Asia/Kolkata")
                    );

                    return flightService.reserveSeats(request.getFlightId(), seatsRequested)
                            .then(Mono.defer(() -> {

                                Booking booking = new Booking();
                                booking.setFlightId(request.getFlightId());
                                booking.setEmail(request.getEmail());
                                booking.setName(request.getName());
                                booking.setSeats(seatsRequested);
                                booking.setMealType(request.getMealType());
                                booking.setPassengers(request.getPassengers());

                                // Save dates as strings to avoid ZonedDateTime codec issues
                                booking.setBookingDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
                                booking.setJourneyDate(journeyDate.toString());

                                booking.setTotalPrice(seatsRequested * flight.getPrice());
                                booking.setStatus(BookingStatus.BOOKED);
                                booking.setPnr(PnrGenerator.generate(8));

                                return bookingRepo.save(booking);
                            }));
                });
    }

    public Mono<Booking> getByPnr(String pnr) {
        return bookingRepo.findByPnr(pnr);
    }

    public Flux<Booking> historyByEmail(String email) {
        return bookingRepo.findByEmail(email);
    }

    public Mono<Booking> cancelByPnr(String pnr) {
        return bookingRepo.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("PNR not found: " + pnr)))
                .flatMap(booking -> {

                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

                    ZonedDateTime journey = booking.getJourneyDate() != null
                            ? ZonedDateTime.parse(booking.getJourneyDate())
                            : null;

                    if (journey != null && Duration.between(now, journey).toHours() < 24) {
                        return Mono.error(new IllegalStateException(
                                "Cannot cancel less than 24 hours before journey"
                        ));
                    }

                    booking.setStatus(BookingStatus.CANCELLED);

                    return flightService.releaseSeats(booking.getFlightId(), booking.getSeats())
                            .then(bookingRepo.save(booking));
                });
    }

    public Flux<Booking> allBookings() {
        return bookingRepo.findAll();
    }
}
