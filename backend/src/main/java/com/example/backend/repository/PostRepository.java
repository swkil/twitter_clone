package com.example.backend.repository;

import com.example.backend.dto.PostResponse;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Post p WHERE p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findAllActive(Pageable pageable);

    @Query("SELECT new com.example.backend.dto.PostResponse(p, " +
            "(SELECT COUNT(c) FROM Comment c WHERE c.post = p), " +
            "(SELECT COUNT(l) FROM Like l WHERE l.post = p), " +
            "EXISTS(SELECT 1 FROM Like l2 WHERE l2.post = p AND l2.user = :user)) " +
            "FROM Post p WHERE p.deleted = false")
    Page<PostResponse> findAllWithCounts(@Param("user") User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deleted = false")
    Optional<Post> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId AND p.deleted = false")
    long countByUserIdAndNotDeleted(@Param("userId") Long userId);
}
