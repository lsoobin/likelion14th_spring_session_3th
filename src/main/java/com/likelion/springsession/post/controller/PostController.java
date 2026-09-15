package com.likelion.springsession.post.controller;

import com.likelion.springsession.post.dto.PostCreateRequest;
import com.likelion.springsession.post.dto.PostDetailResponse;
import com.likelion.springsession.post.dto.PostSummaryResponse;
import com.likelion.springsession.post.dto.PostUpdateRequest;
import com.likelion.springsession.post.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public List<PostSummaryResponse> getPosts() {
        return postService.getPostSummaries();
    }

    @GetMapping("/{postId}")
    public PostDetailResponse getPost(@PathVariable Long postId) {
        return postService.getPost(postId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailResponse createPost(@Valid @RequestBody PostCreateRequest request) {
        return postService.createPost(request);
    }

    @PutMapping("/{postId}")
    public PostDetailResponse updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        return postService.updatePost(postId, request);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
    }
}
