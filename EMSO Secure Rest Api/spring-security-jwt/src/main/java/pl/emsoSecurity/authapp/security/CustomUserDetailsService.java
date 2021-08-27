package pl.emsoSecurity.authapp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.emsoSecurity.authapp.exception.NotFoundException;
import pl.emsoSecurity.authapp.model.User;
import pl.emsoSecurity.authapp.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /***
     * This method is used to load the user by their username.
     * @param email
     * @return UserDetails
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("User not found [email: " + email + "]")
                );

        return UserPrincipal.create(user);
    }

    /***
     * This method is used to load the user by their user id.
     * @param id
     * @return UserDetails
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User not found [id: " + id + "]")
        );

        return UserPrincipal.create(user);
    }
}
