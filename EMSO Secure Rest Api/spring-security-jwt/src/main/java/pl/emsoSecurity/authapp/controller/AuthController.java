package pl.emsoSecurity.authapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.emsoSecurity.authapp.dto.JwtAuthenticationResponse;
import pl.emsoSecurity.authapp.dto.LoginRequest;
import pl.emsoSecurity.authapp.dto.SignUpRequest;
import pl.emsoSecurity.authapp.service.AuthService;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /***
     * This api is used to sign-in to the security framework to receive the Auth Token for validation of the user.
     * @param loginRequest
     * @return returns an auth token to be used for authorization of the user.
     */
    @PostMapping("/signin")
    @ResponseStatus(OK)
    public JwtAuthenticationResponse login(
            @Valid @RequestBody LoginRequest loginRequest)
    {
        return authService.authenticateUser(loginRequest);
    }

    /***
     * This api is used to sign-up into the security framework and store the user creds for use in further validation.
     * @param signUpRequest
     * @return returns a confirmation code - 1 for yes and - for no
     */
    @PostMapping("/signup")
    @ResponseStatus(OK)
    public Long register(
            @Valid @RequestBody SignUpRequest signUpRequest)
    {
        return authService.registerUser(signUpRequest);
    }

}
