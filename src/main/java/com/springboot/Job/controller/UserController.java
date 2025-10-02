package com.springboot.Job.controller;

import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.Job.model.UserBean;
import com.springboot.Job.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /* ===================== LOGIN ===================== */

    @GetMapping("/login")
    public String showLoginPage() {
        return "userlogin";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String gmail,
                            @RequestParam String password,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Optional<UserBean> user = userService.authenticateUser(gmail, password);

        if (user.isPresent()) {
            session.setAttribute("user", user.get());
            session.setAttribute("userId", user.get().getId());
            return "redirect:/user/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/user/login";
        }
    }

    /* ===================== DASHBOARD ===================== */

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/user/login";

        Optional<UserBean> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());

            byte[] profilePhoto = userService.getProfilePhoto(userId);
            if (profilePhoto != null) {
                model.addAttribute("profilePhoto", Base64.getEncoder().encodeToString(profilePhoto));
            }
            return "userdashboard";
        }
        return "redirect:/user/login";
    }

    /* ===================== PROFILE ===================== */

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/user/login";

        Optional<UserBean> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());

            byte[] profilePhoto = userService.getProfilePhoto(userId);
            if (profilePhoto != null) {
                model.addAttribute("profilePhoto", Base64.getEncoder().encodeToString(profilePhoto));
            }
            return "userprofile";
        }
        return "redirect:/user/login";
    }

    @GetMapping("/profile/edit")
    public String showEditProfile(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/user/login";

        Optional<UserBean> user = userService.getUserById(userId);
        user.ifPresent(value -> model.addAttribute("user", value));

        return "editprofile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String dateOfBirth,
                                @RequestParam String gender,
                                @RequestParam String phone,
                                @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/user/login";

        UserBean user = new UserBean();
        user.setId(userId);
        user.setName(name);
        user.setDateOfBirth(dateOfBirth); // Should be LocalDate in entity
        user.setGender(gender);
        user.setPhone(phone);

        boolean isUpdated = userService.updateUserProfile(user, profilePhoto);

        if (isUpdated) {
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            userService.getUserById(userId).ifPresent(u -> session.setAttribute("user", u));
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile");
        }

        return "redirect:/user/profile";
    }

    /* ===================== REGISTRATION ===================== */

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new UserBean());
        return "userregister";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserBean user,
                               @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {

        if (!user.getPassword().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/user/register";
        }

        if (userService.isEmailExists(user.getGmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/user/register";
        }

        boolean isRegistered = userService.registerUser(user, profilePhoto);
        if (isRegistered) {
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/user/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Registration failed");
            return "redirect:/user/register";
        }
    }

    /* ===================== LOGOUT ===================== */

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/index";
    }
}
