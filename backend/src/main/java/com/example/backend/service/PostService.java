package com.example.backend.service;

import com.example.backend.dto.PostRequest;
import com.example.backend.dto.PostResponse;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.LikeRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public PostResponse createPost(PostRequest request) {
        User detachedUser = authenticationService.getCurrentUser();

        User currentUser = userRepository.findById(detachedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = Post.builder()
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .user(currentUser)
                .deleted(false)
                .build();

        post = postRepository.save(post);
        return PostResponse.fromEntity(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        User currentUser = authenticationService.getCurrentUser();

        Page<Post> posts = postRepository.findAllActive(pageable);
        return posts.map(post -> {
            PostResponse response = PostResponse.fromEntity(post);
            Long commentCount = commentRepository.countByPostId(post.getId());
            Long likeCount = likeRepository.countByPostId(post.getId());
            boolean isLiked = likeRepository.existsByUserAndPost(currentUser, post);
            // 북마크 추가 예정

            response.setCommentCount(commentCount);
            response.setLikeCount(likeCount);
            response.setLiked(isLiked);

            return response;
        });
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        User currentUser = authenticationService.getCurrentUser();

        Page<Post> posts = postRepository.findByUserIdAndNotDeleted(userId, pageable);
        return posts.map(post -> {
            PostResponse response = PostResponse.fromEntity(post);
            Long commentCount = commentRepository.countByPostId(post.getId());
            Long likeCount = likeRepository.countByPostId(post.getId());
            boolean isLiked = likeRepository.existsByUserAndPost(currentUser, post);
            // 북마크 추가 예정

            response.setCommentCount(commentCount);
            response.setLikeCount(likeCount);
            response.setLiked(isLiked);

            return response;
        });
    }

    @Transactional(readOnly = true)
    public Long getUserPostCount(Long userId) {
        authenticationService.getCurrentUser();
        return postRepository.countByUserIdAndNotDeleted(userId);
    }

    public PostResponse updatePost(Long postId, PostRequest request) {
        User currentUser = authenticationService.getCurrentUser();
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }

        post.setContent(request.getContent());

        post = postRepository.save(post);
        return PostResponse.fromEntity(post);
    }

    public void deletePost(Long postId) {
        User currentUser = authenticationService.getCurrentUser();
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this post");
        }

        post.setDeleted(true);
        postRepository.save(post);
    }
}
