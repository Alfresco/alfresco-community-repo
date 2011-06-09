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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.context.Context;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;

import org.alfresco.query.AbstractCannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.PagingResults;
import org.alfresco.util.Pair;
import org.aopalliance.intercept.MethodInvocation;
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
    
    private MethodSecurityInterceptor methodSecurityInterceptor;
    private Method method;
    
    protected AbstractCannedQueryPermissions(CannedQueryParameters parameters, String queryExecutionId, MethodSecurityInterceptor methodSecurityInterceptor, Method method)
    {
        super(parameters, queryExecutionId);
        
        this.methodSecurityInterceptor = methodSecurityInterceptor;
        this.method = method;
    }
    
    protected PagingResults<R> applyPostQueryPermissions(List<R> results, String authenticationToken, int requestedCount)
    {
        int requestTotalCountMax = getParameters().requestTotalResultCountMax();
        int maxChecks = (((requestTotalCountMax > 0) && (requestTotalCountMax > requestedCount)) ? requestTotalCountMax : requestedCount);
        
        return applyPermissions(results, authenticationToken, maxChecks);
    }
    
    @SuppressWarnings("unchecked")
    protected PagingResults<R> applyPermissions(List<R> results, String authenticationToken, int maxChecks)
    {
        long start = System.currentTimeMillis();
        
        // empty result
        PagingResults<R> ret = new PagingResults<R>()
        {
            @Override
            public String getQueryExecutionId()
            {
                return null;
            }
            
            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return new Pair<Integer, Integer>(0, 0);
            }
            
            @Override
            public List<R> getPage()
            {
                return new ArrayList<R>(0);
            }
            
            @Override
            public Boolean hasMoreItems()
            {
                return false;
            }
        };
        
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof SecureContext))
        {
            return ret; // empty result
        }
        
        Authentication authentication = (((SecureContext) context).getAuthentication());
        
        ConfigAttributeDefinition cad = methodSecurityInterceptor.getObjectDefinitionSource().getAttributes(new InternalMethodInvocation(method));
        MaxChecksCollection c = (MaxChecksCollection)methodSecurityInterceptor.getAfterInvocationManager().decide(authentication, null, cad, new MaxChecksCollection((Collection)results, maxChecks));
        
        final List<R> permissionCheckedResults = (List) c.getWrapped();
        
        final boolean cutoff = c.isCutoff();
        final int count = permissionCheckedResults.size();
        
        // final result
        ret = new PagingResults<R>()
        {
            @Override
            public String getQueryExecutionId()
            {
                return null;
            }
            
            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return new Pair<Integer, Integer>(count, ((! cutoff) ? count : null));
            }
            
            @Override
            public List<R> getPage()
            {
                return permissionCheckedResults;
            }
            
            @Override
            public Boolean hasMoreItems()
            {
                return (! cutoff);
            }
        };
        
        if (logger.isTraceEnabled())
        {
            logger.trace("applyPermissions: "+count+" items in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return ret;
    }
    
    class InternalMethodInvocation implements MethodInvocation 
    {
        Method method;
        
        public InternalMethodInvocation(Method method) 
        {
            this.method = method;
        }
        
        protected InternalMethodInvocation() 
        {
            throw new UnsupportedOperationException();
        }
        
        public Object[] getArguments() 
        {
            throw new UnsupportedOperationException();
        }
        
        public Method getMethod() 
        {
            return this.method;
        }
        
        public AccessibleObject getStaticPart() 
        {
            throw new UnsupportedOperationException();
        }
        
        public Object getThis() 
        {
            throw new UnsupportedOperationException();
        }
        
        public Object proceed() throws Throwable 
        {
            throw new UnsupportedOperationException();
        }
    }
}
