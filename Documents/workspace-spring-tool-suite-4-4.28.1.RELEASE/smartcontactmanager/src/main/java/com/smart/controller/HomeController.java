package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

//import ch.qos.logback.core.model.Model;
//import org.springframework.web.bind.annotation.ResponseBody;

//import com.smart.dao.UserRepository;
//import com.smart.entities.Contact;
//import com.smart.entities.User;

@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home-smart contact Manager");
		return "home";
	}
	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","about-smart contact Manager");
		return "about";
	}
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register-smart contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","Login Page");
		return "login";
	}
	//handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1 ,@RequestParam(value  = "agreement",defaultValue="false") boolean agreement,Model model,HttpSession session) {
		try {
			
			if(!agreement) {
				System.out.println("you have not agreed terms and conditions");
				throw new Exception("you have not agreed terms and conditions");
			}
			if(result1.hasErrors()) {
				System.out.println("ERROR"+ result1.toString());
				model.addAttribute("User",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement"+agreement);
			System.out.print("User"+user);
			User result = this.userRepository.save(user);
			
			model.addAttribute("User",new User());
			session.setAttribute("message", new Message("Successfully Registered!!","alert-success"));
			return "signup";
			
		} catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("something went wrong!!"+e.getMessage(),"alert-danger"));
			return "signup";
		}
	}
}
