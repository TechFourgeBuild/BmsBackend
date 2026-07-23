package com.bms.BMSProject.controller;

import com.bms.BMSProject.entity.City;
import com.bms.BMSProject.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @PostMapping
    public ResponseEntity<City> addCity(@RequestBody City city) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cityService.addCity(city));
    }

    @GetMapping
    public ResponseEntity<List<City>> getAllCities()
    {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Long id)
    {
        return ResponseEntity.ok(cityService.getCityById(id));
    }
}