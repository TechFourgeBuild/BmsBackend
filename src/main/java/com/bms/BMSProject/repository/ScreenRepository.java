package com.bms.BMSProject.repository;

import com.bms.BMSProject.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen,Long> {

    List<Screen> findByTheaterId(Long theaterId);

    boolean existsByTheaterIdAndName(Long theaterId, String name);
}