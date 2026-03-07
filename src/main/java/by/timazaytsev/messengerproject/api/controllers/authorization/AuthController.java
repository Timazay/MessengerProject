package by.timazaytsev.messengerproject.api.controllers.authorization;

import by.timazaytsev.messengerproject.features.authorization.login.LoginRequest;
import by.timazaytsev.messengerproject.features.authorization.common.AuthResponse;
import by.timazaytsev.messengerproject.features.authorization.login.LoginHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginHandler loginHandler;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginHandler.authenticate(request));
    }
}
