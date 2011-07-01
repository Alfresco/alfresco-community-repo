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

import java.util.List;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.context.Context;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;

import org.alfresco.query.AbstractCannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.security.authentication.AlfrescoSecureContext;
import org.alfresco.repo.security.permissions.PermissionCheckedCollection;
import org.alfresco.util.Pair;
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
    
    /**
     * {@inheritDoc}
     * <p/>
     * By default, the is a permission checking class.  Override the method if you wish to
     * switch the behaviour at runtime.
     * 
     * @return      <tt>true</tt> always
     */
    @Override
    protected boolean isApplyPostQueryPermissions()
    {
        return true;
    }

    @Override
    protected List<R> applyPostQueryPermissions(List<R> results, int requestedCount)
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || (! (context instanceof AlfrescoSecureContext)))
        {
            // This indicates that we have come via the internal service methods
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignoring post-query permissions.  The secure context is empty: " + this);
            }
            return results;
        }
        Authentication authentication = (((SecureContext) context).getAuthentication());
        
        List<R> resultsOut = (List<R>) methodSecurity.applyPermissions(results, authentication, requestedCount);
        // Done
        return resultsOut;
    }

    /**
     * Overrides the default implementation to check for the permission data
     * that will allow a good guess as to the maximum number of results in
     * the event of a permission-based cut-off.
     */
    @Override
    protected Pair<Integer, Integer> getTotalResultCount(List<R> results)
    {
        // Start with the simplest
        int size = results.size();
        int possibleSize = size;
        
        if (results instanceof PermissionCheckedCollection)
        {
            @SuppressWarnings("unchecked")
            PermissionCheckedCollection<R> pcc = (PermissionCheckedCollection<R>) results;
            if (pcc.isCutOff())
            {
                // We didn't get all the results processed, so make a guess
                double successRatio = (double)size/(double)pcc.sizeOriginal();
                int possiblyMissed = (int) (pcc.sizeUnchecked() * successRatio);
                possibleSize = size + possiblyMissed;
            }
        }
        // Done
        return new Pair<Integer, Integer>(size, possibleSize);
    }
}
