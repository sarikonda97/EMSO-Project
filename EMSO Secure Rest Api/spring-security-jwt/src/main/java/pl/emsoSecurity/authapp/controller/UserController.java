package pl.emsoSecurity.authapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.emsoSecurity.authapp.dto.UserSummary;
import pl.emsoSecurity.authapp.security.CurrentUser;
import pl.emsoSecurity.authapp.security.UserPrincipal;
import pl.emsoSecurity.authapp.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /***
     * This endpoint is used to identify the current user based on the JWT token that the user has.
     * @param currentUser
     * @return
     */
    @GetMapping("me")
    @PreAuthorize("hasRole('USER')")
    public UserSummary getCurrentUser(
            @CurrentUser UserPrincipal currentUser)
    {
        return userService.getCurrentUser(currentUser);
    }

}
