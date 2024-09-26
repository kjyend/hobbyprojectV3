package com.hobbyproject.controller;

import com.hobbyproject.dto.comment.request.CreatedComment;
import com.hobbyproject.dto.comment.response.CommentResponseDto;
import com.hobbyproject.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;


    @GetMapping("/post/{postId}/comment")
    public List<CommentResponseDto> getCommentList(@PathVariable("postId") Long postId){
        return commentService.getList(postId);
    }

    @PostMapping("/post/{postId}/comment")
    public void addComment(@PathVariable("postId") Long postId, @Valid @RequestBody CreatedComment createdComment,@AuthenticationPrincipal UserDetails userDetails){
        commentService.commentCreate(createdComment, postId, userDetails.getUsername());
    }

    @DeleteMapping("/post/{postId}/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, @AuthenticationPrincipal UserDetails userDetails){

        if (commentService.isCommentOwner(commentId, userDetails.getUsername())) {
            commentService.deleteComment(commentId);

            return ResponseEntity.ok("Comment 삭제에 성공했습니다.");
        }else {
            return ResponseEntity.status(403).body("Comment 삭제에 실패했습니다.");
        }
    }

}
