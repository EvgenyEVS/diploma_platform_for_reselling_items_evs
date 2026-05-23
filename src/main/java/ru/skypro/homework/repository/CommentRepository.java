package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByAdPkOrderByCreatedAtDesc(int adPk);

    List<Comment> findByAuthorId(int authorId);

    void deleteByAdPk(int adPk);
}
