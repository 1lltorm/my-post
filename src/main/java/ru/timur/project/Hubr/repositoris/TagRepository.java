package ru.timur.project.Hubr.repositoris;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.Tag;
import ru.timur.project.Hubr.models.User;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    List<Tag> findByNameContainingIgnoreCase(String name);
    List<Tag> findByActive(boolean active);
}
