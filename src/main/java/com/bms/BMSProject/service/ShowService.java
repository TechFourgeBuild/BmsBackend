package com.bms.BMSProject.service;

import com.bms.BMSProject.dto.ShowRequest;
import com.bms.BMSProject.entity.Movie;
import com.bms.BMSProject.entity.Screen;
import com.bms.BMSProject.entity.Show;
import com.bms.BMSProject.exception.BookingException;
import com.bms.BMSProject.exception.ResourceNotFoundException;
import com.bms.BMSProject.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieService movieService;
    private final ScreenService screenService;

    //addshow
    public Show addShow(ShowRequest request)
    {
        Movie movie =movieService.getMovieById(request.getMovieId());
        Screen screen=screenService.getScreenById(request.getScreenId());

        // 🟡 Checking time conflict on same screen
        if (isShowTimeConflict(screen.getId(), request.getShowDate(),
                request.getStartTime(), request.getEndTime())) {
            throw new BookingException(
                    "Show timings conflict on screen '" + screen.getName() +
                            "' on date " + request.getShowDate()
            );
        }

        // 🟡 Extra: Check if end time is after start time
        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().equals(request.getStartTime())) {
            throw new BookingException("End time must be after start time");
        }
        
        Show show=Show.builder()
                .movie(movie)
                .screen(screen)
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .ticketPrice(request.getTicketPrice())
                .build();

        return showRepository.save(show);
    }

    private boolean isShowTimeConflict(Long screenId, LocalDate date,
                                       LocalTime startTime, LocalTime endTime) {
        List<Show> existingShows = showRepository.findByScreenIdAndShowDate(screenId, date);

        for (Show existingShow : existingShows) {
            // ✅ Direct LocalTime comparison
            boolean overlaps = (startTime.isBefore(existingShow.getEndTime()) &&
                    endTime.isAfter(existingShow.getStartTime()));
            if (overlaps) {
                return true;
            }
        }
        return false;
    }

    public List<Show> getAllShows()
    {
        return showRepository.findAll();
    }
    public Show getShowById(Long id)
    {
        return showRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Show not found with id: " + id));

    }

    public List<Show> getShowsByMovies(Long movieId)
    {
        return showRepository.findByMovieId(movieId);
    }

    public List<Show> getShowsByMoviesAndDate(Long movieId, LocalDate date)
    {
        return showRepository.findByMovieIdAndShowDate(movieId,date);
    }

    //getShowByScreen
    public List<Show> getShowByScreen(Long screenId) {
        return showRepository.findByScreenId(screenId);
    }
}