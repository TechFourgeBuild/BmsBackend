package com.bms.BMSProject.repository;

import com.bms.BMSProject.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    List<Booking> findByUserId(Long userId);
    List<Booking> findByShowId(Long showId);

    //find all seat ids that are already booked for given show
    @Query("SELECT s.id FROM Booking b JOIN b.seats s WHERE b.show.id=:showId AND b.status='CONFIRMED'")
    List<Long> findBookedSeatIdsByShowId(@Param("showId") Long showId);
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.show s " +
            "JOIN FETCH s.movie " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theater")
    List<Booking> findAllWithDetails();
}

