package com.springboot.Job.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.Job.model.UserBean;
import com.springboot.Job.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<UserBean> authenticateUser(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }

    public Optional<UserBean> getUserById(int id) {
        return userRepository.findById(id);
    }

    public boolean updateUserProfile(UserBean user, MultipartFile profilePhoto) {
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                byte[] profilePhotoBytes = profilePhoto.getBytes();
                return userRepository.updateUser(user, profilePhotoBytes);
            } catch (IOException e) {
                throw new RuntimeException("Failed to process profile photo", e);
            }
        } else {
            return userRepository.updateUserWithoutPhoto(user);
        }
    }

    public boolean registerUser(UserBean user, MultipartFile profilePhoto) {
        byte[] profilePhotoBytes = null;
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                profilePhotoBytes = profilePhoto.getBytes();
            } catch (IOException e) {
                throw new RuntimeException("Failed to process profile photo", e);
            }
        }
        return userRepository.createUser(user, profilePhotoBytes);
    }

    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public byte[] getProfilePhoto(int userId) {
        return userRepository.getProfilePhotoById(userId);
    }
}