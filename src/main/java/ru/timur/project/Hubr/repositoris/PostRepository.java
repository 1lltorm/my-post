package ru.timur.project.Hubr.repositoris;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.Tag;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    ArrayList<Post> findPostsByTagListContaining(Tag tag);

    List<Post> findPostsByActive(boolean b);
}
