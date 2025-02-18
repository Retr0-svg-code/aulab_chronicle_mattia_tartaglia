package it.aulab.aulab_chronicle.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.ArticleRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;

@Service
public class ArticleService implements CrudService<ArticleDto, Article, Long> {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ImageService imageService;

    @Override
    public List<ArticleDto> readAll() {
        List<ArticleDto> dtos=new ArrayList<ArticleDto>();
        for(Article article:articleRepository.findAll()) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }
    @Override
    public ArticleDto read(Long key) {
        Optional<Article> optArticle = articleRepository.findById(key);
        if(optArticle.isPresent()){
            return modelMapper.map(optArticle.get(), ArticleDto.class);
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author id= " + key + " not found");
        }
    }
    @Override
    public ArticleDto create(Article article, Principal principal, MultipartFile file) {
        String url = "";
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = (userRepository.findById(userDetails.getId())).get();
            article.setUser(user);
        }

        if(!file.isEmpty()){
            try{
                CompletableFuture<String> futureUrl = imageService.uploadImage(file);
                url = futureUrl.get();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        article.setIsAccepted(null);

        ArticleDto dto=modelMapper.map(articleRepository.save(article), ArticleDto.class);
        if(!file.isEmpty()){
            imageService.saveImageOnDB(url, article);
        }

        return dto;
    }
    @Override
    public ArticleDto update(Long key, Article updateArticle, MultipartFile file) {
        String url = "";

        //controllo se l'articolo esiste in base all'id
        if(articleRepository.existsById(key)){
            //assegno all'articolo proveniente dal form lo stesso id dell'articolo da modificare
            updateArticle.setId(key);
            //recupero l'articolo originale non modificato
            Article article = articleRepository.findById(key).get();
            //imposto l'utente dell'articolo originale all'articolo proveniente dal form
            updateArticle.setUser(article.getUser());

            //verifico se devo modificare o meno l'immagine
            if(!file.isEmpty()){
                try{
                    //elimino l'immagine precedente
                    imageService.deleteImage(article.getImage().getPath());
                    try{
                        //salvo l'immagine nuova
                        CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                        url = futureUrl.get();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    //salvo il nuovo path nel database
                    imageService.saveImageOnDB(url, updateArticle);

                    //immagine modificata, articolo in revisione
                    updateArticle.setIsAccepted(null);
                    return modelMapper.map(articleRepository.save(updateArticle), ArticleDto.class);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else if(article.getImage() == null){ //se l'articolo originale non ha nulla di nuovo, allora non è stato modificato nulla
                updateArticle.setIsAccepted(article.getIsAccepted());
            }else{
                //se non è stata modificata l'immagine, faccio un check se tutti i campi e se diversi l'articolo torna in revisione

                //se non è stata modificata l'immagine, rimetto la stessa immagine dell'articolo originale
                updateArticle.setImage(article.getImage());

                if(updateArticle.equals(article)==false){
                    updateArticle.setIsAccepted(null);
                }else{
                    updateArticle.setIsAccepted(article.getIsAccepted());
                }

                return modelMapper.map(articleRepository.save(updateArticle), ArticleDto.class);
            }
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return null;
    }
    
    @Override
    public void delete(Long key) {
        if(articleRepository.existsById(key)){
            Article article = articleRepository.findById(key).get();

            try{
                String path = article.getImage().getPath();
                article.getImage().setArticle(null);
                imageService.deleteImage(path);
            }catch(Exception e){
                e.printStackTrace();
            }

            articleRepository.deleteById(key);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public List<ArticleDto> searchByCategory(Category category) {
        List<ArticleDto> dtos=new ArrayList<ArticleDto>();
        for(Article article:articleRepository.findByCategory(category)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    public List<ArticleDto> searchByAuthor(User user) {
        List<ArticleDto> dtos=new ArrayList<ArticleDto>();
        for(Article article:articleRepository.findByUser(user)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    public void setIsAccepted(Boolean result, Long id){
        Article article = articleRepository.findById(id).get();
        article.setIsAccepted(result);
        articleRepository.save(article);
    }

    public List<ArticleDto> search(String keyword){
        List<ArticleDto> dtos=new ArrayList<ArticleDto>();
        for(Article article:articleRepository.search(keyword)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }
    
}
