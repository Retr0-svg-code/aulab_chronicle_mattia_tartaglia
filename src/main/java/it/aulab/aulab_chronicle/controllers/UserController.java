package it.aulab.aulab_chronicle.controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.dtos.UserDto;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.services.ArticleService;
import it.aulab.aulab_chronicle.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ArticleService articleService;

    //rotta della home
    @GetMapping("/")
    public String home(Model viewModel) {
        List<ArticleDto> articles = articleService.readAll();
        
        //articoli in ordine decrescente
        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());
        
        List<ArticleDto> lastThreeArticles = articles.stream().limit( 3).collect(Collectors.toList());

        viewModel.addAttribute("articles", lastThreeArticles);
        return "home";
    }

    //rotta per registrarsi
    @GetMapping("/register")
    public String register(Model model){
        model.addAttribute("user", new UserDto());
        return "auth/register";
    }

    //rotta per il login
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                                BindingResult result, 
                                Model model,
                                RedirectAttributes redirectAttributes, 
                                HttpServletRequest request, HttpServletResponse response){
                                
        User existingUser = userService.findUserByEmail(userDto.getEmail());
        
        if(existingUser !=null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()){
            result.rejectValue("email", null, "Email is already in use!");
        }
        if(result.hasErrors()){
            model.addAttribute("user", userDto);
            return "auth/register";
        }

        userService.saveUser(userDto, redirectAttributes, request, response);
        redirectAttributes.addFlashAttribute("successMessage", "User registered successfully");
        return "redirect:/";
    }

    @GetMapping("/search/{id}")
    public String userArticleSearch(@PathVariable("id") Long id, Model viewModel) {
        User user = userService.find(id);
        viewModel.addAttribute("title", "Tutti gli articoli di " + user.getUsername());

        List<ArticleDto> articles = articleService.searchByAuthor(user);
        viewModel.addAttribute("articles", articles);

        return "article/articles";
    }
}
