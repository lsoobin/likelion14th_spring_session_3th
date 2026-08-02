package com.likelion.springsession.post.dto;

import java.time.LocalDateTime;

// 게시글 목록 화면에 필요한 데이터만 담는 Response DTO
// Post Entity에서 content 제외
public class PostSummaryResponse {

    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;

    public PostSummaryResponse(
            Long id,
            String title,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
