package com.springboot.Job.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.Job.model.UserBean;
import com.springboot.Job.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    
    
    @Autowired
    private JavaMailSender mailSender;

    // =================== AUTHENTICATION ===================
    public Optional<UserBean> authenticateUser(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }

    public Optional<UserBean> getUserById(int id) {
        return userRepository.findById(id);
    }

    // =================== PROFILE MANAGEMENT ===================
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

    public boolean isPhoneExists(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    public byte[] getProfilePhoto(int userId) {
        return userRepository.getProfilePhotoById(userId);
    }

    // =================== OTP MANAGEMENT ===================
    private Map<String, OtpData> otpStore = new HashMap<>();

    public Optional<UserBean> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean updatePassword(String email, String newPassword) {
        return userRepository.updatePassword(email, newPassword);
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

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + "\nThis code expires in 2 minutes.");

        mailSender.send(message);
    }
}
