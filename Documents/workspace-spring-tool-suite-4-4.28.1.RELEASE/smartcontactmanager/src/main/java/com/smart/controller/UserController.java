package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
//import org.springframework.data.jpa.domain.JpaSort.Path;
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

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

import java.nio.file.Path;
import java.nio.file.Paths;


@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	//method for adding common data 
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		System.out.println("UserName"+userName);
		
		//get the user using username(Email)
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("USER"+user);
		
		model.addAttribute("user",user);
	}
	
	//dashboard home
	@GetMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session)
	{
		try {
			String name=principal.getName();
			User user=this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			if(file.isEmpty()) {
				System.out.println("File is empty");
				contact.setImage("contact.png");
				
			}else {
				//file 
				contact.setImage(file.getOriginalFilename());
				String uploadDir = System.getProperty("user.home") + File.separator + "smart_contact_images";
				File saveDir = new File(uploadDir);
				if (!saveDir.exists()) {
				    saveDir.mkdirs();
				}
				Path path = Paths.get(uploadDir + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

//				System.out.println("Path: " + saveFile.getAbsolutePath());
				System.out.println("Image is uploaded");
			}
			
			
			user.getContacts().add(contact);
			contact.setUser(user);

			this.userRepository.save(user);
			System.out.println("DATA"+contact);
			System.out.println("Added to data base");
			//message success......
			session.setAttribute("message", new Message("your contact is added!! Add more..","success"));
		}catch(Exception e) {
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			//message error..
			session.setAttribute("message", new Message("Something went wrong!! Try again..","danger"));

		}	
//		session.removeAttribute("message");

		return "redirect:/user/add-contact";
		
	}
	//show contacts
	//per page=5[n]
	//current page=0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(
	    @PathVariable(name = "page", required = false) Integer page, 
	    Model m, 
	    Principal principal) {
	    
	    // Set default page if null
	    if (page == null) {
	        page = 0;
	    }
	    
	    String userName = principal.getName();
	    User user = this.userRepository.getUserByUserName(userName);
	    
	    // Create pageable request
	    Pageable pageable = PageRequest.of(page, 5);
	    Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
	    
	    // Handle out-of-bound pages
	    if (contacts.getTotalPages() > 0 && page >= contacts.getTotalPages()) {
	        page = contacts.getTotalPages() - 1;
	        return "redirect:/user/show-contacts/" + page;
	    }
	    if (page < 0) {
	        page = 0;
	        return "redirect:/user/show-contacts/" + page;
	    }
	    
	    // Add attributes to model
	    m.addAttribute("contacts", contacts);
	    m.addAttribute("currentPage", page); // 0-based
	    m.addAttribute("totalPages", contacts.getTotalPages());
	    
	    // Generate page numbers list (1-based for display)
	    List<Integer> pageNumbers = IntStream.rangeClosed(1, contacts.getTotalPages())
	        .boxed()
	        .collect(Collectors.toList());
	    m.addAttribute("pageNumbers", pageNumbers);
	    
	    return "normal/show_contacts";
	}
	
	//showing perticular contact details
	@GetMapping("/{Cid}/contact")
	public String showContactDetail(@PathVariable("Cid") Integer Cid,Model model,Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(Cid);
		Contact contact = contactOptional.get();
		//
		String userName=principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getId()==contact.getUser().getId()) {
		model.addAttribute("contact",contact);
		model.addAttribute("title",contact.getName());
		}
		System.out.println("CID"+Cid);
		return "normal/contact_detail";
	}
	//delete contact handler
	@GetMapping("/delete/{Cid}")
	public String deleteContact(@PathVariable("Cid") Integer Cid,Model model,HttpSession session,Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(Cid);
		Contact contact=contactOptional.get();
		System.out.println("contact"+contact.getCid());
		
//		contact.setUser(null);
		//check
//		this.contactRepository.delete(contact);
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		session.setAttribute("message", new Message("contact delete succesfully...","success"));
		return "redirect:/user/show-contacts/0";
	}
	
	//open update contact handler
	@PostMapping("/update-contact/{Cid}")
	public String updateForm(@PathVariable("Cid") Integer Cid,Model m) {
		m.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(Cid).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	//upadte contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal) {
		try {
			//old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();
			
			//image
			if(!file.isEmpty()) {
				//file work
				//rewrite
				//delete new photo
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				//update new photo
				File saveFile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath() + File.separator +file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message",new Message("Your Contact is updated..","success"));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		System.out.println("CONTACT NAME"+contact.getName());
		System.out.println("CONTACT ID"+contact.getCid());
		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","profile page");
		return "normal/profile";
	}

}
