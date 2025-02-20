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

    @GetMapping("/search/{id}")
    public String categorySearch(@PathVariable("id") Long id, Model viewModel) {
        CategoryDto category = categoryService.read(id);

        viewModel.addAttribute("title", "Tutti gli articoli trovati per categoria " + category.getName());

        List<ArticleDto> articles = articleService.searchByCategory(modelMapper.map(category, Category.class));

        List<ArticleDto> acceptedArticles = articles.stream().filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).collect(Collectors.toList());
        viewModel.addAttribute("articles", acceptedArticles);

        return "article/articles";
    }

    @GetMapping("create")
    public String categoryCreate(Model viewModel) {
        viewModel.addAttribute("title", "Crea una categoria");
        viewModel.addAttribute("category", new Category());
        return "category/create";
    }

    @PostMapping
    public String categoryStore(@Valid @ModelAttribute("category") Category category,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model viewModel) {
        if(result.hasErrors()) {
            viewModel.addAttribute("title", "Crea una categoria");
            viewModel.addAttribute("category", category);
            return "category/create";
        }

        categoryService.create(category, null, null);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria aggiunta con successo!");

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String categoryEdit (@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Modifica categoria");
        viewModel.addAttribute("category", categoryService.read(id));
        return "category/update";
    }

    @PostMapping("/update/{id}")
    public String categoryUpdate(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("category") Category category,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model viewModel) {
        if(result.hasErrors()) {
            viewModel.addAttribute("title", "Modifica categoria");
            viewModel.addAttribute("category", category);
            return "category/update";
        }

        categoryService.update(id, category, null);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria modificata con successo!");

        return "redirect:/admin/dashboard"; 
    }

    @GetMapping("delete/{id}")
    public String categoryDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria cancellata con successo!");

        return "redirect:/admin/dashboard";
    }
}
