package com.example.backend.service;

import com.example.backend.dto.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.FollowRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final AuthenticationService authenticationService;

    private UserResponse mapToUserResponse(User user) {
        User currentUser = authenticationService.getCurrentUser();

        boolean isFollowing = false;
        if (!currentUser.getId().equals(user.getId())) {
            isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, user);
        }

        Long followersCount = followRepository.countFollowers(user);
        Long followingCount = followRepository.countFollowing(user);

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .followersCount(followersCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username" + username));

        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id" + userId));

        return mapToUserResponse(user);
    }
}
