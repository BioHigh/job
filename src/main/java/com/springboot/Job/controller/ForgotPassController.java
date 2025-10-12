package com.springboot.Job.controller;

import java.util.Optional;


import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.Job.model.UserBean;
import com.springboot.Job.service.UserService;

@Controller
public class ForgotPassController {

    @Autowired
    private UserService userService;

    // =================== FORGOT PASSWORD ===================

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgotpassword"; // loads forgotpassword.html
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String gmail,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {

        Optional<UserBean> user = userService.findByEmail(gmail);

        if (user.isPresent()) {
            Long lastOtpTime = (Long) session.getAttribute("lastOtpTime");
            long currentTime = System.currentTimeMillis();

            if (lastOtpTime != null && (currentTime - lastOtpTime) < 120_000) {
                long remainingTime = (120_000 - (currentTime - lastOtpTime)) / 1000;
                redirectAttributes.addFlashAttribute("error",
                        "Please wait " + remainingTime + " seconds before requesting another OTP");
                return "redirect:/verify-otp";
            }

            String otp = userService.generateOtp(gmail);
            userService.sendOtpEmail(gmail, otp);

            session.setAttribute("resetEmail", gmail);
            session.setAttribute("lastOtpTime", currentTime);

            redirectAttributes.addFlashAttribute("success", "OTP sent to your Gmail!");
            return "redirect:/verify-otp";

        } else {
            redirectAttributes.addFlashAttribute("error", "Email not found!");
            return "redirect:/forgot-password";
        }
    }

    // =================== VERIFY OTP ===================

    @GetMapping("/verify-otp")
    public String showVerifyOtpPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/forgot-password";
        }

        Long lastOtpTime = (Long) session.getAttribute("lastOtpTime");
        int remainingSeconds = 120; // default 2 minutes

        if (lastOtpTime != null) {
            long elapsedTime = System.currentTimeMillis() - lastOtpTime;
            remainingSeconds = (int) ((120_000 - elapsedTime) / 1000);
            if (remainingSeconds < 0) remainingSeconds = 0;
        }

        model.addAttribute("isResendDisabled", remainingSeconds > 0);
        model.addAttribute("remainingSeconds", remainingSeconds);

        return "verifyotp"; // loads verifyotp.html
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please try again.");
            return "redirect:/forgot-password";
        }

        boolean isValid = userService.validateOtp(email, otp);
        if (isValid) {
            session.setAttribute("otpVerified", true);
            redirectAttributes.addFlashAttribute("success", "OTP verified successfully!");
            return "redirect:/reset-password";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP. Please try again.");
            return "redirect:/verify-otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/forgot-password";
        }

        Long lastOtpTime = (Long) session.getAttribute("lastOtpTime");
        long currentTime = System.currentTimeMillis();

        if (lastOtpTime != null && (currentTime - lastOtpTime) < 120_000) {
            long remainingTime = (120_000 - (currentTime - lastOtpTime)) / 1000;
            redirectAttributes.addFlashAttribute("error",
                    "Please wait " + remainingTime + " seconds before requesting another OTP");
            return "redirect:/verify-otp";
        }

        String otp = userService.generateOtp(email);
        userService.sendOtpEmail(email, otp);

        session.setAttribute("lastOtpTime", currentTime);
        redirectAttributes.addFlashAttribute("success", "OTP sent to your Gmail!");
        return "redirect:/verify-otp";
    }

    // =================== RESET PASSWORD ===================

    @GetMapping("/reset-password")
    public String showResetPasswordPage(HttpSession session) {
        if (Boolean.TRUE.equals(session.getAttribute("otpVerified"))) {
            return "resetpassword"; // loads resetpassword.html
        }
        return "redirect:/forgot-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("resetEmail");

        if (email == null || !Boolean.TRUE.equals(session.getAttribute("otpVerified"))) {
            redirectAttributes.addFlashAttribute("error", "Session expired or unauthorized access.");
            return "redirect:/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
            return "redirect:/reset-password";
        }

        boolean updated = userService.updatePassword(email, newPassword);
        if (updated) {
            session.removeAttribute("otpVerified");
            session.removeAttribute("resetEmail");

            redirectAttributes.addFlashAttribute("success", "Password reset successful! Please login.");
            return "redirect:/user/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to reset password. Try again.");
            return "redirect:/reset-password";
        }
    }
}
