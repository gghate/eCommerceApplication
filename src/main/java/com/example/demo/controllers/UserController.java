package com.example.demo.controllers;

import java.util.Optional;

import com.example.demo.ExceptionHandling.CustomException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestController
@RequestMapping("/api/user")
public class UserController {
	private static Logger logger = LogManager.getLogger(UserController.class);
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		return ResponseEntity.of(userRepository.findById(id));
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}
	
	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) throws CustomException{
		User user = new User();
		user.setUsername(createUserRequest.getUsername());
       if(createUserRequest.getPassword().length()<7)
	   {
		   logger.error("Failed creating user. Password length should be greater than or equal to 7");
		   throw new CustomException("Password length should greater than or equal to 7");
	   }

       if(!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword()))
	   {
		   logger.error("Failed creating user. Password and confirmed password do not match");
		   throw new CustomException("Password and confirmed password do not match");
	   }
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
		Cart cart = new Cart();
		cartRepository.save(cart);
		user.setCart(cart);
		try{
			userRepository.save(user);
			logger.info("User created with username : "+user.getUsername());
		}
		catch (Exception e)
		{
			logger.error("Failed creating user");
			throw new CustomException(e.getMessage());

		}

		return ResponseEntity.ok(user);
	}
	
}
