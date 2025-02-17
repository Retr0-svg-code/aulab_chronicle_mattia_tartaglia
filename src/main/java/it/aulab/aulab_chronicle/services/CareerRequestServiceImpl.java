package it.aulab.aulab_chronicle.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.aulab.aulab_chronicle.models.CareerRequest;
import it.aulab.aulab_chronicle.models.Role;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.CareerRequestRepository;
import it.aulab.aulab_chronicle.repositories.RoleRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;

@Service
public class CareerRequestServiceImpl implements CareerRequestService {
    @Autowired
    private CareerRequestRepository careerRequestRepository;
    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public boolean isRoleAlreadyAssigned(User user, CareerRequest careerRequest) {
        List<Long> allUserIds = careerRequestRepository.findAllUserIds();

        if(!allUserIds.contains(user.getId())){
            return false;
        }

        List<Long> request = careerRequestRepository.findAllByUserId(user.getId());
        return request.stream().anyMatch(roleId -> roleId.equals(careerRequest.getId()));
    }

    public void save(CareerRequest careerRequest, User user) {
        careerRequest.setUser(user);
        careerRequest.setIsChecked(false);
        careerRequestRepository.save(careerRequest);

        // invio mail di richiesta ruolo
        emailService.sendSimpleEmail("adminAulabpost@admin.com", "Richiesta per ruolo: " + careerRequest.getRole().getName(), "C'è una nuova richiesta di collaborazione da " + user.getUsername());
    }

    @Override
    public void careerAccept(Long requestId) {
        //recupero richiesta
        CareerRequest request = careerRequestRepository.findById(requestId).get();

        //recupero utente e ruolo dalla richiesta
        User user = request.getUser();
        Role role = request.getRole();

        //recupero ruoli già posseduti dall'utente
        List<Role> rolesUser = user.getRoles();
        Role newRole = roleRepository.findByName(role.getName());
        rolesUser.add(newRole);

        //salvo le modifiche
        user.setRoles(rolesUser);
        userRepository.save(user);
        request.setIsChecked(true);
        careerRequestRepository.save(request);

        emailService.sendSimpleEmail(user.getEmail(), "Ruolo abilitato", "Ciao, la tua richiesta di collaborazioe è stata accettata!");

    }

    @Override
    public CareerRequest find(Long id){
        return careerRequestRepository.findById(id).get();
    }
}
