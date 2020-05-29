package org.glassfish.grizzly.http;

import org.glassfish.grizzly.http.util.HttpStatus;

public class HttpErrorException extends RuntimeException
{
    HttpStatus statusCode;

    public HttpErrorException(HttpStatus statusCode)
    {
        this.statusCode = statusCode;
    }

    public HttpErrorException(String s, HttpStatus statusCode)
    {
        super(s);
        this.statusCode = statusCode;
    }

    public HttpErrorException(Throwable cause, HttpStatus statusCode)
    {
        super(cause);
        this.statusCode = statusCode;
    }

    public HttpErrorException(String message, Throwable cause, HttpStatus statusCode)
    {
        super(message, cause);
        this.statusCode = statusCode;
    }

}
