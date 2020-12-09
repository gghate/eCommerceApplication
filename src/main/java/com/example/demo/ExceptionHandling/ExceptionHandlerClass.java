package com.example.demo.ExceptionHandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.Timestamp;

@ControllerAdvice
public class ExceptionHandlerClass {

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<ExceptionMessage> restException(CustomException e,WebRequest request)
    {
        String path=((ServletWebRequest)request).getRequest().getRequestURI().toString();
        return new ResponseEntity<ExceptionMessage>(new ExceptionMessage(e.getMessage(),"Failure",""+new Timestamp(System.currentTimeMillis()),path), HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<String> noHandlerFound(NoHandlerFoundException e)
    {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
    }


    class ExceptionMessage{
        String message;
        String status;
        String timestamp;
        String path;

        public ExceptionMessage(String message, String status, String timestamp, String path) {
            this.message = message;
            this.status = status;
            this.timestamp = timestamp;
            this.path = path;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
