package it.aulab.aulab_chronicle.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.dtos.CategoryDto;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.services.ArticleService;
import it.aulab.aulab_chronicle.services.CategoryService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ModelMapper modelMapper;

    //rotta ricerca articolo per categoria
    @GetMapping("/search/{id}")
    public String categorySearch(@PathVariable("id") Long id, Model viewModel){
        CategoryDto category = categoryService.read(id);
        viewModel.addAttribute("title", "Tutti gli articoli trovati per categoria " + category.getName());

        List<ArticleDto> articles = articleService.searchByCategory(modelMapper.map(category, Category.class));

        List<ArticleDto> acceptedArticles = articles.stream().filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).collect(Collectors.toList());
        viewModel.addAttribute("articles", acceptedArticles);

        return "article/articles";
    }

    //rotta creazione categoria
    @GetMapping("create")
    public String categoryCreate(Model viewModel){
        viewModel.addAttribute("title", "Crea categoria");
        viewModel.addAttribute("category", new CategoryDto());
        return "categories/create";
    }

    @PostMapping
    public String categoryStore(@Valid @ModelAttribute("category") Category category,
                                BindingResult result, 
                                RedirectAttributes redirectAttributes,
                                Model viewModel){
        
        //controllo errori di validazione
        if(result.hasErrors()){
            viewModel.addAttribute("title", "Crea categoria");
            viewModel.addAttribute("category", category);
            return "categories/create";
        }

        categoryService.create(category, null, null);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria creata con successo");

        return "redirect:/admin/dashboard";
    }

    //rotta modifica categoria
    @GetMapping("/edit/{id}")
    public String categoryEdit(@PathVariable("id") Long id, Model viewModel){
        viewModel.addAttribute("title", "Modifica categoria");
        viewModel.addAttribute("category", categoryService.read(id));
        return "category/update";
    }

    //rotta memorizzazione categoria modificata
    @PostMapping("/update/{id}")
    public String categoryUpdate(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("category") Category category,
                                BindingResult result, 
                                RedirectAttributes redirectAttributes,
                                Model viewModel){
        //controllo errori di validazione
        if(result.hasErrors()){
            viewModel.addAttribute("title", "Modifica categoria");
            viewModel.addAttribute("category", category);
            return "categories/update";
        }

        categoryService.update(id, category, null);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria modificata con successo");

        return "redirect:/admin/dashboard";
    }

    //rotta cancellazione categoria
    @GetMapping("/delete/{id}")
    public String categoryDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes){

        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria eliminata con successo");

        return "redirect:/admin/dashboard";
    }
}
