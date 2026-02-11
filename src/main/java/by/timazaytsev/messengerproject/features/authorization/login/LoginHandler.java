package by.timazaytsev.messengerproject.features.authorization.login;

import by.timazaytsev.messengerproject.features.authorization.common.AuthResponse;
import by.timazaytsev.messengerproject.api.common.service.JwtService;
import by.timazaytsev.messengerproject.infrastructure.entity.RefreshToken;
import by.timazaytsev.messengerproject.infrastructure.entity.User;
import by.timazaytsev.messengerproject.infrastructure.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginHandler {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginMapper loginMapper;

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.emailOrUsername(),
                        request.password()
                )
        );

        User user = userRepository.findUserByUsername(request.emailOrUsername())
                .orElseGet(() -> userRepository.findUserByMail(request.emailOrUsername())
                        .orElseThrow(() -> new EntityNotFoundException("User not found")));

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = jwtService.generateRefreshToken(user);

        return loginMapper.toAuthResponse(accessToken,
                refreshToken.getToken(),
                jwtService.extractAccessExpiration(accessToken).getTime());
    }
}
