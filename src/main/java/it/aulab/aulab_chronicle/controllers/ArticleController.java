package it.aulab.aulab_chronicle.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.dtos.CategoryDto;
import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.repositories.ArticleRepository;
import it.aulab.aulab_chronicle.services.ArticleService;
import it.aulab.aulab_chronicle.services.CrudService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/articles")
public class ArticleController {
    //rotta creazione articolo
    @Autowired
    @Qualifier("categoryService")
    private CrudService<CategoryDto, Category, Long> categoryService;
    
    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ModelMapper modelMapper;
    
    //rotta index articoli
    @GetMapping
    public String articlesIndex(Model viewModel){
        viewModel.addAttribute("title", "Articoli");
        
        List<ArticleDto> articles = new ArrayList<ArticleDto>();
        for(Article article : articleRepository.findByIsAcceptedTrue()){
            articles.add(modelMapper.map(article, ArticleDto.class));
        }
        
        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());
        viewModel.addAttribute("articles", articles);
        
        return "article/articles";
    }
    
    @GetMapping("create")
    public String articleCreate(Model viewModel){ 
        viewModel.addAttribute("title", "Crea articolo");
        viewModel.addAttribute("article", new Article());
        viewModel.addAttribute("categories", categoryService.readAll());
        return "articles/create";
    }
    
    //rotta store articolo
    @PostMapping
    public String articleStore(@Valid @ModelAttribute("article") Article article, 
    BindingResult result, 
    Model viewModel,
    RedirectAttributes redirectAttributes,
    Principal principal,
    MultipartFile file) {
        
        //controllo errori validazione
        if(result.hasErrors()){
            viewModel.addAttribute("title", "Crea articolo");
            viewModel.addAttribute("categories", categoryService.readAll());
            viewModel.addAttribute("categories", categoryService.readAll());
            return "articles/create";
        }
        
        articleService.create(article, principal, file);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo creato con successo!");
        return "redirect:/";
    }
    
    //rotta dettaglio articolo
    @GetMapping("detail/{id}")
    public String detailArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article detail");
        viewModel.addAttribute("article", articleService.read(id));
        return "article/detail";
    }

    //rotta modifica articolo
    @GetMapping("edit/{id}")
    public String editArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Modifica articolo");
        viewModel.addAttribute("article", articleService.read(id));
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/edit";
    }

    //rotta memorizzazione modifica articolo
    @PostMapping("/update/{id}")
    public String articleUpdate(@PathVariable("id") Long id, 
                                @Valid @ModelAttribute("article") Article article, 
                                BindingResult result, 
                                Model viewModel, 
                                RedirectAttributes redirectAttributes,
                                Principal principal, 
                                MultipartFile file) {
        
        if(result.hasErrors()){
            viewModel.addAttribute("title", "Modifica articolo");
            article.setImage(articleService.read(id).getImage());
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/edit";
        }
        
        articleService.update(id, article, file);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo modificato con successo!");

        return "redirect:/articles";
    }
    
    //rotta cancellazione articolo
    @GetMapping("delete/{id}")
    public String articleDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        articleService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo eliminato con successo!");
        return "redirect:/writer/dashboard";
    }
    
    //rotta dettaglio articolo per il revisore
    @GetMapping("revisor/detail/{id}")
    public String revisorDetailArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article detail");
        viewModel.addAttribute("article", articleService.read(id));
        return "revisor/detail";
    }
    
    //rotta dedicata all'azione del revisore
    @PostMapping("/accept")
    public String articleIsAccepted(@RequestParam("action") String action, @RequestParam("articleId") Long articleId, RedirectAttributes redirectAttributes) {
        
        if(action.equals("accept")){
            articleService.setIsAccepted(true, articleId);
            redirectAttributes.addFlashAttribute("resultMessage", "Articolo accettato con successo!");
        } else if(action.equals("reject")){
            articleService.setIsAccepted(false, articleId);
            redirectAttributes.addFlashAttribute("resultMessage", "Articolo rifiutato");
        }else{
            redirectAttributes.addFlashAttribute("resultMessage", "Azione non corretta");
        }
        
        return "redirect:/revisor/dashboard";
    }

    //rotta ricerca articolo
    @GetMapping("/search")
    public String articleSearch(@Param("keyword") String keyword, Model viewModel) {
        viewModel.addAttribute("title", "Articoli trovati");

        List<ArticleDto> articles = articleService.search(keyword);

        List<ArticleDto> acceptedArticles = articles.stream().filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);

        return "article/articles";
    }
}
