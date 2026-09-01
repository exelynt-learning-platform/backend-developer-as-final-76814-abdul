package com.exelynt.booking.service;

import com.exelynt.booking.dto.request.LoginRequest;
import com.exelynt.booking.dto.request.RegisterRequest;
import com.exelynt.booking.dto.response.LoginResponse;
import com.exelynt.booking.dto.response.UserResponse;
import com.exelynt.booking.exception.DuplicateResourceException;
import com.exelynt.booking.model.Role;
import com.exelynt.booking.model.User;
import com.exelynt.booking.repository.UserRepository;
import com.exelynt.booking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    /** Public self-registration always creates a USER (never ADMIN) to prevent privilege escalation. */
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    public LoginResponse login(LoginRequest request) {
        // Delegates to Spring Security's AuthenticationManager, which uses our
        // DaoAuthenticationProvider + BCryptPasswordEncoder to verify the password.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("User vanished after authentication"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails, user.getRole().name());

        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }
}
