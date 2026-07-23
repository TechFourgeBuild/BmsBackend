package com.bms.BMSProject.service;

import com.bms.BMSProject.dto.SeatRequest;
import com.bms.BMSProject.entity.Screen;
import com.bms.BMSProject.entity.Seat;
import com.bms.BMSProject.exception.DuplicateResourceException;
import com.bms.BMSProject.exception.ResourceNotFoundException;
import com.bms.BMSProject.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenService screenService;

    //addSeat
    public Seat addSeat(SeatRequest request) {
        Screen screen = screenService.getScreenById(request.getScreenId());

        // Checking if seat with same number already exists on this screen
        if (seatRepository.existsByScreenIdAndSeatNumber(request.getScreenId(), request.getSeatNumber())) {
            throw new DuplicateResourceException(
                    "Seat with number '" + request.getSeatNumber() + "' already exists on this screen"
            );
        }
        Seat seat = Seat.builder()
                .seatNumber(request.getSeatNumber())
                .row(request.getRow())
                .col(request.getCol())
                .seatType(request.getSeatType())
                .screen(screen)
                .build();
        return seatRepository.save(seat);
    }

    public List<Seat> getSeatsByScreen(Long screenId)
    {
        return seatRepository.findByScreenId(screenId);
    }

    public Seat getSeatById(Long id)
    {
        return seatRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Seat not found with id: " + id));
    }
}
