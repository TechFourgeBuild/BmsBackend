package com.bms.BMSProject.service;

import com.bms.BMSProject.entity.Movie;
import com.bms.BMSProject.exception.DuplicateResourceException;
import com.bms.BMSProject.exception.ResourceNotFoundException;
import com.bms.BMSProject.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public Movie addMovie(Movie movie)
    {
        if (movieRepository.existsByTitle(movie.getTitle())) {
            throw new DuplicateResourceException(
                    "Movie with title '" + movie.getTitle() + "' already exists"
            );
        }
        return movieRepository.save(movie);
    }

    public List<Movie> getAllMovies()
    {
        return movieRepository.findAll();
    }

    public Movie getMovieById(Long id)
    {
        return movieRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Movie not found with id: " + id));

    }

    public List<Movie> searchByTitle(String title){
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Movie> getByGenre(String genre){
        return movieRepository.findByGenre(genre);
    }

    public List<Movie> getByLanguage(String language){
        return movieRepository.findByLanguage(language);
    }

    //update movie
    public Movie updateMovie(Long id, Movie movie) {
        Movie existing = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        existing.setTitle(movie.getTitle());
        existing.setDescription(movie.getDescription());
        existing.setGenre(movie.getGenre());
        existing.setLanguage(movie.getLanguage());
        existing.setRating(movie.getRating());
        existing.setDurationMinutes(movie.getDurationMinutes());
        existing.setReleaseDate(movie.getReleaseDate());
        existing.setPosterUrl(movie.getPosterUrl());
        return movieRepository.save(existing);
    }

    //delete movie
    public void deleteMovie(Long id) {
        Movie existing = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movieRepository.delete(existing);
    }

}