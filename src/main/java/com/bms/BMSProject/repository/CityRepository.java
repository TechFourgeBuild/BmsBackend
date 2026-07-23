package com.bms.BMSProject.repository;

import com.bms.BMSProject.entity.City; // Adjust the import based on your entity package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    boolean existsByName(String name);

}