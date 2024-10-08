package com.hobbyproject.dto.post.response;

import com.hobbyproject.entity.Post;
import com.hobbyproject.entity.UploadFile;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class PostResponseDto {

    private Long id;
    private String title;
    private String content;
    private List<String> imageUrls;


    public PostResponseDto(Post post) {
        this.id = post.getPostId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.imageUrls = post.getUploadFiles().stream()
                .map(UploadFile::getStoreFileName)
                .collect(Collectors.toList());
    }

    @Builder
    public PostResponseDto(Long id, String title, String content, List<String> imageUrls) {
        this.id = id;
        this.title = title.substring(0,Math.min(title.length(),10));
        this.content = content;
        this.imageUrls = imageUrls;
    }
}
