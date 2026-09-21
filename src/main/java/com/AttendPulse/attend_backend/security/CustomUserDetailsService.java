package com.AttendPulse.attend_backend.security;

import com.AttendPulse.attend_backend.entity.User;
import com.AttendPulse.attend_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Block pending/rejected students
        if (user.getRole() == User.Role.STUDENT) {
            if (user.getStatus() == null ||user.getStatus() == User.Status.PENDING) {
                throw new UsernameNotFoundException("Account pending teacher approval!");
            }
            if (user.getStatus() == User.Status.REJECTED) {
                throw new UsernameNotFoundException("Account rejected by teacher!");
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}