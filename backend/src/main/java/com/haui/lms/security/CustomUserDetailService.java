package com.haui.lms.security;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.entity.User;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(404, ErrorMessage.User.USER_NOT_EXISTED));
        return new CustomUserDetails(user);
    }
}
