package com.flightapp.bookingservice.repository;

import com.flightapp.flightbookingwebflux.model.Booking;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingRepository extends ReactiveMongoRepository<Booking, String> {
    Mono<Booking> findByPnr(String pnr);
    Flux<Booking> findByEmail(String email);
}
