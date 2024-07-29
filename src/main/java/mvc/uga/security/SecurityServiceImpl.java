package mvc.uga.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import mvc.uga.entity.Role;
import mvc.uga.entity.User;
import mvc.uga.repository.UserRepo;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final AuthenticationContext authenticationContext;
    private final UserRepo userRepo;

    public SecurityServiceImpl(AuthenticationContext authenticationContext, UserRepo userRepo) {
        this.authenticationContext = authenticationContext;
        this.userRepo = userRepo;
    }

    public UserDetails getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .orElseThrow(() -> new UserNotAuthenticatedException("User is not authenticated."));
    }

    public void logout() {
        authenticationContext.logout();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user == null || !user.isEnabled()) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        Collection<SimpleGrantedAuthority> authorities = mapRolesToAuthorities(user.getRoles());
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    private Collection<SimpleGrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

}
