package com.flightapp.bookingservice.dto;

import com.flightapp.flightbookingwebflux.model.Passenger;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BookingRequest {
    private String flightId;
    private String name;
    private String email;
    private int seats; // optional if passengers list provided
    private String mealType;
    private List<Passenger> passengers;
}
