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
package org.alfresco.repo.cmis.ws.utils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.cmis.CMISQueryException;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.repo.cmis.ws.CmisException;
import org.alfresco.repo.cmis.ws.CmisFaultType;
import org.alfresco.repo.cmis.ws.EnumServiceException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;

/**
 * @author Dmitry Velichkevich
 */
public abstract class ExceptionUtil
{
    private static final Map<String, EnumServiceException> CLASS_TO_ENUM_EXCEPTION_MAPPING;
    static
    {
        CLASS_TO_ENUM_EXCEPTION_MAPPING = new HashMap<String, EnumServiceException>();
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(AccessDeniedException.class.getName(), EnumServiceException.PERMISSION_DENIED);
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(java.lang.RuntimeException.class.getName(), EnumServiceException.RUNTIME);
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(UnsupportedOperationException.class.getName(), EnumServiceException.NOT_SUPPORTED);
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(InvalidNodeRefException.class.getName(), EnumServiceException.INVALID_ARGUMENT);
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(ContentIOException.class.getName(), EnumServiceException.NOT_SUPPORTED);
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(CMISQueryException.class.getName(), EnumServiceException.INVALID_ARGUMENT);
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(FileExistsException.class.getName(), EnumServiceException.NAME_CONSTRAINT_VIOLATION);
    }

    public static CmisException createCmisException(String message, EnumServiceException exceptionType)
    {
        return createCmisException(message, exceptionType, null, 0);
    }

    public static CmisException createCmisException(String message, Throwable cause)
    {
        EnumServiceException exceptionType = null;
        
        if (cause instanceof CMISServiceException)
        {
            return createCmisException((CMISServiceException)cause);
        }

        if (CLASS_TO_ENUM_EXCEPTION_MAPPING.containsKey(cause.getClass().getName()))
        {
            exceptionType = CLASS_TO_ENUM_EXCEPTION_MAPPING.get(cause.getClass().getName());
        }

        exceptionType = (exceptionType == null) ? (EnumServiceException.RUNTIME) : (exceptionType);

        return createCmisException(message, exceptionType, cause, 0);
    }

    public static CmisException createCmisException(String message, EnumServiceException exceptionType, Throwable cause)
    {
        return createCmisException(message, exceptionType, cause, 0);
    }

    public static CmisException createCmisException(CMISServiceException exception)
    {
        return createCmisException(exception.getMessage(), EnumServiceException.fromValue(exception.getFaultName()), exception, exception.getStatusCode());
    }
    
    public static CmisException createCmisException(String message, EnumServiceException exceptionType, Throwable cause, int errorCode)
    {
        CmisFaultType fault = new CmisFaultType();
        fault.setMessage(message);
        fault.setType(exceptionType);
        fault.setCode(BigInteger.valueOf(errorCode));

        return new CmisException(message, fault, cause);
    }
}
