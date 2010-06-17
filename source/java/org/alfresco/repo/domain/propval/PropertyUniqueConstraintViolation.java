/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
