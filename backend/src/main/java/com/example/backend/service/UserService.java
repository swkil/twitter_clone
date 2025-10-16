package com.example.backend.service;

import com.example.backend.dto.UserResponse;
import com.example.backend.dto.UserUpdateRequest;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.FollowRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final AuthenticationService authenticationService;
    private final FileUploadService fileUploadService;

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

    public UserResponse updateUserProfile(UserUpdateRequest request) {
        User currentUser = authenticationService.getCurrentUser();

        currentUser.setFullName(request.getFullName());
        currentUser.setBio(request.getBio());

        User updatedUser = userRepository.save(currentUser);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public String updateProfileImage(MultipartFile file) {
        User currentUser = authenticationService.getCurrentUser();

        String storedFilename = fileUploadService.storeFile(file);
        String imageUrl = "/images/" + storedFilename;

        currentUser.setProfileImageUrl(imageUrl);
        userRepository.save(currentUser);

        return imageUrl;
    }
}
