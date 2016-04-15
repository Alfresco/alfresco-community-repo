package org.alfresco.repo.domain.propval;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception generated when the {@link PropertyValueDAO}
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyUniqueConstraintViolation extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -7792036870731759068L;

    private final Serializable value1;
    private final Serializable value2;
    private final Serializable value3;
    
    public PropertyUniqueConstraintViolation(Serializable value1, Serializable value2, Serializable value3, Throwable cause)
    {
        super("Non-unique values for unique constraint: " + value1 + "-" + value2 + "-" + value3, cause);
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    public Serializable getValue1()
    {
        return value1;
    }

    public Serializable getValue2()
    {
        return value2;
    }

    public Serializable getValue3()
    {
        return value3;
    }
}
