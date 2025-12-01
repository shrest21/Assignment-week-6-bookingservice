package com.flightapp.bookingservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Passenger {
    private String name;
    private String gender;
    private int age;
    private String seatNumber;
    private String meal;
}
