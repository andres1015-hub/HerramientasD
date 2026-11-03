package com.example.ProyectoDesarrollo.Controllers;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Principal principal) {
        if (principal != null && !(principal instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "Login";
    }
}
