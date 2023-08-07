package com.contact.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.helper.Message;
import com.contact.model.User;
import com.contact.repository.UserRepository;
import com.contact.service.EmailService;

@Controller
public class ForgotController {

	Random random = new Random(1000);

	@Autowired
	EmailService emailService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@RequestMapping("/forgot")
	public String openEmailForm(Model model) {
		model.addAttribute("title", "forgot - Contact Manager");
		return "forgot-email-form.html";
	}

	@PostMapping(value = "/send-otp")
	public String sendOTP(Model model, @RequestParam("email") String email, HttpSession session) {
		model.addAttribute("title", "forgot - Contact Manager");
		User user = userRepository.findByEmail(email);
		if (user == null) {
			session.setAttribute("message", "User does not exits with this email !!");
			return "forgot-email-form";
		} else {
			int otp = random.nextInt(999999);
			String to = email;
			String subject = "OTP From SCM";
			String message = "<h1> OTP = " + otp + "</h1>";
			boolean flag = emailService.sendEmail(to, subject, message);
			if (flag) {
				session.setAttribute("otp", otp);
				session.setAttribute("email", email);
				return "verify_otp";
			} else {
				session.setAttribute("message", "Check your email id !!");
				return "forgot-email-form";
			}
		}
	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession session) {
		Integer myOtp = (int) session.getAttribute("otp");
		if (myOtp.equals(otp)) {
			return "password_change_form";
		} else {
			session.setAttribute("message", "You have entered wrong otp!!");
			return "verify_otp";
		}

	}

	@PostMapping("/change-password")
	public String changePassword(HttpSession session, @RequestParam("newPassword") String newPassword,
			@RequestParam("confirmPassword") String confirmPassword) {
		String email = (String) session.getAttribute("email");
		User user = userRepository.findByEmail(email);
		if (newPassword.contentEquals(confirmPassword)) {
			user.setPassword(bCryptPasswordEncoder.encode(newPassword));
			userRepository.save(user);
			session.setAttribute("message", new Message("Your Password is Successfully Changed...", "success"));
			return "redirect:/signin?change=password changed successfully...";
		} else {
			session.setAttribute("message", new Message("Please Enter Correct Confirm Password !!", "danger"));
			return "password_change_form";
		}

	}
}
