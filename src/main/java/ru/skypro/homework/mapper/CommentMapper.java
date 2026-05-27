package ru.skypro.homework.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.skypro.homework.dto.comments.CommentDto;
import ru.skypro.homework.dto.comments.CommentsDto;
import ru.skypro.homework.dto.comments.CreateOrUpdateCommentDto;
import ru.skypro.homework.model.Advertisements;
import ru.skypro.homework.model.Comment;
import ru.skypro.homework.model.User;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.skypro.homework.dto.comments.CommentDto;
import ru.skypro.homework.dto.comments.CommentsDto;
import ru.skypro.homework.dto.comments.CreateOrUpdateCommentDto;
import ru.skypro.homework.model.Advertisements;
import ru.skypro.homework.model.Comment;
import ru.skypro.homework.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "author.id", target = "author")
    @Mapping(source = "author.firstName", target = "authorFirstName")
    @Mapping(source = "pk", target = "pk")
    @Mapping(source = "createdAt", target = "createdAt")
    CommentDto toCommentDto(Comment comment);

    List<CommentDto> toCommentDtoList(List<Comment> comments);

    default CommentsDto toCommentsDto(List<Comment> comments) {
        CommentsDto commentsDto = new CommentsDto();
        commentsDto.setCount(comments.size());
        commentsDto.setResults(toCommentDtoList(comments));
        return commentsDto;
    }

    @Mapping(target = "pk", ignore = true)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "ad", source = "ad")
    @Mapping(target = "createdAt", ignore = true)
    Comment toEntity(CreateOrUpdateCommentDto dto, User author, Advertisements ad);

    @Mapping(target = "pk", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "ad", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateCommentFromDto(CreateOrUpdateCommentDto dto, @MappingTarget Comment comment);

    @AfterMapping
    default void setAuthorImageUrl(@MappingTarget CommentDto commentDto, Comment comment) {
        User author = comment.getAuthor();
        if (author != null && author.getImage() != null && !author.getImage().isEmpty()) {
            commentDto.setAuthorImage("/images/avatars/" + author.getImage());
        } else {
            commentDto.setAuthorImage(null);
        }
    }
}