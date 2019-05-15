package org.alfresco.rest.exception;

public class JsonToModelConversionException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    public <T> JsonToModelConversionException(Class<T> classz, Exception e)
    {
        super(String.format("Could not parse Json Response to model [%s] error: %s", classz.getName(), e.getMessage()));
    }

}
