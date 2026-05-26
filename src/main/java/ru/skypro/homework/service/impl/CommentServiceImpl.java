package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.dto.comments.CommentDto;
import ru.skypro.homework.dto.comments.CommentsDto;
import ru.skypro.homework.dto.comments.CreateOrUpdateCommentDto;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.model.Advertisements;
import ru.skypro.homework.model.Comment;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.CommentService;

import java.util.List;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentRepository commentRepository,
                              AdRepository adRepository,
                              UserRepository userRepository,
                              CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    public CommentsDto getCommentsByAdId(int adId) {
        log.debug("Getting comments for ad with id: {}", adId);

        if (!adRepository.existsById(adId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found with id: " + adId);
        }

        List<Comment> comments = commentRepository.findByAdPkOrderByCreatedAtDesc(adId);
        return commentMapper.toCommentsDto(comments);
    }

    @Override
    @Transactional
    public CommentDto addComment(int adId, CreateOrUpdateCommentDto dto, String username) {
        log.debug("Adding comment to ad: {} by user: {}", adId, username);


        Advertisements ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ad not found with id: " + adId));


        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + username));


        Comment comment = commentMapper.toEntity(dto, author, ad);
        comment.setCreatedAt(System.currentTimeMillis());

        Comment savedComment = commentRepository.save(comment);
        log.info("Created comment with id: {} for ad: {}", savedComment.getPk(), adId);

        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @commentServiceImpl.isCommentAuthor(#commentId, authentication.name)")
    public void deleteComment(int adId, int commentId, String username) {
        log.debug("Deleting comment: {} from ad: {} by user: {}", commentId, adId, username);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Comment not found with id: " + commentId));

        if (comment.getAd().getPk() != adId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Comment with id: " + commentId + " does not belong to ad with id: " + adId);
        }

        commentRepository.delete(comment);
        log.info("Deleted comment with id: {}", commentId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @commentServiceImpl.isCommentAuthor(#commentId, authentication.name)")
    public CommentDto updateComment(int adId, int commentId, CreateOrUpdateCommentDto dto, String username) {
        log.debug("Updating comment: {} from ad: {} by user: {}", commentId, adId, username);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Comment not found with id: " + commentId));

        if (comment.getAd().getPk() != adId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Comment with id: " + commentId + " does not belong to ad with id: " + adId);
        }

        commentMapper.updateCommentFromDto(dto, comment);

        Comment updatedComment = commentRepository.save(comment);
        log.info("Updated comment with id: {}", commentId);

        return commentMapper.toCommentDto(updatedComment);
    }

    @SuppressWarnings("unused")
    public boolean isCommentAuthor(int commentId, String username) {
        return commentRepository.findById(commentId)
                .map(comment -> comment.getAuthor().getUsername().equals(username))
                .orElse(false);
    }

}

