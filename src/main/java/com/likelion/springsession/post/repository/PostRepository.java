package com.likelion.springsession.post.repository;

import com.likelion.springsession.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository 상속
public interface PostRepository extends JpaRepository<Post, Long> {
}
