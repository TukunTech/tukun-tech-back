package com.upc.tukuntech.backend.modules.auth.infrastructure.security;

import com.upc.tukuntech.backend.modules.auth.domain.entity.UserEntity;
import com.upc.tukuntech.backend.modules.auth.domain.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        UserEntity u = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Set<GrantedAuthority> authorities = new HashSet<>();

        u.getRoles().forEach(r -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getName()));
            r.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority("PERM_" + p.getName())));
        });

        return User
                .withUsername(u.getEmail())
                .password(u.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!u.getEnabled())
                .credentialsExpired(false)
                .disabled(!u.getEnabled())
                .build();
    }

}
