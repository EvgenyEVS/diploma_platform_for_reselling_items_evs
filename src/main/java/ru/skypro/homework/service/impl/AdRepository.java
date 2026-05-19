package ru.skypro.homework.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.model.Advertisements;

@Repository
public interface AdRepository extends JpaRepository <Advertisements, Integer> {
}
