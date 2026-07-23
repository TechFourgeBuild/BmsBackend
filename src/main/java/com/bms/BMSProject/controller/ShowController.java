package com.bms.BMSProject.controller;

import com.bms.BMSProject.dto.ShowRequest;
import com.bms.BMSProject.entity.Show;
import com.bms.BMSProject.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @PostMapping
    public ResponseEntity<Show> addShow(@RequestBody ShowRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(showService.addShow(request));
    }

    @GetMapping
    public ResponseEntity<List<Show>> getAllShows()
    {
        return ResponseEntity.ok(showService.getAllShows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Show> getShowById(@PathVariable Long id)
    {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Show>> getShowsByMovies(@PathVariable Long movieId)
    {
        return ResponseEntity.ok(showService.getShowsByMovies(movieId));
    }
    @GetMapping("/movie/{movieId}/date")
    public ResponseEntity<List<Show>>
    getShowsByMoviesAndDate(@PathVariable Long movieId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
    {
        return ResponseEntity.ok(showService.getShowsByMoviesAndDate(movieId,date));
    }
}