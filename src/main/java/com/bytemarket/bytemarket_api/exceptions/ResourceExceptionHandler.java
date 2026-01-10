package com.bytemarket.bytemarket_api.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ResourceExceptionHandler {

    //422
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<StandardError> handleOutOfStock(
            OutOfStockException e,
            HttpServletRequest request
    ){
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;

        StandardError error = new StandardError(
                Instant.now(),
                status.value(),
                "Out of stock",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }

    //  404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardError> handleEntityNotFound(
            EntityNotFoundException e,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Entity not found",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    //409
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<StandardError> handleOptimisticLock(
            OptimisticLockingFailureException e,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Conflict",
                "Conflito de atualização. Tente novamente.",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }
}
