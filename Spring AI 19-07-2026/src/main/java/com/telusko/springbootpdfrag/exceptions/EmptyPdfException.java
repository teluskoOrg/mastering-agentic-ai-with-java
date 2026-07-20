package com.telusko.springbootpdfrag.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class EmptyPdfException extends RuntimeException
{

    public EmptyPdfException(String filename)
    {
        super("no extractable text in " + filename);
    }
}
