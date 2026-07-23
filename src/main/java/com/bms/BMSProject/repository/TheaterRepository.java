package com.bms.BMSProject.repository;

import com.bms.BMSProject.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheaterRepository extends JpaRepository<Theater,Long> {

    List<Theater> findByCityId(Long cityId);

    boolean existsByNameAndCityId(String name, Long cityId);
}
