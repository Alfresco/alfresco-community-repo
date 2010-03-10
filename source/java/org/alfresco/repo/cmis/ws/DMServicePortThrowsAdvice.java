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
package org.alfresco.repo.cmis.ws;

import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.ThrowsAdvice;

/**
 * Map Alfresco Exceptions to CMIS Exceptions
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
public class DMServicePortThrowsAdvice implements ThrowsAdvice
{
    private static final Log LOGGER = LogFactory.getLog("org.alfresco.repo.cmis.ws");

    public void afterThrowing(AccessDeniedException e) throws CmisException
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.error(e.toString(), e);
        }
        throw ExceptionUtil.createCmisException(("Access denied. Message: " + e.toString()), e);
    }

    public void afterThrowing(java.lang.RuntimeException e) throws CmisException
    {
        Throwable result = e;
        if (null != e.getCause())
        {
            result = e.getCause();
        }
        if (LOGGER.isErrorEnabled())
        {
            LOGGER.error(result.toString(), result);
        }
        throw (result instanceof CmisException) ? ((CmisException) result) : (ExceptionUtil.createCmisException(("Runtime error. Message: " + result.toString()), result));
    }

    public void afterThrowing(java.lang.Exception e) throws CmisException
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.error(e.toString(), e);
        }
        if (!(e instanceof CmisException))
        {
            throw ExceptionUtil.createCmisException(("Some error occured during last service invokation. Message: " + e.toString()), e);
        }
        else
        {
            throw (CmisException) e;
        }
    }
}
