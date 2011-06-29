/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.ArrayList;
import java.util.List;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.context.Context;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;

import org.alfresco.query.AbstractCannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.security.authentication.AlfrescoSecureContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic support for canned query implementations - permission check
 * 
 * @author janv
 * @since 4.0
 */
public abstract class AbstractCannedQueryPermissions<R> extends AbstractCannedQuery<R>
{
    private Log logger = LogFactory.getLog(AbstractCannedQueryPermissions.class);

    private MethodSecurityBean<R> methodSecurity;
    
    protected AbstractCannedQueryPermissions(CannedQueryParameters parameters, MethodSecurityBean<R> methodSecurity)
    {
        super(parameters);
        this.methodSecurity = methodSecurity;
    }
    
    protected List<R> applyPostQueryPermissions(List<R> results, int requestedCount)
    {
        int requestTotalCountMax = getParameters().requestTotalResultCountMax();
        int maxChecks = (((requestTotalCountMax > 0) && (requestTotalCountMax > requestedCount)) ? requestTotalCountMax : requestedCount);
        
        return applyPermissions(results, maxChecks);
    }
    
    protected List<R> applyPermissions(List<R> results, int maxChecks)
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || (! (context instanceof AlfrescoSecureContext)))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unexpected context: "+(context == null ? "null" : context.getClass())+" - "+Thread.currentThread().getId());
            }
            
            return new WrappedList<R>(new ArrayList<R>(0), true, false); // empty result
        }
        Authentication authentication = (((SecureContext) context).getAuthentication());
        
        List<R> resultsOut = methodSecurity.applyPermissions(results, authentication, maxChecks);
        // Done
        return resultsOut;
    }
}
