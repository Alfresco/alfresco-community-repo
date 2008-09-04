/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws;

import org.alfresco.repo.cmis.ws.BasicFault;
import org.alfresco.repo.cmis.ws.PermissionDeniedException;
import org.alfresco.repo.cmis.ws.RuntimeException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.ThrowsAdvice;

/**
 * @author Dmitry Lazurkin
 *
 */
public class DMServicePortThrowsAdvice implements ThrowsAdvice
{
    private static final Log log = LogFactory.getLog("org.alfresco.repo.cmis.ws");

    public void afterThrowing(AccessDeniedException e) throws PermissionDeniedException
    {
        if (log.isInfoEnabled())
        {
            log.info(e);
        }

        // TODO: error code
        BasicFault basicFault = ExceptionUtils.createBasicFault(null, "Access denied");
        throw new PermissionDeniedException("Access denied", basicFault, e);
    }

    public void afterThrowing(java.lang.RuntimeException e) throws RuntimeException
    {
        if (log.isErrorEnabled())
        {
            log.error(e);
        }

        // TODO: error code
        BasicFault basicFault = ExceptionUtils.createBasicFault(null, "Runtime error");
        throw new RuntimeException("Runtime error", basicFault, e);
    }

}
