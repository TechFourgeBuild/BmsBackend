package com.bms.BMSProject.controller;

import com.bms.BMSProject.dto.TheaterRequest;
import com.bms.BMSProject.entity.Theater;
import com.bms.BMSProject.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    //post add
    @PostMapping
    public ResponseEntity<Theater> addTheater(@RequestBody TheaterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(theaterService.addTheater(request));
    }

    @GetMapping
    public ResponseEntity<List<Theater>> getAllTheaters()
    {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Theater> getTheaterById(@PathVariable Long id)
    {
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<Theater>> getTheaterByCity(@PathVariable Long cityId)
    {
        return ResponseEntity.ok(theaterService.getTheaterByCity(cityId));
    }

}