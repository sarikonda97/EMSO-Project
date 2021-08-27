package pl.emsoSecurity.authapp.service;

import org.springframework.stereotype.Service;
import pl.emsoSecurity.authapp.security.UserPrincipal;
import pl.emsoSecurity.authapp.dto.UserSummary;

@Service
public class UserService {

    /***
     * This method is used to get the current user by passing the user principal pojo to it.
     * @param userPrincipal
     * @return UserSummary
     */
    public UserSummary getCurrentUser(UserPrincipal userPrincipal) {
        return UserSummary.builder()
                .id(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .name(userPrincipal.getName())
                .build();
    }
}
