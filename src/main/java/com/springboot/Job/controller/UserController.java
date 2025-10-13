package com.springboot.Job.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.Job.model.CategoryBean;
import com.springboot.Job.model.JobPostBean;
import com.springboot.Job.model.Owner;
import com.springboot.Job.model.UserBean;
import com.springboot.Job.repository.CategoryRepository;
import com.springboot.Job.service.JobPostService;
import com.springboot.Job.service.OwnerService;
import com.springboot.Job.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private OwnerService ownerService;
    
    @Autowired
    private JobPostService jobPostService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "userlogin";
    }
    
    @GetMapping("/index")
	public String showIndex1() {
	
		return "index";
	}
    
    @GetMapping("/aboutus")
	public String showIndex2() {
	
		return "CuBu/aboutus";
	}
    @GetMapping("/contactus")
	public String showIndex3() {
	
		return "CuBu/contactus";
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
            return "redirect:/user/home"; // Redirect to a home endpoint
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/user/login";
        }
    }

    //==================12.10.2025 ==========================
    @GetMapping("/home")
    public String showHome(HttpSession session, Model model, @RequestParam(defaultValue = "1") int page) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }
        
        // Add job listings for logged-in users
        int pageSize = 4;
        long totalJobs = jobPostService.countAllJobs();
        List<Map<String, Object>> activeJobs = jobPostService.getActiveJobsWithOwners(page, pageSize);
        int totalPages = jobPostService.getTotalPages(pageSize);
        
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("hasPrev", page > 1);
        
        Optional<UserBean> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "home";
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
            return "/editprofile";
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
    
    

    @GetMapping("/by-category")
    public String getJobsByCategory(Model model) {
        List<CategoryBean> categories = categoryRepository.findAll();
        Map<Integer, Integer> jobCounts = categoryRepository.getAllCategoriesWithJobCounts();
        
        model.addAttribute("categories", categories);
        model.addAttribute("jobCounts", jobCounts);
        return "jobs-by-category";
    }
    
    //===============fix 9.10.2025=====================
    
    @GetMapping("/guestview_companies")
    public String showGuestViewCompanies(HttpSession session, Model model) {
        System.out.println("=== Guest view companies accessed ===");
        
        List<Owner> companies = ownerService.getAllCompanies();
        
        List<Map<String, Object>> companiesWithPhotos = new ArrayList<>();
        
        for (Owner company : companies) {
            Map<String, Object> companyMap = new HashMap<>();
            companyMap.put("company", company);
            
            byte[] profilePhoto = ownerService.getProfilePhoto(company.getId());
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                companyMap.put("profilePhotoBase64", base64Photo);
            } else {
                companyMap.put("profilePhotoBase64", null);
            }
            
            companiesWithPhotos.add(companyMap);
        }
        
        model.addAttribute("companiesWithPhotos", companiesWithPhotos);
        
        return "guestview_companies";
    }
    /// 
    @GetMapping("/company/{id}")
    public String viewCompanyJobs(@PathVariable("id") Integer companyId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        Optional<Owner> company = ownerService.getOwnerById(companyId);
        if (company.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Company not found");
            return "redirect:/user_companies";
        }
        
        List<JobPostBean> companyJobs = jobPostService.findJobsByCompanyId(companyId);
        
        model.addAttribute("company", company.get());
        model.addAttribute("companyJobs", companyJobs);
        
        byte[] profilePhoto = ownerService.getProfilePhoto(companyId);
        if (profilePhoto != null) {
            String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
            model.addAttribute("companyProfilePhoto", base64Photo);
        }
        
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        if (ownerId != null) {
            Optional<Owner> loggedInOwner = ownerService.getOwnerById(ownerId);
            loggedInOwner.ifPresent(o -> {
                model.addAttribute("owner", o);
                byte[] ownerProfilePhoto = ownerService.getProfilePhoto(ownerId);
                if (ownerProfilePhoto != null) {
                    String base64OwnerPhoto = Base64.getEncoder().encodeToString(ownerProfilePhoto);
                    model.addAttribute("profilePhoto", base64OwnerPhoto);
                }
            });
        }
        
        return "company-jobs";
    }
    
    @GetMapping("/user_companies")
    public String showUserCompanies(HttpSession session, Model model) {
        List<Owner> companies = ownerService.getAllCompanies();
        
        List<Map<String, Object>> companiesWithPhotos = new ArrayList<>();
        
        for (Owner company : companies) {
            Map<String, Object> companyMap = new HashMap<>();
            companyMap.put("company", company);
            
            byte[] profilePhoto = ownerService.getProfilePhoto(company.getId());
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                companyMap.put("profilePhotoBase64", base64Photo);
            } else {
                companyMap.put("profilePhotoBase64", null);
            }
            
            companiesWithPhotos.add(companyMap);
        }
        
        model.addAttribute("companiesWithPhotos", companiesWithPhotos);
        
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
           
        }
        
        return "user_companies";
    }
    
    //8.10.2025
    @GetMapping("/job/detail/{id}")
    public String viewJobDetail(@PathVariable("id") Integer jobId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in (either owner or user)
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        Integer userId = (Integer) session.getAttribute("userId");
        
        // Find the job by ID
        Optional<JobPostBean> jobPost = jobPostService.findJobById(jobId);
        
        if (jobPost.isPresent()) {
            model.addAttribute("job", jobPost.get());
            
            // Get company info
            Optional<Owner> company = ownerService.getOwnerById(jobPost.get().getOwnerId());
            if (company.isPresent()) {
                model.addAttribute("company", company.get());
                
                // Get company profile photo
                byte[] companyProfilePhoto = ownerService.getProfilePhoto(company.get().getId());
                if (companyProfilePhoto != null) {
                    String base64Photo = Base64.getEncoder().encodeToString(companyProfilePhoto);
                    model.addAttribute("companyProfilePhoto", base64Photo);
                }
            }
            
            // Add user type information for template
            if (ownerId != null) {
                model.addAttribute("userType", "owner");
                Optional<Owner> owner = ownerService.getOwnerById(ownerId);
                owner.ifPresent(o -> {
                    model.addAttribute("owner", o);
                    byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
                    if (profilePhoto != null) {
                        String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                        model.addAttribute("profilePhoto", base64Photo);
                    }
                });
            } else if (userId != null) {
                model.addAttribute("userType", "user");
                Optional<UserBean> user = userService.getUserById(userId);
                user.ifPresent(u -> {
                    model.addAttribute("user", u);
                    byte[] profilePhoto = userService.getProfilePhoto(userId);
                    if (profilePhoto != null) {
                        String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                        model.addAttribute("profilePhoto", base64Photo);
                    }
                });
            } else {
                model.addAttribute("userType", "guest");
            }
            
            return "job-detail";
        } else {
            redirectAttributes.addFlashAttribute("error", "Job not found.");
            return "redirect:/user/user_companies";
        }
    }
}