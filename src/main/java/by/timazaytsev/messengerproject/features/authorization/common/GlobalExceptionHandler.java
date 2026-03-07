package by.timazaytsev.messengerproject.features.authorization.common;

import by.timazaytsev.messengerproject.api.common.exception.ErrorResponseDto;
import by.timazaytsev.messengerproject.api.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации данных",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponseDto.class)))
    @ExceptionHandler({
            MethodArgumentNotValidException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleValidationExceptions(MethodArgumentNotValidException ex) {
        return new ErrorResponseDto("400", ex.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            NotFoundException.class
    })
    public ErrorResponseDto handleNotFoundException(Exception e) {
        return new ErrorResponseDto("404 NOT FOUND", e.getMessage());
    }
}
