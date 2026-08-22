package com.likelion.springsession.post.service;

import com.likelion.springsession.post.dto.PostSummaryResponse;
import com.likelion.springsession.post.entity.Post;
import com.likelion.springsession.post.repository.PostRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;

    // 생성자 주입: PostService가 PostRepository를 직접 new 하지 않는다.
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // Entity 목록을 조회해 Response DTO 목록으로 변환한다.
    public List<PostSummaryResponse> getPostSummaries() {
        List<Post> posts = postRepository.findAll();
        List<PostSummaryResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            PostSummaryResponse response = new PostSummaryResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getCreatedAt()
            );

            responses.add(response);
        }

        return responses;
    }
}
