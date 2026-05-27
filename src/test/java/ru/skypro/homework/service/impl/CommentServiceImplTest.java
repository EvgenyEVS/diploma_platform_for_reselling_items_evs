package ru.skypro.homework.service.impl;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void getCommentsByAdId_ShouldReturnComments_WhenAdExists() {
        int adId = 1;
        List<Comment> comments = Arrays.asList(new Comment(), new Comment());
        CommentsDto expectedDto = new CommentsDto();
        expectedDto.setCount(2);

        when(adRepository.existsById(adId)).thenReturn(true);
        when(commentRepository.findByAdPkOrderByCreatedAtDesc(adId)).thenReturn(comments);
        when(commentMapper.toCommentsDto(comments)).thenReturn(expectedDto);

        CommentsDto result = commentService.getCommentsByAdId(adId);

        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(2);
    }

    @Test
    void getCommentsByAdId_ShouldThrowNotFound_WhenAdNotExists() {
        int adId = 999;
        when(adRepository.existsById(adId)).thenReturn(false);

        assertThatThrownBy(() -> commentService.getCommentsByAdId(adId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addComment_ShouldCreateComment_WhenValid() {
        int adId = 1;
        String username = "user@example.com";

        CreateOrUpdateCommentDto dto = new CreateOrUpdateCommentDto();
        dto.setText("Test comment");

        Advertisements ad = new Advertisements();
        ad.setPk(adId);

        User author = new User();
        author.setUsername(username);
        author.setId(1);

        Comment comment = new Comment();
        comment.setText("Test comment");

        Comment savedComment = new Comment();
        savedComment.setPk(1);
        savedComment.setText("Test comment");

        CommentDto expectedDto = new CommentDto();
        expectedDto.setPk(1);
        expectedDto.setText("Test comment");

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(author));
        when(commentMapper.toEntity(dto, author, ad)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(savedComment);
        when(commentMapper.toCommentDto(savedComment)).thenReturn(expectedDto);

        CommentDto result = commentService.addComment(adId, dto, username);

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getPk()).isEqualTo(1);
        verify(commentRepository).save(comment);
    }

    @Test
    void addComment_ShouldThrowNotFound_WhenAdNotExists() {
        int adId = 999;
        String username = "user@example.com";
        CreateOrUpdateCommentDto dto = new CreateOrUpdateCommentDto();
        dto.setText("Test comment");

        when(adRepository.findById(adId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(adId, dto, username))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addComment_ShouldThrowNotFound_WhenUserNotExists() {
        int adId = 1;
        String username = "nonexistent@example.com";
        CreateOrUpdateCommentDto dto = new CreateOrUpdateCommentDto();
        dto.setText("Test comment");

        Advertisements ad = new Advertisements();
        ad.setPk(adId);

        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(adId, dto, username))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteComment_ShouldDelete_WhenUserIsAuthor() {
        int adId = 1;
        int commentId = 1;
        String username = "author@example.com";

        User author = new User();
        author.setUsername(username);

        Advertisements ad = new Advertisements();
        ad.setPk(adId);

        Comment comment = new Comment();
        comment.setPk(commentId);
        comment.setAuthor(author);
        comment.setAd(ad);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(comment);

        commentService.deleteComment(adId, commentId, username);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_ShouldThrowNotFound_WhenCommentNotExists() {
        int adId = 1;
        int commentId = 999;
        String username = "user@example.com";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(adId, commentId, username))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteComment_ShouldThrowNotFound_WhenCommentDoesNotBelongToAd() {
        int adId = 1;
        int commentId = 1;
        String username = "user@example.com";

        Advertisements differentAd = new Advertisements();
        differentAd.setPk(999);

        Comment comment = new Comment();
        comment.setPk(commentId);
        comment.setAd(differentAd);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(adId, commentId, username))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateComment_ShouldUpdate_WhenUserIsAuthor() {
        int adId = 1;
        int commentId = 1;
        String username = "author@example.com";

        CreateOrUpdateCommentDto dto = new CreateOrUpdateCommentDto();
        dto.setText("Updated comment");

        User author = new User();
        author.setUsername(username);

        Advertisements ad = new Advertisements();
        ad.setPk(adId);

        Comment comment = new Comment();
        comment.setPk(commentId);
        comment.setAuthor(author);
        comment.setAd(ad);
        comment.setText("Old comment");

        Comment updatedComment = new Comment();
        updatedComment.setPk(commentId);
        updatedComment.setText("Updated comment");

        CommentDto expectedDto = new CommentDto();
        expectedDto.setPk(commentId);
        expectedDto.setText("Updated comment");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(updatedComment);
        when(commentMapper.toCommentDto(updatedComment)).thenReturn(expectedDto);

        CommentDto result = commentService.updateComment(adId, commentId, dto, username);

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Updated comment");
        verify(commentMapper).updateCommentFromDto(dto, comment);
        verify(commentRepository).save(comment);
    }

    @Test
    void isCommentAuthor_ShouldReturnTrue_WhenUserIsAuthor() {
        int commentId = 1;
        String username = "author@example.com";

        User author = new User();
        author.setUsername(username);

        Comment comment = new Comment();
        comment.setPk(commentId);
        comment.setAuthor(author);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        boolean result = commentService.isCommentAuthor(commentId, username);

        assertThat(result).isTrue();
    }

    @Test
    void isCommentAuthor_ShouldReturnFalse_WhenUserIsNotAuthor() {
        int commentId = 1;
        String username = "other@example.com";

        User author = new User();
        author.setUsername("author@example.com");

        Comment comment = new Comment();
        comment.setPk(commentId);
        comment.setAuthor(author);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        boolean result = commentService.isCommentAuthor(commentId, username);

        assertThat(result).isFalse();
    }

    @Test
    void isCommentAuthor_ShouldReturnFalse_WhenCommentNotFound() {
        int commentId = 999;
        String username = "any@example.com";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        boolean result = commentService.isCommentAuthor(commentId, username);

        assertThat(result).isFalse();
    }
}
