package com.bms.BMSProject.repository;

import com.bms.BMSProject.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat,Long> {

    List<Seat> findByScreenId(Long screenId);

    boolean existsByScreenIdAndSeatNumber(Long screenId, String seatNumber);
}
