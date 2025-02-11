package it.aulab.aulab_chronicle.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.aulab.aulab_chronicle.dtos.UserDto;
import it.aulab.aulab_chronicle.services.UserService;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    //rotta della home
    @GetMapping("/")
    public String home() {
        return "home";
    }

    //rotta per registrarsi
    @GetMapping("/register")
    public String register(Model model){
        model.addAttribute("user", new UserDto());
        return "auth/register";
    }
}
