package com.hobbyproject.controller.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hobbyproject.dto.comment.request.CreatedComment;
import com.hobbyproject.entity.Comment;
import com.hobbyproject.entity.DeleteStatus;
import com.hobbyproject.entity.Member;
import com.hobbyproject.entity.Post;
import com.hobbyproject.repository.commnet.CommentRepository;
import com.hobbyproject.repository.member.MemberRepository;
import com.hobbyproject.repository.post.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureMockMvc
class CommentRestControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @AfterEach
    void clean() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        memberRepository.deleteAll();
    }


    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getCommentListSuccessTest() throws Exception {
        Member member = Member.builder()
                .loginId("1")
                .password(passwordEncoder.encode("1"))
                .name("김회원")
                .birthday(LocalDate.parse("2000-11-11"))
                .build();


        Post postBuild = Post.builder()
                .title("제목1")
                .content("내용1")
                .member(member)
                .build();
        memberRepository.save(member);
        Post post = postRepository.save(postBuild);

        List<Comment> commentList = List.of(
                new Comment("내용1", DeleteStatus.NO,postBuild, member,null),
                new Comment("내용2", DeleteStatus.NO,postBuild, member,null)
        );

        commentRepository.saveAll(commentList);


        // when: 댓글 목록 조회 요청 수행
        mockMvc.perform(MockMvcRequestBuilders.get("/post/{postId}/comment", post.getPostId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].content").value("내용1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("김회원"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].content").value("내용2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("김회원"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("댓글 추가 성공")
    void addCommentSuccessTest() throws Exception {
        Member member = Member.builder()
                .loginId("1")
                .password(passwordEncoder.encode("1"))
                .name("김회원")
                .birthday(LocalDate.parse("2000-11-11"))
                .build();


        Post postBuild = Post.builder()
                .title("제목1")
                .content("내용1")
                .member(member)
                .build();
        memberRepository.save(member);
        Post post = postRepository.save(postBuild);

        CreatedComment createdComment = CreatedComment.builder()
                .content("새로운 댓글")
                .build();


        mockMvc.perform(MockMvcRequestBuilders.post("/post/{postId}/comment", post.getPostId())
                        .content(objectMapper.writeValueAsString(createdComment))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.user(member.getLoginId()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());

        assertEquals(1L, commentRepository.count());

    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteCommentSuccessTest() throws Exception {
        Member member = Member.builder()
                .loginId("1")
                .password(passwordEncoder.encode("1"))
                .name("김회원")
                .birthday(LocalDate.parse("2000-11-11"))
                .build();


        Post postBuild = Post.builder()
                .title("제목1")
                .content("내용1")
                .member(member)
                .build();
        memberRepository.save(member);
        Post post = postRepository.save(postBuild);

        Comment comment = Comment.builder()
                .content("댓글")
                .post(post)
                .member(member)
                .isDeleted(DeleteStatus.NO)
                .parent(null)
                .build();

        commentRepository.save(comment);

        assertEquals(1L, commentRepository.count());

        mockMvc.perform(MockMvcRequestBuilders.delete("/post/{postId}/comment/{commentId}", post.getPostId(), comment.getCommentId())
                        .with(SecurityMockMvcRequestPostProcessors.user(member.getLoginId()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());

        assertEquals(0L, commentRepository.count());
    }




    @Test
    @DisplayName("다른 사용자가 댓글 삭제 시도 실패")
    void deleteCommentByNonOwnerTest() throws Exception {
        // given
        Member commentOwner = Member.builder()
                .loginId("owner123")
                .password(passwordEncoder.encode("password"))
                .name("작성자")
                .birthday(LocalDate.parse("2000-01-01"))
                .build();
        memberRepository.save(commentOwner);

        Post post = Post.builder()
                .title("테스트 게시글")
                .content("게시글 내용")
                .member(commentOwner)
                .build();
        postRepository.save(post);

        Comment comment = Comment.builder()
                .content("댓글 내용")
                .member(commentOwner)
                .post(post)
                .build();
        commentRepository.save(comment);

        Member otherUser = Member.builder()
                .loginId("other123")
                .password(passwordEncoder.encode("password"))
                .name("다른 사용자")
                .birthday(LocalDate.parse("2000-01-01"))
                .build();
        memberRepository.save(otherUser);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("/post/{postId}/comment/{commentId}", post.getPostId(), comment.getCommentId())
                        .with(SecurityMockMvcRequestPostProcessors.user(otherUser.getLoginId()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print());
    }








}