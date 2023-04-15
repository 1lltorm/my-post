package ru.timur.project.Hubr.services;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.timur.project.Hubr.models.Comment;
import ru.timur.project.Hubr.repositoris.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    @Transactional
    public void save(Comment comment) {
        enrich(comment);
        comment.setBody(markdownToHTML(comment.getBody()));
        commentRepository.save(comment);
    }

    public Comment findById(int id) {
        return commentRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteById(int id) {
        commentRepository.deleteById(id);
    }

    //Markdown
    private String markdownToHTML(String markdown) {
        Parser parser = Parser.builder().build();

        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        return renderer.render(document);
    }

    private void enrich(Comment comment) {
        comment.setCreatedAt(LocalDateTime.now());
    }

}
