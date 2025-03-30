package io.github.kappa243.remitly2025.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(BankNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleBankNotFound(BankNotFoundException e, WebRequest wr) {
        return new ResponseEntity<>("Bank not found", HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BankAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<String> handleBankAlreadyExists(BankAlreadyExistsException e, WebRequest wr) {
        return new ResponseEntity<>("Bank already exists", HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(HeadBankNotFoundException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<String> handleHeadBankNotFound(HeadBankNotFoundException e, WebRequest wr) {
        return new ResponseEntity<>("Headquarter bank does not exists.", HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(CountryNotExistsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleCountryNotExists(CountryNotExistsException e, WebRequest wr) {
        return new ResponseEntity<>("Country does not exists", HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(ChildBranchesFoundException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<String> handleChildBranchesFound(ChildBranchesFoundException e, WebRequest wr) {
        return new ResponseEntity<>("Child branches found for given headquarter", HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>("Validation Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body("Validation Error: " + ex.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(", ")));
    }
    
}
