package pl.emsoSecurity.authapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.emsoSecurity.authapp.exception.AppException;
import pl.emsoSecurity.authapp.exception.ConflictException;
import pl.emsoSecurity.authapp.model.Role;
import pl.emsoSecurity.authapp.model.RoleName;
import pl.emsoSecurity.authapp.model.User;
import pl.emsoSecurity.authapp.security.JwtTokenProvider;
import pl.emsoSecurity.authapp.security.UserPrincipal;
import pl.emsoSecurity.authapp.repository.UserRepository;
import pl.emsoSecurity.authapp.dto.JwtAuthenticationResponse;
import pl.emsoSecurity.authapp.dto.LoginRequest;
import pl.emsoSecurity.authapp.dto.SignUpRequest;
import pl.emsoSecurity.authapp.repository.RoleRepository;

import java.util.Collections;

@Service
@Slf4j
public class AuthService {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /***
     * This method is used to authenticate the user based on the JWT token
     * @param loginRequest
     * @return JWTAuthenticationResponse
     */
    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        log.info("User with [email: {}] has logged in", userPrincipal.getEmail());

        return new JwtAuthenticationResponse(jwt);
    }

    public Long registerUser(SignUpRequest signUpRequest) {

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ConflictException("Email [email: " + signUpRequest.getEmail() + "] is already taken");
        }

        User user = new User(signUpRequest.getName(), signUpRequest.getEmail(), signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set. Add default roles to database."));

        user.setRoles(Collections.singleton(userRole));

        log.info("Successfully registered user with [email: {}]", user.getEmail());

        return userRepository.save(user).getId();
        }
}
