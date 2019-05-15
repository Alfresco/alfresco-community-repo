package org.alfresco.rest.exception;

import java.util.Collection;

public class EmptyRestModelCollectionException extends Exception
{
    private static final long serialVersionUID = 1L;

    public <E> EmptyRestModelCollectionException(Collection<E> models)
    {        
        super(String.format("Empty Rest Model Collection of type: %s -> %s", models.getClass(), models.toString()));
    }
}
