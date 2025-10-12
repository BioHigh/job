package com.springboot.Job.controller;

import org.springframework.stereotype.Controller;


import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class IndexController {
	
	
	
	@GetMapping("/home")
	public String showIndex2() {
	
		return "home";
	}
	@GetMapping("/company")
	public String showIndex3() {
	
		return "owner/company";
	}
	
	
	
}
