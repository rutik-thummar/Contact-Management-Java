package com.contact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.contact.helper.Message;
import com.contact.model.Contact;
import com.contact.model.MyOrder;
import com.contact.model.User;
import com.contact.repository.ContactRepository;
import com.contact.repository.MyOrderRepository;
import com.contact.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ContactRepository contactRepository;

	@Autowired
	MyOrderRepository myOrderRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		User user = userRepository.findByEmail(principal.getName());
		model.addAttribute("user", user);
	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String addContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String addContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			User user = userRepository.findByEmail(principal.getName());
			if (file.isEmpty()) {
				contact.setImage("contact1.png");
			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			user.getContacts().add(contact);
			contact.setUser(user);
			userRepository.save(user);
			session.setAttribute("message", new Message("Your Contact is Added !! Add more...", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !! Try again...", "danger"));
		}
		return "normal/add_contact_form";
	}

	@RequestMapping("/view")
	public String viewUser(Model model, Principal principal) {
		model.addAttribute("title", "Show User Contacts");
		User user = userRepository.findByEmail(principal.getName());
		List<Contact> contacts = contactRepository.findContactsByUser(user.getId());
		model.addAttribute("contacts", contacts);
		return "normal/show_contact";
	}

	@RequestMapping("/contact/{cId}")
	public String showContactDetail(@PathVariable("cId") int cId, Model model, Principal principal) {
		model.addAttribute("title", "Show User Contacts");
		Contact contact = contactRepository.findById(cId).get();
		User user = userRepository.findByEmail(principal.getName());
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		return "normal/contact_detail";
	}

	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") int cId, Model model, HttpSession session) {
		contactRepository.deleteById(cId);
		session.setAttribute("message", new Message("Contact Deleted Successfully...", "success"));
		return "redirect:/user/view";
	}

	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") int cId, Model model) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}

	@PostMapping(value = "/process-update")
	public String update(Model model, @ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			HttpSession session, Principal principal) {
		try {
			Contact oldContact = contactRepository.findById(contact.getcId()).get();
			if (!file.isEmpty()) {
//				File deleteFile=new ClassPathResource("static/img").getFile();
//				File file1=new File(deleteFile,oldContact.getImage());
//				file1.delete();
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			} else {
				contact.setImage(oldContact.getImage());
			}
			User user = userRepository.findByEmail(principal.getName());
			contact.setUser(user);
			contactRepository.save(contact);
			session.setAttribute("message", new Message("Your Contact is Update...", "success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/contact/" + contact.getcId();
	}

	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile");
		return "normal/profile";
	}

	@GetMapping("/setting")
	public String setting(Model model) {
		model.addAttribute("title", "Setting");
		return "normal/setting";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, @RequestParam("confirmPassword") String confirmPassword,
			Principal principal, HttpSession session) {
		User user = userRepository.findByEmail(principal.getName());
		if (newPassword.contentEquals(confirmPassword)) {
			if (bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
				user.setPassword(bCryptPasswordEncoder.encode(newPassword));
				userRepository.save(user);
				session.setAttribute("message", new Message("Your Password Is Successfully Changed...", "success"));
			} else {
				session.setAttribute("message", new Message("Please Enter Correct Old Password !!", "danger"));
				return "redirect:/user/setting";
			}
		} else {
			session.setAttribute("message", new Message("Please Enter Correct Confirm Password !!", "danger"));
			return "redirect:/user/setting";
		}
		return "redirect:/user/index";
	}

	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {
		int amt = Integer.parseInt(data.get("amount").toString());
		RazorpayClient client = new RazorpayClient("rzp_test_xaKAiszSfHigUk", "lRe8mKYaJc0nuFTjfmRfZnmw");
		JSONObject ob = new JSONObject();
		ob.put("amount", amt * 100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_235425");
		Order order = client.orders.create(ob);
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(amt);
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setReceipt(order.get("receipt"));
		myOrder.setUser(userRepository.findByEmail(principal.getName()));
		myOrderRepository.save(myOrder);
		return order.toString();
	}

	@PostMapping("/update_order")
	@ResponseBody
	public ResponseEntity<Map<String, String>> updateOrder(@RequestBody Map<String, Object> data, Principal principal) {
		MyOrder myOrder = myOrderRepository.fingByOrderId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		myOrderRepository.save(myOrder);
		return ResponseEntity.ok(Map.of("msg", "updated"));
	}
}
