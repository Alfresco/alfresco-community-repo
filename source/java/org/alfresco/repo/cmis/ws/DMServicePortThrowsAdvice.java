/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
        if (LOGGER.isErrorEnabled())
        {
            LOGGER.error(e.toString(), e);
        }

        throw ExceptionUtil.createCmisException(("Runtime error. Message: " + e.toString()), e);
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
