package com.likelion.springsession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.springsession.post.entity.Post;
import com.likelion.springsession.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SpringsessionApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void cleanDatabase() {
        postRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createPostReturnsCreatedPost() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "JPA CRUD",
                                  "content": "게시글을 생성합니다."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("JPA CRUD"))
                .andExpect(jsonPath("$.content").value("게시글을 생성합니다."))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertThat(postRepository.count()).isEqualTo(1);
    }

    @Test
    void getPostsReturnsSummariesWithoutContent() throws Exception {
        postRepository.save(new Post("첫 번째 글", "첫 번째 내용"));
        postRepository.save(new Post("두 번째 글", "두 번째 내용"));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].title").value("첫 번째 글"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[0].content").doesNotExist());
    }

    @Test
    void getPostReturnsDetail() throws Exception {
        Post savedPost = postRepository.save(new Post("상세 제목", "상세 내용"));

        mockMvc.perform(get("/api/posts/{postId}", savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPost.getId()))
                .andExpect(jsonPath("$.title").value("상세 제목"))
                .andExpect(jsonPath("$.content").value("상세 내용"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void updatePostUsesDirtyChecking() throws Exception {
        Post savedPost = postRepository.save(new Post("수정 전", "수정 전 내용"));

        mockMvc.perform(put("/api/posts/{postId}", savedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "수정 후",
                                  "content": "수정 후 내용"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정 후"))
                .andExpect(jsonPath("$.content").value("수정 후 내용"));

        Post updatedPost = postRepository.findById(savedPost.getId()).orElseThrow();
        assertThat(updatedPost.getTitle()).isEqualTo("수정 후");
        assertThat(updatedPost.getContent()).isEqualTo("수정 후 내용");
    }

    @Test
    void deletePostReturnsNoContent() throws Exception {
        Post savedPost = postRepository.save(new Post("삭제할 글", "삭제할 내용"));

        mockMvc.perform(delete("/api/posts/{postId}", savedPost.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(postRepository.existsById(savedPost.getId())).isFalse();
    }

    @Test
    void invalidCreateAndUpdateRequestsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": " ", "content": ""}
                                """))
                .andExpect(status().isBadRequest());

        Post savedPost = postRepository.save(new Post("원래 제목", "원래 내용"));
        mockMvc.perform(put("/api/posts/{postId}", savedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "content": "수정 내용"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingPostReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}", 999999))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/posts/{postId}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "없는 글", "content": "수정 시도"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/posts/{postId}", 999999))
                .andExpect(status().isNotFound());
    }
}
