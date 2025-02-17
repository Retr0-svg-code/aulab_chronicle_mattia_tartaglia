package it.aulab.aulab_chronicle.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.models.CareerRequest;
import it.aulab.aulab_chronicle.models.Role;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.RoleRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;
import it.aulab.aulab_chronicle.services.CareerRequestService;


@Controller
@RequestMapping("/operations")
public class OperationController {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CareerRequestService careerRequestService;

    //rotta richiesta collab
    @GetMapping("/career/request")
    public String careerRequestCreate(Model viewModel){
        viewModel.addAttribute("title", "Richiesta di collaborazione");
        viewModel.addAttribute("careerRequest", new CareerRequest());

        List<Role> roles = roleRepository.findAll();
        //rimozione ruolo user dal form
        roles.removeIf(e -> e.getName().equals("ROLE_USER"));
        viewModel.addAttribute("roles", roles);

        return "career/requestForm";
    }

    @PostMapping("/career/request/save")
    public String careerRequestStore(@ModelAttribute("careerRequest") CareerRequest careerRequest, Principal principal, RedirectAttributes redirectAttributes){
        User user = userRepository.findByEmail(principal.getName());

        if(careerRequestService.isRoleAlreadyAssigned(user, careerRequest)){
            redirectAttributes.addFlashAttribute("errorMessage", "Ruolo già assegnato!");
            return "redirect:/";
        }

        careerRequestService.save(careerRequest, user);

        redirectAttributes.addFlashAttribute("successMessage", "Richiesta inviata con successo!");
        return "redirect:/";
    }

    //rotta dettaglio della richiesta
    @GetMapping("/career/requests/detail/{id}")
    public String careerRequestDetail(@PathVariable("id") Long id, Model viewModel){
        viewModel.addAttribute("title", "Dettaglio richiesta");
        viewModel.addAttribute("careerRequest", careerRequestService.find(id));

        return "career/requestDetail";
    }

    //rotta accettazione della richiesta
    @PostMapping("/career/requests/accept/{requestId}")
    public String careerRequestAccept(@PathVariable Long requestId, RedirectAttributes redirectAttributes){
        careerRequestService.careerAccept(requestId);
        redirectAttributes.addFlashAttribute("successMessage", "Ruolo abilitato con successo!");

        return "redirect:/admin/dashboard";
    }
}
