//package com.sqlcanvas.todoapi.auth;
//
//import com.sqlcanvas.todoapi.user.domain.Email;
//import com.sqlcanvas.todoapi.user.domain.User;
//import com.sqlcanvas.todoapi.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    @Transactional(readOnly = true)
//    public UserDetails loadUserByUsername(String emailStr) {
//        // 1. 検索 (UserEntity ではなく User)
//        Email email = new Email(emailStr);
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));
//
//        // 2. 変換
//        // ★ここがエラーの原因だったはず。「entity」ではなく「user」を渡す
//        return new CustomUserDetails(user);
//    }
//}