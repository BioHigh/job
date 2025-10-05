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

    @GetMapping("/login")
    public String showLoginPage() {
        return "userlogin";
    }
    
    @GetMapping("/index")
    public String showIndex1() {
        return "index";
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
            return "redirect:/user/home"; // ✅ redirect fixes the CSS URL issue
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/user/login";
        }
    }

    @GetMapping("/home")
    public String showHome(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }
        
        Optional<UserBean> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "home"; // ✅ this is fine since URL is now /user/home
        } else {
            return "redirect:/user/login";
        }
    }


    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Optional<UserBean> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            byte[] profilePhoto = userService.getProfilePhoto(userId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            return "userdashboard";
        } else {
            return "redirect:/user/login";
        }
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Optional<UserBean> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            // Get profile photo separately
            byte[] profilePhoto = userService.getProfilePhoto(userId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            return "userprofile";
        } else {
            return "redirect:/user/login";
        }
    }

    @GetMapping("/profile/edit")
    public String showEditProfile(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Optional<UserBean> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "admin/editprofile";
        } else {
            return "redirect:/user/login";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                               @RequestParam Integer age,
                               @RequestParam String dateOfBirth,
                               @RequestParam String gender,
                               @RequestParam String phone,
                               @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        // Create UserBean manually
        UserBean user = new UserBean();
        user.setId(userId);
        user.setName(name);
        user.setAge(age);
        user.setDateOfBirth(dateOfBirth);
        user.setGender(gender);
        user.setPhone(phone);

        boolean isUpdated = userService.updateUserProfile(user, profilePhoto);
        
        if (isUpdated) {
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            Optional<UserBean> updatedUser = userService.getUserById(userId);
            updatedUser.ifPresent(u -> session.setAttribute("user", u));
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile");
        }
        
        return "redirect:/user/profile";
    }

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
        
        // Validation
        if (!user.getPassword().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/user/register";
        }

        if (userService.isEmailExists(user.getGmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/user/register";
        }

        // Check if phone number already exists
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            if (userService.isPhoneExists(user.getPhone())) {
                redirectAttributes.addFlashAttribute("error", "Phone number already exists");
                return "redirect:/user/register";
            }
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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/index";
    }
}