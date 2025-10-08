package com.springboot.Job.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.Job.model.Owner;
import com.springboot.Job.repository.OwnerRepository;

@Service
public class OwnerService {

    @Autowired
    private OwnerRepository ownerRepository;

    // Maximum file size: 2MB
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    public Optional<Owner> authenticateOwner(String email, String password) {
        return ownerRepository.findByEmailAndPassword(email, password);
    }

    public Optional<Owner> getOwnerById(int id) {
        return ownerRepository.findById(id);
    }

    public boolean updateOwnerProfile(Owner owner, MultipartFile profilePhoto) {
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                // Validate file size
                if (profilePhoto.getSize() > MAX_FILE_SIZE) {
                    throw new RuntimeException("File size too large. Maximum size is 2MB.");
                }
                
                // Validate file type
                String contentType = profilePhoto.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new RuntimeException("Only image files are allowed.");
                }
                
                byte[] profilePhotoBytes = profilePhoto.getBytes();
                return ownerRepository.updateOwner(owner, profilePhotoBytes);
            } catch (IOException e) {
                throw new RuntimeException("Failed to process profile photo", e);
            }
        } else {
            return ownerRepository.updateOwnerWithoutPhoto(owner);
        }
    }

    public boolean registerOwner(Owner owner, MultipartFile profilePhoto) {
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            try {
                // Validate file size
                if (profilePhoto.getSize() > MAX_FILE_SIZE) {
                    throw new RuntimeException("File size too large. Maximum size is 2MB.");
                }
                
                // Validate file type
                String contentType = profilePhoto.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new RuntimeException("Only image files are allowed.");
                }
                
                byte[] profilePhotoBytes = profilePhoto.getBytes();
                return ownerRepository.createOwner(owner, profilePhotoBytes);
            } catch (IOException e) {
                throw new RuntimeException("Failed to process profile photo", e);
            }
        } else {
            return ownerRepository.createOwnerWithoutPhoto(owner);
        }
    }

    public boolean isEmailExists(String email) {
        return ownerRepository.findByEmail(email).isPresent();
    }

    public boolean isPhoneExists(String phone) {
        return ownerRepository.findByPhone(phone).isPresent();
    }

    public byte[] getProfilePhoto(int ownerId) {
        return ownerRepository.getProfilePhotoById(ownerId);
    }
    
    public List<Owner> getAllCompanies() {
        return ownerRepository.findAllCompanies();
    }
}