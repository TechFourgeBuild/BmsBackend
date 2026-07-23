package com.bms.BMSProject.controller;


import com.bms.BMSProject.dto.ScreenRequest;
import com.bms.BMSProject.entity.Screen;
import com.bms.BMSProject.service.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    @PostMapping
    public ResponseEntity<Screen> addScreen(@RequestBody ScreenRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(screenService.addScreen(request));
    }

    @GetMapping
    public ResponseEntity<List<Screen>> getAllScreens()
    {
        return ResponseEntity.ok(screenService.getAllScreens());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Screen> getScreenById(@PathVariable Long id)
    {
        return ResponseEntity.ok(screenService.getScreenById(id));
    }

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<Screen>>  getScreensByTheaterId(@PathVariable Long theaterId)
    {
        return ResponseEntity.ok(screenService.getScreensByTheater(theaterId));
    }
}