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
package org.alfresco.opencmis;

import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;

/**
 * Interceptor to catch various exceptions and translate them into CMIS-related exceptions
 * <p/>
 * TODO: Externalize messages
 * TODO: Use ExceptionStackUtil to dig out exceptions of interest regardless of depth
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AlfrescoCmisExceptionInterceptor implements MethodInterceptor
{
    public Object invoke(MethodInvocation mi) throws Throwable
    {
        try
        {
            return mi.proceed();
        }
        catch (AuthenticationException e)
        {
            throw new CmisPermissionDeniedException(e.getMessage(), e);
        }
        catch (CheckOutCheckInServiceException e)
        {
            throw new CmisVersioningException("Check out failed: " + e.getMessage(), e);
        }
        catch (FileExistsException fee)
        {
            throw new CmisContentAlreadyExistsException("An object with this name already exists!", fee);
        }
        catch (IntegrityException ie)
        {
            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
        }
        catch (AccessDeniedException ade)
        {
            throw new CmisPermissionDeniedException("Permission denied!", ade);
        }
        catch (Exception e)
        {
            if (e instanceof CmisBaseException)
            {
                throw (CmisBaseException) e;
            }
            else
            {
                throw new CmisRuntimeException(e.getMessage(), e);
            }
        }
    }
}
