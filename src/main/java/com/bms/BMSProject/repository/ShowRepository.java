package com.bms.BMSProject.repository;

import com.bms.BMSProject.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show,Long> {

    List<Show> findByMovieId(Long MovieId);
    List<Show> findByScreenId(Long screenId);
    List<Show> findByMovieIdAndShowDate(Long movieId, LocalDate showDate);

    //  For time conflict check
    List<Show> findByScreenIdAndShowDate(Long screenId, LocalDate date);

}