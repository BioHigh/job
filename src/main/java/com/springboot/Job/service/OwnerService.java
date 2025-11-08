package com.springboot.Job.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.Job.model.Owner;
import com.springboot.Job.repository.OwnerRepository;

@Service
public class OwnerService {

    @Autowired
    private OwnerRepository ownerRepository;
    
    @Autowired
    private JavaMailSender mailSender;

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
        // Set authKey before saving
        owner.setAuthKey(java.util.UUID.randomUUID().toString());
        
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
    
    public boolean validateApprovedEmail(String submittedEmail, String approvedEmail) {
        if (approvedEmail == null || approvedEmail.trim().isEmpty()) {
            throw new RuntimeException("No approved email provided. Please use the approval link.");
        }
        
        if (!submittedEmail.equals(approvedEmail)) {
            throw new RuntimeException("Email mismatch. You must use the same email that was approved: " + approvedEmail);
        }
        
        return true;
    }
    
    private Map<String, OtpData> otpStore = new HashMap<>();

    public Optional<Owner> findByEmail(String email) {
        return ownerRepository.findByEmail(email);
    }

    public boolean updatePassword(String email, String newPassword) {
        return ownerRepository.updatePassword(email, newPassword);
    }

    public String generateOtp(String email) {
        Random random = new Random();
        String otp = String.format("%06d", random.nextInt(999_999));

        otpStore.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(2)));
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        OtpData otpData = otpStore.get(email);
        return otpData != null && otpData.getExpiry().isAfter(LocalDateTime.now()) 
               && otpData.getCode().equals(otp);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code - JobJump Employer Portal");
        message.setText("Your OTP is: " + otp + "\nThis code expires in 2 minutes.\n\nIf you didn't request this, please ignore this email.");

        mailSender.send(message);
    }

    private static class OtpData {
        private final String code;
        private final LocalDateTime expiry;

        public OtpData(String code, LocalDateTime expiry) {
            this.code = code;
            this.expiry = expiry;
        }

        public String getCode() { return code; }
        public LocalDateTime getExpiry() { return expiry; }
    }
    

    public List<Owner> getActiveCompaniesWithPagination(int page, int size) {
        int offset = (page - 1) * size;
        return ownerRepository.findActiveCompaniesWithPagination(size, offset);
    }

    public int getTotalActiveCompanies() {
        return ownerRepository.countActiveCompanies();
    }

    public int getTotalPages(int pageSize) {
        int totalCompanies = getTotalActiveCompanies();
        return (int) Math.ceil((double) totalCompanies / pageSize);
    }
    
    // Search methods - Add these
    public List<Owner> searchActiveCompanies(String search) {
        return ownerRepository.searchActiveCompanies(search);
    }
    
    public List<Owner> searchActiveCompaniesWithPagination(String search, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return ownerRepository.searchActiveCompaniesWithPagination(search, pageSize, offset);
    }
    
    public int countSearchActiveCompanies(String search) {
        return ownerRepository.countSearchActiveCompanies(search);
    }
}