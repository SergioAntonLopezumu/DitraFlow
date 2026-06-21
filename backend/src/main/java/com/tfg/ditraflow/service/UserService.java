package com.tfg.ditraflow.service;

import com.tfg.ditraflow.model.dto.UserRegisterDTO;
import com.tfg.ditraflow.model.entity.User;
import com.tfg.ditraflow.model.entity.CompanySize;
import com.tfg.ditraflow.model.entity.IndustrySector;
import com.tfg.ditraflow.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerNewUser(UserRegisterDTO registerDto) {
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Este correo electrónico ya está registrado en el sistema.");
        }

        User newUser = new User();
        newUser.setEmail(registerDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        newUser.setCompanyName(registerDto.getCompanyName());
        
        if (registerDto.getCompanySize() != null && !registerDto.getCompanySize().trim().isEmpty()) {
            try {
                CompanySize companySize = CompanySize.valueOf(registerDto.getCompanySize().toUpperCase());
                newUser.setCompanySize(companySize);
            } catch (IllegalArgumentException e) {
                newUser.setCompanySize(null);
            }
        }

        if (registerDto.getIndustrySector() != null && !registerDto.getIndustrySector().trim().isEmpty()) {
            try {
                IndustrySector sector = IndustrySector.fromCode(registerDto.getIndustrySector());
                newUser.setIndustrySector(sector);
            } catch (IllegalArgumentException e) {
                newUser.setIndustrySector(null);
            }
        }

        return userRepository.save(newUser);
    }

    public boolean hasCompletedSurvey(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getResults() != null && !user.getResults().isEmpty();
        }
        
        return false;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el email: " + email));
    }

    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional 
    public void resetProgress(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // B. Limpiar la lista en memoria
        if (user.getResults() != null) {
            user.getResults().clear();
        }
        
        // C. USAR EL REPOSITORIO PARA GUARDAR (NO user.save)
        userRepository.save(user); 
    }

    @Transactional 
    public void deleteAccount(String email) {
        // Mejor práctica para el borrado en cascada:
        // 1. Buscar la entidad cargada
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado para eliminar"));
        
        // 2. Usar el repositorio para borrar la entidad cargada
        // Esto permite que JPA procese la anotación @OneToMany(cascade = CascadeType.ALL)
        userRepository.delete(user);
    }
}