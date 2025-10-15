package com.example.backend.repository;

import com.example.backend.entity.Like;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, Long> {
    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);

    boolean existsByUserAndPost(User user, Post post);

    void deleteByUserAndPost(User user, Post post);
}
