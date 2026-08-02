package com.likelion.springsession.study;

import com.likelion.springsession.post.dto.PostSummaryResponse;
import com.likelion.springsession.post.service.PostService;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// PostStudyRunner는 Controller가 아니라 2주차 실습용 임시 호출 코드입니다!
// 3주차 CRUD 세션에서 Controller가 이 역할을 대신할 예정입니다~
@Component
public class PostStudyRunner implements CommandLineRunner {

    private final PostService postService;

    public PostStudyRunner(PostService postService) {
        this.postService = postService;
    }

    @Override
    public void run(String... args) {
        List<PostSummaryResponse> posts = postService.getPostSummaries();
        for (PostSummaryResponse post : posts) {
            System.out.println(
                    post.getId()
                    + " | "
                    + post.getTitle()
                    + " | "
                    + post.getCreatedAt()
            );
        }
    }
}
