package com.springboot.Job.controller;

import com.springboot.Job.model.UserBean; // import UserBean
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("sessionUser")
    public UserBean addUserToModel(HttpSession session) {
        return (UserBean) session.getAttribute("user");
    }

}
