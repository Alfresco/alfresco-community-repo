/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor to catch various exceptions and translate them into CMIS-related exceptions
 * <p/>
 * TODO: Externalize messages
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AlfrescoCmisExceptionInterceptor implements MethodInterceptor
{
    /**
     * Exceptions that are specifically handled.
     */
    @SuppressWarnings({ "rawtypes" })
    public static final Class[] EXCEPTIONS_OF_INTEREST;
    static
    {
        Class<?>[] coreClasses = new Class[] {
                AuthenticationException.class,
                CheckOutCheckInServiceException.class,
                FileExistsException.class,
                IntegrityException.class,     // Similar to StaleObjectState
                AccessDeniedException.class,
                NodeLockedException.class
                };
     
        List<Class<?>> retryExceptions = new ArrayList<Class<?>>();
        // Add core classes to the list.
        retryExceptions.addAll(Arrays.asList(coreClasses));
        
        EXCEPTIONS_OF_INTEREST = retryExceptions.toArray(new Class[] {});
    }

    private static Log logger = LogFactory.getLog(AlfrescoCmisExceptionInterceptor.class);
    
    public Object invoke(MethodInvocation mi) throws Throwable
    {
        try
        {
            return mi.proceed();
        }
        catch (Exception e)
        {
            // We dig into the exception to see if there is anything of interest to CMIS
            Throwable cmisAffecting = ExceptionStackUtil.getCause(e, EXCEPTIONS_OF_INTEREST);
            
            if (cmisAffecting == null)
            {
                // The exception is not something that CMIS needs to handle in any special way
                if (e instanceof CmisBaseException)
                {
                    throw (CmisBaseException) e;
                }
                else
                {
                    throw new CmisRuntimeException(e.getMessage(), e);
                }
            }
            // All other exceptions are carried through with full stacks but treated as the exception of interest
            else if (cmisAffecting instanceof AuthenticationException)
            {
                throw new CmisPermissionDeniedException(cmisAffecting.getMessage(), e);
            }
            else if (cmisAffecting instanceof CheckOutCheckInServiceException)
            {
                throw new CmisVersioningException("Check out failed: " + cmisAffecting.getMessage(), e);
            }
            else if (cmisAffecting instanceof FileExistsException)
            {
                throw new CmisContentAlreadyExistsException("An object with this name already exists: " + cmisAffecting.getMessage(), e);
            }
            else if (cmisAffecting instanceof IntegrityException)
            {
                throw new CmisConstraintException("Constraint violation: " + cmisAffecting.getMessage(), e);
            }
            else if (cmisAffecting instanceof AccessDeniedException)
            {
                throw new CmisPermissionDeniedException("Permission denied: " + cmisAffecting.getMessage(), e);
            }
            else if (cmisAffecting instanceof NodeLockedException)
            {
                throw new CmisUpdateConflictException("Update conflict: " + cmisAffecting.getMessage(), e);
            }
            else
            {
                // We should not get here, so log an error but rethrow to have CMIS handle the original cause
                logger.error("Exception type not handled correctly: " + e.getClass().getName());
                throw new CmisRuntimeException(e.getMessage(), e);
            }
        }
    }
}
