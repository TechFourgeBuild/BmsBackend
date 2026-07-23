package com.bms.BMSProject.service;


import com.bms.BMSProject.dto.ScreenRequest;
import com.bms.BMSProject.entity.Screen;
import com.bms.BMSProject.entity.Theater;
import com.bms.BMSProject.exception.DuplicateResourceException;
import com.bms.BMSProject.exception.ResourceNotFoundException;
import com.bms.BMSProject.repository.ScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterService theaterService;

    //addscreen
    public Screen addScreen(ScreenRequest request) {
        Theater theater = theaterService
                .getTheaterById(request.getTheaterId());

        if (screenRepository.existsByTheaterIdAndName(request.getTheaterId(), request.getName())) {
            throw new DuplicateResourceException(
                    "Screen with name '" + request.getName() + "' already exists in this theater"
            );
        }

        Screen screen = Screen.builder()
                .name(request.getName())
                .totalSeats(request.getTotalSeats())
                .theater(theater)
                .build();
        return screenRepository.save(screen);
    }

    public List<Screen> getAllScreens()
    {
        return screenRepository.findAll();
    }

    public Screen getScreenById(Long id)
    {
        return screenRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Screen not found with id: " + id));
    }

    public List<Screen> getScreensByTheater(Long theaterId)
    {
        return screenRepository.findByTheaterId(theaterId);
    }
}