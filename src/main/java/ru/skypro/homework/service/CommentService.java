package ru.skypro.homework.service;

import ru.skypro.homework.dto.comments.CommentDto;
import ru.skypro.homework.dto.comments.CommentsDto;
import ru.skypro.homework.dto.comments.CreateOrUpdateCommentDto;

public interface CommentService {

    CommentsDto getCommentsByAdId(int adId);

    CommentDto addComment(int adId, CreateOrUpdateCommentDto dto, String username);

    void deleteComment(int adId, int commentId, String username);

    CommentDto updateComment(int adId, int commentId, CreateOrUpdateCommentDto dto, String username);
}
