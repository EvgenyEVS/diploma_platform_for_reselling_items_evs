package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.model.Advertisements;

import java.util.List;

@Repository
public interface AdRepository extends JpaRepository <Advertisements, Integer> {

    List<Advertisements> findByAuthorId(Integer id);
}
