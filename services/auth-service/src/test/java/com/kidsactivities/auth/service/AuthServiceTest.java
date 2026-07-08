package com.kidsactivities.auth.service;

import com.kidsactivities.auth.dto.request.LoginRequest;
import com.kidsactivities.auth.dto.request.RegisterRequest;
import com.kidsactivities.auth.dto.response.AuthResponse;
import com.kidsactivities.auth.entity.User;
import com.kidsactivities.auth.repository.UserRepository;
import com.kidsactivities.auth.security.JwtService;
import com.kidsactivities.common.event.RabbitConstants;
import com.kidsactivities.common.event.UserRegisteredEvent;
import com.kidsactivities.common.exception.BadRequestException;
import com.kidsactivities.common.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("US-03 / US-04 — AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("US-03: Given email unique When register Then PARENT + JWT + event")
    void register_shouldCreateUserPublishEventAndReturnToken() {
        RegisterRequest request = buildRegisterRequest("new@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");
        assertThat(response.getUser().getRole()).isEqualTo(Role.PARENT);

        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConstants.EXCHANGE),
                eq(RabbitConstants.USER_REGISTERED),
                eventCaptor.capture()
        );
        assertThat(eventCaptor.getValue().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("US-03: Given email exists When register Then 400")
    void register_shouldRejectDuplicateEmail() {
        RegisterRequest request = buildRegisterRequest("existing@example.com");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cet email est déjà utilisé");
    }

    @Test
    @DisplayName("US-04: Given valid credentials When login Then JWT")
    void login_shouldReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("parent@example.com");
        request.setPassword("parent123");

        User user = User.builder()
                .id(1L)
                .email("parent@example.com")
                .firstName("Marie")
                .lastName("Dupont")
                .role(Role.PARENT)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    private RegisterRequest buildRegisterRequest(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("password123");
        request.setFirstName("Jean");
        request.setLastName("Martin");
        return request;
    }
}
