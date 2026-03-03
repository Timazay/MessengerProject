package by.timazaytsev.messengerproject.features.authorization.login;

import by.timazaytsev.messengerproject.api.common.exception.NotFoundException;
import by.timazaytsev.messengerproject.api.common.security.service.JwtService;
import by.timazaytsev.messengerproject.features.authorization.common.AuthResponse;
import by.timazaytsev.messengerproject.infrastructure.entity.RefreshToken;
import by.timazaytsev.messengerproject.infrastructure.entity.User;
import by.timazaytsev.messengerproject.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginHandlerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginMapper loginMapper;

    @InjectMocks
    private LoginHandler loginHandler;

    private LoginRequest loginRequest;
    private User user;
    private RefreshToken refreshToken;
    private AuthResponse expectedAuthResponse;
    private Date expirationDate;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "password123");
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setMail("test@example.com");

        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-value");
        refreshToken.setUser(user);

        expirationDate = new Date();
        expectedAuthResponse = new AuthResponse("access-token", "refresh-token-value", expirationDate.getTime());
    }

    @Test
    void authenticate_ShouldAuthenticateAndReturnAuthResponse_WhenUserFoundByUsername() {
        // Arrange
        when(userRepository.findUserByUsername(loginRequest.emailOrUsername()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
        when(jwtService.extractAccessExpiration("access-token")).thenReturn(expirationDate);
        when(loginMapper.toAuthResponse("access-token", refreshToken.getToken(), expirationDate.getTime()))
                .thenReturn(expectedAuthResponse);

        // Act
        AuthResponse result = loginHandler.authenticate(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAuthResponse, result);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.emailOrUsername(),
                        loginRequest.password()
                )
        );
        verify(userRepository).findUserByUsername(loginRequest.emailOrUsername());
        verify(userRepository, never()).findUserByMail(anyString());
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshToken(user);
        verify(jwtService).extractAccessExpiration("access-token");
        verify(loginMapper).toAuthResponse("access-token", refreshToken.getToken(), expirationDate.getTime());
    }

    @Test
    void authenticate_ShouldAuthenticateAndReturnAuthResponse_WhenUserFoundByMail() {
        // Arrange
        when(userRepository.findUserByUsername(loginRequest.emailOrUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findUserByMail(loginRequest.emailOrUsername()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
        when(jwtService.extractAccessExpiration("access-token")).thenReturn(expirationDate);
        when(loginMapper.toAuthResponse("access-token", refreshToken.getToken(), expirationDate.getTime()))
                .thenReturn(expectedAuthResponse);

        // Act
        AuthResponse result = loginHandler.authenticate(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAuthResponse, result);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findUserByUsername(loginRequest.emailOrUsername());
        verify(userRepository).findUserByMail(loginRequest.emailOrUsername());
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    void authenticate_ShouldThrowNotFoundException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findUserByUsername(loginRequest.emailOrUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findUserByMail(loginRequest.emailOrUsername()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> loginHandler.authenticate(loginRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findUserByUsername(loginRequest.emailOrUsername());
        verify(userRepository).findUserByMail(loginRequest.emailOrUsername());
        verifyNoInteractions(jwtService, loginMapper);
    }
}
