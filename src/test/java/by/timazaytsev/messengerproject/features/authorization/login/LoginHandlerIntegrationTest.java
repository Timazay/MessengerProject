package by.timazaytsev.messengerproject.features.authorization.login;

import by.timazaytsev.messengerproject.MessengerProjectApplication;
import by.timazaytsev.messengerproject.api.common.configuration.MinIOConfig;
import by.timazaytsev.messengerproject.api.common.configuration.filter.JwtAuthenticationFilter;
import by.timazaytsev.messengerproject.api.common.service.JwtService;
import by.timazaytsev.messengerproject.configuration.EnvironmentTestConfig;
import by.timazaytsev.messengerproject.features.authorization.common.AuthResponse;
import by.timazaytsev.messengerproject.infrastructure.entity.RefreshToken;
import by.timazaytsev.messengerproject.infrastructure.entity.Role;
import by.timazaytsev.messengerproject.infrastructure.entity.User;
import by.timazaytsev.messengerproject.infrastructure.entity.enums.RoleName;
import by.timazaytsev.messengerproject.infrastructure.repository.RefreshTokenRepository;
import by.timazaytsev.messengerproject.infrastructure.repository.RoleRepository;
import by.timazaytsev.messengerproject.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = MessengerProjectApplication.class)
@Testcontainers(disabledWithoutDocker = true)
@Import(EnvironmentTestConfig.class)
@ActiveProfiles("test")
public class   LoginHandlerIntegrationTest {

    @MockitoSpyBean
    private JwtService jwtService;

    @MockitoBean
    private MinIOConfig minIOConfig;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private LoginHandler loginHandler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role userRole;
    private User testUser;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRole = new Role();
        userRole.setName(RoleName.USER);
        userRole = roleRepository.save(userRole);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setMail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setRoles(List.of(userRole));
        testUser = userRepository.save(testUser);

        User emailUser = new User();
        emailUser.setUsername("emailuser");
        emailUser.setMail("login@example.com");
        emailUser.setPassword(passwordEncoder.encode("Password123!"));
        emailUser.setRoles(List.of(userRole));
        userRepository.save(emailUser);

        validLoginRequest = new LoginRequest("testuser", "Password123!");
    }

    @Test
    void authenticate_ShouldReturnAuthResponse_WhenValidCredentialsByUsername() {
        // Arrange
        lenient().doReturn("access_token")
                .when(jwtService).generateAccessToken(any(User.class));

        lenient().doReturn(RefreshToken.builder()
                        .token("refresh_token")
                        .user(testUser)
                        .expiryDate(Instant.now().plusSeconds(60))
                        .build())
                .when(jwtService).generateRefreshToken(any(User.class));

        lenient().doReturn(Date.from(Instant.now()))
                .when(jwtService).extractAccessExpiration("access_token");

        // Act
        AuthResponse response = loginHandler.authenticate(validLoginRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertTrue(response.expiresIn() > 0);
        assertTrue(response.expiresIn() <= new Date().getTime() + 3600000); // 1 час максимум
    }

    @Test
    void authenticate_ShouldThrowBadCredentialsException_WhenInvalidPassword() {
        // Act & Assert
        assertThrows(BadCredentialsException.class,
                () -> loginHandler.authenticate(new LoginRequest("usertest", "123Password!")));

        // Проверяем, что refresh token НЕ был создан
        assertEquals(0, refreshTokenRepository.count());

        // Проверяем, что jwtService НЕ вызывался
        verify(jwtService, never()).generateAccessToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }
}
