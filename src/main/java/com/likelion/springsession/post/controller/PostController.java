package com.likelion.springsession.post.controller;

import com.likelion.springsession.post.dto.PostSummaryResponse;
import com.likelion.springsession.post.service.PostService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/api/posts")
    public List<PostSummaryResponse> getPosts() {
        return postService.getPostSummaries();
    }
}
