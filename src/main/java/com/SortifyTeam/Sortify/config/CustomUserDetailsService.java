package com.SortifyTeam.Sortify.config;

import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + username));

        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            throw new DisabledException("Akun Anda telah dinonaktifkan. Silakan hubungi admin.");
        }
        if (user.getAccountStatus() == User.AccountStatus.BANNED) {
            throw new DisabledException("Akun Anda telah diblokir karena melanggar ketentuan.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}


