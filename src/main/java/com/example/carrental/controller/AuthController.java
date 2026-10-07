package com.example.carrental.controller;

import com.example.carrental.dto.UserRegistrationDto;
import com.example.carrental.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username/email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Account created successfully! Please sign in with your credentials.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new UserRegistrationDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegistration(@Valid @ModelAttribute("registrationDto") UserRegistrationDto dto,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration complete! You may now sign in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("registrationError", ex.getMessage());
            return "auth/register";
        }
    }
}
