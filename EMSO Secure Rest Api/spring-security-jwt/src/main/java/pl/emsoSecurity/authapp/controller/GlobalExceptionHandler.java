package pl.emsoSecurity.authapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import pl.emsoSecurity.authapp.exception.BadRequestException;
import pl.emsoSecurity.authapp.exception.ConflictException;
import pl.emsoSecurity.authapp.dto.ErrorResponse;
import pl.emsoSecurity.authapp.exception.NotFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /***
     * This handler catches and handles a bad http request exception
     * @param exception
     * @return
     */
    @ExceptionHandler({BadRequestException.class, NoSuchFieldException.class, NumberFormatException.class, JsonProcessingException.class, IllegalArgumentException.class, PropertyReferenceException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse runtime(
            RuntimeException exception)
    {
        log.info(exception.getMessage());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    /***
     * This handler catches and handles a http request not found exception
     * @param notFoundException
     * @return
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundHandler(
            NotFoundException notFoundException)
    {
        log.info(notFoundException.getMessage());
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), notFoundException.getMessage());
    }

    /***
     * This handler catches and handles a conflicting http request
     * @param conflictException
     * @return
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse conflictHandler(
            ConflictException conflictException)
    {
        log.info(conflictException.getMessage());
        return new ErrorResponse(HttpStatus.CONFLICT.value(), conflictException.getMessage());
    }

    /***
     * This handler catches and handles a client error in the http request.
     * @param httpClientErrorException
     * @return
     */
    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse httpClientErrorHandler(
            HttpClientErrorException httpClientErrorException)
    {
        log.info(httpClientErrorException.getMessage());
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), httpClientErrorException.getMessage());
    }
}
