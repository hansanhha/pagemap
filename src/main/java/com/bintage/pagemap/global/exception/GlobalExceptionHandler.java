package com.bintage.pagemap.global.exception;

import com.bintage.pagemap.storage.domain.exception.DomainModelException;
import com.bintage.pagemap.storage.domain.exception.DomainModelNotFoundException;
import com.bintage.pagemap.storage.domain.exception.StorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @PostConstruct
    public void init() {
        Assert.notNull(messageSource, "messageSource must not be null");
        super.setMessageSource(messageSource);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Object> handleStorageException(StorageException e, WebRequest request) {
        if (e instanceof DomainModelNotFoundException) {
            return handleStorageDomainModelNotFoundException(e, request);
        }
        else if (e instanceof DomainModelException) {
            return handleStorageDomainModelException(e, request);
        }
        else {
            return handleExceptionInternal(e, null, e.getHeaders(), HttpStatusCode.valueOf(e.getStorageExceptionCode().getStatus()), request);
        }
    }

    private ResponseEntity<Object> handleStorageDomainModelException(StorageException e, WebRequest request) {
        var httpStatus = HttpStatusCode.valueOf(e.getStorageExceptionCode().getStatus());
        var title = e.getProblemDetailTitle();
        var detail = e.getProblemDetailDetail();
        var instance = e.getProblemDetailInstance();

        var problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, detail);
        problemDetail.setInstance(instance);
        problemDetail.setTitle(title);

        return handleExceptionInternal(e, problemDetail, e.getHeaders(), httpStatus, request);
    }

    private ResponseEntity<Object> handleStorageDomainModelNotFoundException(StorageException e, WebRequest request) {
        var httpStatus = HttpStatusCode.valueOf(e.getStorageExceptionCode().getStatus());
        var title = e.getProblemDetailTitle();
        var detail = e.getProblemDetailDetail();
        var instance = e.getProblemDetailInstance();

        var problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, detail);
        problemDetail.setInstance(instance);
        problemDetail.setTitle(title);

        return handleExceptionInternal(e, problemDetail, e.getHeaders(), httpStatus, request);
    }

}
