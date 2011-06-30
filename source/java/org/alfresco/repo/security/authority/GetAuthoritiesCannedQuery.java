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
package org.alfresco.repo.security.authority;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GetAuthorities CQ - to get paged list of authorities
 * 
 * @author janv
 * @since 4.0
 */
public class GetAuthoritiesCannedQuery extends AbstractCannedQueryPermissions<AuthorityInfo>
{
    private Log logger = LogFactory.getLog(getClass());
    
    private static final String QUERY_NAMESPACE = "alfresco.node";
    private static final String QUERY_SELECT_GET_AUTHORITIES = "select_GetAuthoritiesCannedQuery";
    
    private CannedQueryDAO cannedQueryDAO;
    private TenantService tenantService;
    
    public GetAuthoritiesCannedQuery(
            CannedQueryDAO cannedQueryDAO,
            TenantService tenantService,
            MethodSecurityBean<AuthorityInfo> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);
        
        this.cannedQueryDAO = cannedQueryDAO;
        this.tenantService = tenantService;
    }
    
    @Override
    protected List<AuthorityInfo> queryAndFilter(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        // Get parameters
        GetAuthoritiesCannedQueryParams params = (GetAuthoritiesCannedQueryParams)parameters.getParameterBean();
        
        // Get filter details
        final AuthorityType type = params.getType();
        final String nameFilter = params.getDisplayNameFilter();
        
        // filtered - note: sorting and permissions will be applied post query
        final List<AuthorityInfo> auths = new ArrayList<AuthorityInfo>(100);
        
        QueryCallback callback = new QueryCallback()
        {
            public boolean handle(AuthorityInfo auth)
            {
                // filter
                if (includeFilter(auth, type, nameFilter))
                {
                    auths.add(auth);
                }
                
                // more results
                return true;
            }
        };
        
        ResultHandler resultHandler = new ResultHandler(callback);
        cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_AUTHORITIES, params, 0, Integer.MAX_VALUE, resultHandler);
        resultHandler.done();
        
        if (start != null)
        {
            logger.debug("Base query: "+auths.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return auths;
    }
    
    private class AuthComparator implements Comparator<AuthorityInfo>
    {
        private SortOrder sortOrder;
        private Collator collator;
        
        public AuthComparator(SortOrder sortOrder)
        {
            this.sortOrder = sortOrder;
            this.collator = Collator.getInstance(); // note: currently default locale
        }
        
        public int compare(AuthorityInfo auth1In, AuthorityInfo auth2In)
        {
            AuthorityInfo auth1 = auth1In;
            AuthorityInfo auth2 = auth2In; 
            
            if (sortOrder.equals(SortOrder.DESCENDING))
            {
                auth1 = auth2In;
                auth2 = auth1In;
            }
            
            String s1 = auth1.getAuthorityDisplayName();
            String s2 = auth2.getAuthorityDisplayName();
            
            if (s1 == null)
            {
                s1 = auth1.getAuthorityName();
            }
            
            if (s2 == null)
            {
                s2 = auth1.getAuthorityName();
            }
            
            if (s1 == null)
            {
                return (s2 == null ? 0 : -1);
            }
            else if (s2 == null)
            {
                return 1;
            }
            
            return collator.compare((String)s1, (String)s2); // TODO use collation keys (re: performance)
        }
    }
    
    @Override
    protected boolean isApplyPostQuerySorting()
    {
        // assume sort is required - final decision made in "applyPostQuerySorting"
        return true;
    }
    
    @SuppressWarnings({ "unchecked"})
    protected List<AuthorityInfo> applyPostQuerySorting(List<AuthorityInfo> results, CannedQuerySortDetails sortDetails)
    {
        final List<Pair<Object, SortOrder>> sortPairs = (List)sortDetails.getSortPairs();
        if (sortPairs.size() > 0)
        {
            // single sort option - authority display name (else authority name if former is null)
            Collections.sort(results, new AuthComparator(sortPairs.get(0).getSecond()));
        }
        
        return results;
    }
    
    // startsWith / ignoreCase
    private boolean includeFilter(AuthorityInfo auth, AuthorityType type, String nameFilterLower)
    {
        String authName = auth.getAuthorityName();
        
        AuthorityType authType = AuthorityType.getAuthorityType(authName);
        if ((authName == null) || ((type != null) && (! type.equals(authType))))
        {
            // exclude by type
            return false;
        }
        
        if (nameFilterLower == null)
        {
            return true;
        }
        
        String authDisplayNameLower = (auth.getAuthorityDisplayName() != null ? auth.getAuthorityDisplayName().toLowerCase() : null);
        if ((authDisplayNameLower != null) && (authDisplayNameLower.startsWith(nameFilterLower)))
        {
            return true;
        }
        
        if (authType.isPrefixed())
        {
            authName = authName.substring(authType.getPrefixString().length());
        }
        
        return (authName.toLowerCase().startsWith(nameFilterLower));
    }
    
    @Override
    protected boolean isApplyPostQueryPermissions()
    {
        return true;
    }
    
    private interface QueryCallback
    {
        boolean handle(AuthorityInfo auth);
    }
    
    private class ResultHandler implements CannedQueryDAO.ResultHandler<AuthorityInfoEntity>
    {
        private final QueryCallback resultsCallback;
        private boolean more = true;
        
        private ResultHandler(QueryCallback resultsCallback)
        {
            this.resultsCallback = resultsCallback;
        }
        
        public boolean handleResult(AuthorityInfoEntity result)
        {
            // Do nothing if no further results are required
            if (!more)
            {
                return false;
            }
            
            // Call back
            boolean more = resultsCallback.handle(new AuthorityInfo(tenantService.getBaseName(result.getNode().getNodeRef()), 
                                                                    result.getAuthorityDisplayName(), 
                                                                    result.getAuthorityName()));
            if (!more)
            {
                this.more = false;
            }
            
            return more;
        }
        
        public void done()
        {
        }
    }
}