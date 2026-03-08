package com.urbanthreads.backend.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.urbanthreads.backend.dto.RegisterRequest;
import com.urbanthreads.backend.dto.LoginRequest;
import com.urbanthreads.backend.security.JwtUtil;
import com.urbanthreads.backend.user.Role;
import com.urbanthreads.backend.user.User;
import com.urbanthreads.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);
    }
    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(user.getEmail());
    }
    public String googleLogin(String googleToken) {

        try {

            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(),
                            new JacksonFactory())
                            .setAudience(
                                    java.util.Collections.singletonList(
                                            "264048787060-cjc26fnrg5mk965c3k6pqokdn4a5js71.apps.googleusercontent.com"
                                    )
                            )
                            .build();

            GoogleIdToken idToken = verifier.verify(googleToken);

            if (idToken == null) {
                throw new RuntimeException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {

                        User newUser = new User();

                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setPassword(passwordEncoder.encode("GOOGLE_AUTH_USER"));
                        newUser.setRole(Role.USER);

                        return userRepository.save(newUser);

                    });

            return jwtUtil.generateToken(user.getEmail());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }
}