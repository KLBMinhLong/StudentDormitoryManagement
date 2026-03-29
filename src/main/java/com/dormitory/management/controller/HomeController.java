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
        return "forward:/user/home.html";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "forward:/admin/index.html";
    }

    @GetMapping("/buildings")
    public String buildingsPage() {
        return "forward:/admin/buildings.html";
    }

    @GetMapping("/admin/buildings")
    public String adminBuildingsPage() {
        return "forward:/admin/buildings.html";
    }

    @GetMapping("/rooms")
    public String roomsPage() {
        return "forward:/admin/rooms.html";
    }

    @GetMapping("/admin/rooms")
    public String adminRoomsPage() {
        return "forward:/admin/rooms.html";
    }

    @GetMapping("/admin/beds")
    public String adminBedsPage() {
        return "forward:/admin/beds.html";
    }
}