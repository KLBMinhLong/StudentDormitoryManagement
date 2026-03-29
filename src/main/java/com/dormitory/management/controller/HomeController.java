package com.dormitory.management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "forward:/register.html";
    }

    @GetMapping("/home")
    public String homePage() {
        return "forward:/home.html";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "forward:/admin.html";
    }
}