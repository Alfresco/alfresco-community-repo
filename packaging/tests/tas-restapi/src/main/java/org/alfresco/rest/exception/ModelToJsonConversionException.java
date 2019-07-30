package org.alfresco.rest.exception;

public class ModelToJsonConversionException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public <T> ModelToJsonConversionException(Class<T> classz, Exception e)
    {
        super(String.format("Could not convert model for [%s] to JSON", classz.getName()), e);
    }
}
