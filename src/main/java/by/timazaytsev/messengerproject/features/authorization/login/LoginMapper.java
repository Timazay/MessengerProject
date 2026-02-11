package by.timazaytsev.messengerproject.features.authorization.login;

import by.timazaytsev.messengerproject.features.authorization.common.AuthResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoginMapper {

    AuthResponse toAuthResponse(String accessToken, String refreshToken, Long expiresIn);
}
