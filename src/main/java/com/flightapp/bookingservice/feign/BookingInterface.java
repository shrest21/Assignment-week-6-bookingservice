package com.flightapp.bookingservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "FLIGHTSERVICE")
public interface BookingInterface {

    @PostMapping("/flights/{id}/reserve")
    void reserve(@PathVariable String id, @RequestParam int seats);

    @PostMapping("/flights/{id}/release")
    void release(@PathVariable String id, @RequestParam int seats);

    @GetMapping("/flights/{id}")
    String getFlight(@PathVariable String id);
}
