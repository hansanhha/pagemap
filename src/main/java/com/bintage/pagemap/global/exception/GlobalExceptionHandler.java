package com.bintage.pagemap.global.exception;

import com.bintage.pagemap.auth.domain.exception.AccountException;
import com.bintage.pagemap.storage.domain.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<Object> handleAccountException(AccountException e, WebRequest request) {
        return handleAccountExceptionInternal(e, request);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Object> handleStorageException(StorageException e, WebRequest request) {
        return handleStorageExceptionInternal(e, request);
    }

    private ResponseEntity<Object> handleAccountExceptionInternal(AccountException e, WebRequest request) {
        var httpStatus = HttpStatusCode.valueOf(e.getAccountExceptionCode().getStatus());
        var title = e.getProblemDetailTitle();
        var detail = e.getProblemDetailDetail();
        var instance = e.getProblemDetailInstance();

        var problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, detail);
        problemDetail.setInstance(instance);
        problemDetail.setTitle(title);

        return handleExceptionInternal(e, problemDetail, e.getHeaders(), httpStatus, request);
    }

    private ResponseEntity<Object> handleStorageExceptionInternal(StorageException e, WebRequest request) {
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
