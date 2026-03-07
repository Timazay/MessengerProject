package by.timazaytsev.messengerproject.features.authorization.common;

public record AuthResponse(String accessToken, String refreshToken, Long expiresIn) {
}
