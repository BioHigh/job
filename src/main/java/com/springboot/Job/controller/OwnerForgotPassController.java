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

import com.springboot.Job.model.Owner;
import com.springboot.Job.service.OwnerService;

@Controller
public class OwnerForgotPassController {

    @Autowired
    private OwnerService ownerService;

    // =================== FORGOT PASSWORD FOR OWNER ===================

    @GetMapping("/owner/ownerforgotpassword")
    public String showOwnerForgotPasswordPage() {
        return "owner/ownerforgotpassword"; // loads ownerforgotpassword.html
    }

    @PostMapping("/owner/forgot-password")
    public String processOwnerForgotPassword(@RequestParam String gmail,
                                            HttpSession session,
                                            RedirectAttributes redirectAttributes) {

        Optional<Owner> owner = ownerService.findByEmail(gmail);

        if (owner.isPresent()) {
            Long lastOtpTime = (Long) session.getAttribute("lastOtpTime");
            long currentTime = System.currentTimeMillis();

            if (lastOtpTime != null && (currentTime - lastOtpTime) < 120_000) {
                long remainingTime = (120_000 - (currentTime - lastOtpTime)) / 1000;
                redirectAttributes.addFlashAttribute("error",
                        "Please wait " + remainingTime + " seconds before requesting another OTP");
                return "redirect:/owner/verify-otp";
            }

            String otp = ownerService.generateOtp(gmail);
            ownerService.sendOtpEmail(gmail, otp);

            session.setAttribute("resetEmail", gmail);
            session.setAttribute("lastOtpTime", currentTime);

            redirectAttributes.addFlashAttribute("success", "OTP sent to your email!");
            return "redirect:/owner/verify-otp";

        } else {
            redirectAttributes.addFlashAttribute("error", "Email not found!");
            return "redirect:/owner/ownerforgotpassword";
        }
    }

    // =================== VERIFY OTP FOR OWNER ===================

    @GetMapping("/owner/verify-otp")
    public String showOwnerVerifyOtpPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/owner/ownerforgotpassword";
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

        return "owner/ownerverifyotp"; // loads ownerverifyotp.html
    }

    @PostMapping("/owner/verify-otp")
    public String verifyOwnerOtp(@RequestParam String otp,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please try again.");
            return "redirect:/owner/ownerforgotpassword";
        }

        boolean isValid = ownerService.validateOtp(email, otp);
        if (isValid) {
            session.setAttribute("otpVerified", true);
            redirectAttributes.addFlashAttribute("success", "OTP verified successfully!");
            return "redirect:/owner/reset-password";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP. Please try again.");
            return "redirect:/owner/verify-otp";
        }
    }

    @PostMapping("/owner/resend-otp")
    public String resendOwnerOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/owner/ownerforgotpassword";
        }

        Long lastOtpTime = (Long) session.getAttribute("lastOtpTime");
        long currentTime = System.currentTimeMillis();

        if (lastOtpTime != null && (currentTime - lastOtpTime) < 120_000) {
            long remainingTime = (120_000 - (currentTime - lastOtpTime)) / 1000;
            redirectAttributes.addFlashAttribute("error",
                    "Please wait " + remainingTime + " seconds before requesting another OTP");
            return "redirect:/owner/verify-otp";
        }

        String otp = ownerService.generateOtp(email);
        ownerService.sendOtpEmail(email, otp);

        session.setAttribute("lastOtpTime", currentTime);
        redirectAttributes.addFlashAttribute("success", "OTP sent to your email!");
        return "redirect:/owner/verify-otp";
    }

    // =================== RESET PASSWORD FOR OWNER ===================

    @GetMapping("/owner/reset-password")
    public String showOwnerResetPasswordPage(HttpSession session) {
        if (Boolean.TRUE.equals(session.getAttribute("otpVerified"))) {
            return "owner/ownerresetpassword"; // loads ownerresetpassword.html
        }
        return "redirect:/owner/ownerforgotpassword";
    }

    @PostMapping("/owner/reset-password")
    public String processOwnerResetPassword(@RequestParam String newPassword,
                                           @RequestParam String confirmPassword,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("resetEmail");

        if (email == null || !Boolean.TRUE.equals(session.getAttribute("otpVerified"))) {
            redirectAttributes.addFlashAttribute("error", "Session expired or unauthorized access.");
            return "redirect:/owner/ownerforgotpassword";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
            return "redirect:/owner/reset-password";
        }

        boolean updated = ownerService.updatePassword(email, newPassword);
        if (updated) {
            session.removeAttribute("otpVerified");
            session.removeAttribute("resetEmail");
            session.removeAttribute("lastOtpTime");

            redirectAttributes.addFlashAttribute("success", "Password reset successful! Please login.");
            return "redirect:/owner/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to reset password. Try again.");
            return "redirect:/owner/reset-password";
        }
    }
}