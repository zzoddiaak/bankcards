package example.bankcards.security;

import example.bankcards.entity.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class UserDetailsConfig implements UserDetails {
    @Getter
    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsConfig(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        this.id = user.getId();
        this.username = Objects.requireNonNull(user.getUsername(), "Username cannot be null");
        this.password = Objects.requireNonNull(user.getPassword(), "Password cannot be null");

        this.authorities = user.getRoles() == null
                ? Collections.emptyList()
                : user.getRoles().stream()
                .filter(Objects::nonNull)
                .peek(role -> {
                    if (role.getName() == null || role.getName().isBlank()) {
                        log.warn("Role with id {} has empty name", role.getId());
                    }
                })
                .map(role -> {
                    String authorityName = role.getName().startsWith("ROLE_")
                            ? role.getName()
                            : "ROLE_" + role.getName();
                    return new SimpleGrantedAuthority(authorityName);
                })
                .collect(Collectors.toList());

        log.debug("Created UserDetails for {} with authorities: {}",
                username,
                authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableCollection(authorities);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "UserDetailsConfig{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", authorities=" + authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()) +
                '}';
    }
}