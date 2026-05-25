package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.ads.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.comments.CommentDto;
import ru.skypro.homework.dto.comments.CommentsDto;
import ru.skypro.homework.dto.comments.CreateOrUpdateCommentDto;
import ru.skypro.homework.service.CommentService;

@RestController
@Tag(name = "Комментарии")
@RequestMapping("/ads")
@Slf4j
public class CommentsController {

    private final CommentService commentService;

    public CommentsController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Получение комментариев объявления")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CommentsDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<CommentsDto> getComments(@PathVariable int id) {
        log.debug("Getting comments for ad with id: {}", id);
        CommentsDto comments = commentService.getCommentsByAdId(id);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Добавление комментария к объявлению")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<CommentDto> addComment(
            @PathVariable int id,
            @RequestBody CreateOrUpdateCommentDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Adding comment to ad with id: {} by user: {}", id, userDetails.getUsername());
        CommentDto createdComment = commentService.addComment(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(createdComment);
    }

    @DeleteMapping("/{adId}/comments/{commentId}")
    @Operation(summary = "Удаление комментария")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteComment(@PathVariable int adId,
                                              @PathVariable int commentId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Deleting comment with id: {} from ad: {} by user: {}",
                commentId, adId, userDetails.getUsername());
        commentService.deleteComment(adId, commentId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{adId}/comments/{commentId}")
    @Operation(summary = "Обновление комментария")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable int adId,
            @PathVariable int commentId,
            @RequestBody CreateOrUpdateCommentDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Updating comment with id: {} from ad: {} by user: {}",
                commentId, adId, userDetails.getUsername());
        CommentDto updatedComment = commentService.updateComment(adId, commentId, dto, userDetails.getUsername());
        return ResponseEntity.ok(updatedComment);
    }
}