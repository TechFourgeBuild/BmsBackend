package com.bms.BMSProject.service;

import com.bms.BMSProject.entity.City;
import com.bms.BMSProject.exception.DuplicateResourceException;
import com.bms.BMSProject.exception.ResourceNotFoundException;
import com.bms.BMSProject.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    public City addCity(City city)
    {
        // Checking if city with same name already exists
        if (cityRepository.existsByName(city.getName())) {
            throw new DuplicateResourceException(
                    "City with name '" + city.getName() + "' already exists"
            );
        }
        return cityRepository.save(city);
    }

    public List<City> getAllCities()
    {
        return cityRepository.findAll();
    }

    public City getCityById(Long id)
    {
        return cityRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("City not found with id: " + id));
    }
}