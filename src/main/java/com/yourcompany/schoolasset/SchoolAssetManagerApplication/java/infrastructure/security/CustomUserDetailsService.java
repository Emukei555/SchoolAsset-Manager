package com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.infrastructure.security;

import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.user.User;
import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.domain.model.user.UserRepository;
import com.yourcompany.schoolasset.SchoolAssetManagerApplication.java.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserDetails(user);
    }
}