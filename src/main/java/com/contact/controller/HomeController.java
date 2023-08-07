package com.contact.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.contact.model.User;
import com.contact.repository.UserRepository;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Contact Manager");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

//	@RequestMapping(value = "do_register", method = RequestMethod.POST)
//	public RedirectView registerUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult,
//			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
//			HttpSession session) {
//		try {
//			if (!agreement) {
//				throw new Exception(" You have not agreed the terms and conditions");
//			}
////			if (bindingResult.hasErrors()) {
////				model.addAttribute("user", user);
////				return new RedirectView("/signup");
////			}
//			user.setRole("ROLE_USER");
//			user.setImageUrl("default.jpg");
//			user.setEnabled(true);
//			user.setPassword(passwordEncoder.encode(user.getPassword()));
//			userRepository.save(user);
//			model.addAttribute("user", new User());
//			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
//			return new RedirectView("/signin");
//		} catch (Exception e) {
//			e.printStackTrace();
//			model.addAttribute("user", user);
//			session.setAttribute("message", new Message("Something Went Wrong!!" + e.getMessage(), "alert-danger"));
//			return new RedirectView("/signup");
//		}
//	}

	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login - Contact Manager");
		return "login";
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "do_register")
	@ResponseBody
	public ResponseEntity<Map<String, String>> registerUser(@RequestBody @Valid User user,
//			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, 
			Model model, HttpSession session) {
		try {
//			if (!agreement) {
//				throw new Exception(" You have not agreed the terms and conditions");
//			}
			if ("".equals(user.getPassword()) || "".equals(user.getEmail()) || "".equals(user.getName())) {
				throw new Exception();
			}
			user.setRole("ROLE_USER");
			user.setImageUrl("default.jpg");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			userRepository.save(user);
			return ResponseEntity.ok(Map.of("msg", "added"));
		} catch (Exception e) {
			e.printStackTrace();
			return  (ResponseEntity<Map<String, String>>) ResponseEntity.badRequest();
		}
	}
}
