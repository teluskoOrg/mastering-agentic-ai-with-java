package com.telusko.springbootpdfrag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DocumentNotFoundException extends RuntimeException
{

    public DocumentNotFoundException(Long documentId)
    {
        super("Doc not found "+ documentId);
    }
}
