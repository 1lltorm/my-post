package ru.timur.project.Hubr.repositoris;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.timur.project.Hubr.models.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
}
