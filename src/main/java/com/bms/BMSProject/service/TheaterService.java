package com.bms.BMSProject.service;

import com.bms.BMSProject.dto.TheaterRequest;
import com.bms.BMSProject.entity.City;
import com.bms.BMSProject.entity.Theater;
import com.bms.BMSProject.exception.DuplicateResourceException;
import com.bms.BMSProject.exception.ResourceNotFoundException;
import com.bms.BMSProject.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final CityService cityService;


    public Theater addTheater(TheaterRequest request)
    {
        City city=cityService.getCityById(request.getCityId());

        // 🟡 Extra: if theater with same name already exists in the city
        if (theaterRepository.existsByNameAndCityId(request.getName(), request.getCityId())) {
            throw new DuplicateResourceException(
                    "Theater with name '" + request.getName() + "' already exists in this city"
            );
        }

        Theater theater=Theater.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(city)
                .build();
        return theaterRepository.save(theater);
    }

    public List<Theater> getAllTheaters()
    {
        return theaterRepository.findAll();
    }

    public Theater getTheaterById(Long id)
    {
        return theaterRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Theater not found with id: " + id));

    }

    public List<Theater> getTheaterByCity(Long cityId)
    {
        return theaterRepository.findByCityId(cityId);
    }
}