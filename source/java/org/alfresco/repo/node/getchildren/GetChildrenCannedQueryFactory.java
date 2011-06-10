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
package org.alfresco.repo.node.getchildren;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.encryption.EncryptionEngine;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityInterceptor;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;

/**
 * GetChildren canned query factory - to get paged list of children of a parent node
 * 
 * @author janv
 * @since 4.0
 */
public class GetChildrenCannedQueryFactory extends AbstractCannedQueryFactory<NodeRef>
{
    private DictionaryService dictionaryService;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private LocaleDAO localeDAO;
    private ContentDataDAO contentDataDAO;
    private CannedQueryDAO cannedQueryDAO;
    private TenantService tenantService;
    
    private EncryptionEngine encryptionEngine;
    
    private MethodSecurityInterceptor methodSecurityInterceptor;
    private String methodName;
    private Object methodService;
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    public void setLocaleDAO(LocaleDAO localeDAO)
    {
        this.localeDAO = localeDAO;
    }
    
    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }
    
    public void setCannedQueryDAO(CannedQueryDAO cannedQueryDAO) 
    {
        this.cannedQueryDAO = cannedQueryDAO;
    }
    
    public void setEncryptionEngine(EncryptionEngine encryptionEngine)
    {
        this.encryptionEngine = encryptionEngine;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }    
    
    public void setMethodSecurityInterceptor(MethodSecurityInterceptor methodSecurityInterceptor)
    {
        this.methodSecurityInterceptor = methodSecurityInterceptor;
    }
    
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }
    
    public void setMethodService(Object methodService)
    {
        this.methodService = methodService;
    }
    
    @Override
    public CannedQuery<NodeRef> getCannedQuery(CannedQueryParameters parameters)
    {
        NodePropertyHelper nodePropertyHelper = new NodePropertyHelper(dictionaryService, qnameDAO, localeDAO, contentDataDAO, encryptionEngine);
        
        Method method = null;
        for (Method m : methodService.getClass().getMethods())
        {
            // note: currently matches first found
            if (m.getName().equals(methodName))
            {
                method = m;
                break;
            }
        }
        
        if (method == null)
        {
            throw new AlfrescoRuntimeException("Method not found: "+methodName);
        }
        
        // if not passed in (TODO or not in future cache) then generate a new query execution id
        String queryExecutionId = (parameters.getQueryExecutionId() == null ? super.getQueryExecutionId(parameters) : parameters.getQueryExecutionId());
        
        return (CannedQuery<NodeRef>) new GetChildrenCannedQuery(nodeDAO, qnameDAO, cannedQueryDAO, nodePropertyHelper, tenantService, methodSecurityInterceptor, method, parameters, queryExecutionId);
    }
    
    /**
     * Retrieve an optionally filtered/sorted instance of a {@link CannedQuery} based on parameters including request for a total count (up to a given max)
     * 
     * Note: if both filtering and sorting is required then the combined total of unique QName properties should be the 0 to 3.
     *
     * @param parentRef          parent node ref
     * @param childTypeQNames    type qnames of children nodes (pre-filter)
     * @param filterProps        filter properties
     * @param sortProps          sort property pairs (QName and Boolean - true if ascending)
     * @param pagingRequest      skipCount, maxItems - optionally queryExecutionId and requestTotalCountMax
     * 
     * @return                   an implementation that will execute the query
     */
    public CannedQuery<NodeRef> getCannedQuery(NodeRef parentRef, Set<QName> childTypeQNames, List<FilterProp> filterProps, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("parentRef", parentRef);
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        int requestTotalCountMax = pagingRequest.getRequestTotalCountMax();
        
        // specific query params - context (parent) and inclusive filters (child types, property values)
        GetChildrenCannedQueryParams paramBean = new GetChildrenCannedQueryParams(tenantService.getName(parentRef), childTypeQNames, filterProps);
        
        int skipCount = pagingRequest.getSkipCount();
        if (skipCount == -1)
        {
            skipCount = CannedQueryPageDetails.DEFAULT_SKIP_RESULTS;
        }
        
        int maxItems = pagingRequest.getMaxItems();
        if (maxItems == -1)
        {
            maxItems  = CannedQueryPageDetails.DEFAULT_PAGE_SIZE;
        }
        
        // page details
        CannedQueryPageDetails cqpd = new CannedQueryPageDetails(skipCount, maxItems, CannedQueryPageDetails.DEFAULT_PAGE_NUMBER, CannedQueryPageDetails.DEFAULT_PAGE_COUNT);
        
        // sort details
        CannedQuerySortDetails cqsd = null;
        if (sortProps != null)
        {
            List<Pair<? extends Object, SortOrder>> sortPairs = new ArrayList<Pair<? extends Object, SortOrder>>(sortProps.size());
            for (Pair<QName, Boolean> sortProp : sortProps)
            {
                sortPairs.add(new Pair<QName, SortOrder>(sortProp.getFirst(), (sortProp.getSecond() ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
            }
            
            cqsd = new CannedQuerySortDetails(sortPairs);
        }
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, AuthenticationUtil.getRunAsUser(), requestTotalCountMax, pagingRequest.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
    
    /**
     * Retrieve an unsorted instance of a {@link CannedQuery} based on parameters including request for a total count (up to a given max)
     *
     * @param parentRef          parent node ref
     * @param childTypeQNames    type qnames of children nodes
     * @param pagingRequest      skipCount, maxItems - optionally queryExecutionId and requestTotalCountMax
     * 
     * @return                   an implementation that will execute the query
     */
    public CannedQuery<NodeRef> getCannedQuery(NodeRef parentRef, Set<QName> childTypeQNames, PagingRequest pagingRequest)
    {
        return getCannedQuery(parentRef, childTypeQNames, null, null, pagingRequest);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "nodeDAO", nodeDAO);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);
        PropertyCheck.mandatory(this, "localeDAO", localeDAO);
        PropertyCheck.mandatory(this, "contentDataDAO", contentDataDAO);
        PropertyCheck.mandatory(this, "cannedQueryDAO", cannedQueryDAO);
        
        //PropertyCheck.mandatory(this, "encryptionEngine", encryptionEngine);

        PropertyCheck.mandatory(this, "methodSecurityInterceptor", methodSecurityInterceptor);
        PropertyCheck.mandatory(this, "methodService", methodService);
        PropertyCheck.mandatory(this, "methodName", methodName);
    }
}
