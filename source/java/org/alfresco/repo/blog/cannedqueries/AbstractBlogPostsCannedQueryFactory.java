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
package org.alfresco.repo.blog.cannedqueries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.blog.BlogService.BlogPostInfo;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Neil Mc Erlean, janv
 * @since 4.0
 */
public abstract class AbstractBlogPostsCannedQueryFactory extends AbstractCannedQueryFactory<BlogPostInfo>
{
    private Log logger = LogFactory.getLog(getClass());
    
    protected MethodSecurityBean<BlogPostInfo> methodSecurity;
    protected NodeDAO nodeDAO;
    protected QNameDAO qnameDAO;
    protected CannedQueryDAO cannedQueryDAO;
    protected TenantService tenantService;
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    public void setCannedQueryDAO(CannedQueryDAO cannedQueryDAO)
    {
        this.cannedQueryDAO = cannedQueryDAO;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setMethodSecurity(MethodSecurityBean<BlogPostInfo> methodSecurity)
    {
        this.methodSecurity = methodSecurity;
    }
    
    protected CannedQuerySortDetails createCQSortDetails(QName sortProp, SortOrder sortOrder)
    {
        CannedQuerySortDetails cqsd = null;
        List<Pair<? extends Object, SortOrder>> sortPairs = new ArrayList<Pair<? extends Object, SortOrder>>();
        sortPairs.add(new Pair<QName, SortOrder>(sortProp, sortOrder));
        cqsd = new CannedQuerySortDetails(sortPairs);
        return cqsd;
    }
    
    protected CannedQueryPageDetails createCQPageDetails(PagingRequest pagingReq)
    {
        int skipCount = pagingReq.getSkipCount();
        if (skipCount == -1)
        {
            skipCount = CannedQueryPageDetails.DEFAULT_SKIP_RESULTS;
        }
        
        int maxItems = pagingReq.getMaxItems();
        if (maxItems == -1)
        {
            maxItems  = CannedQueryPageDetails.DEFAULT_PAGE_SIZE;
        }
        
        // page details
        CannedQueryPageDetails cqpd = new CannedQueryPageDetails(skipCount, maxItems);
        return cqpd;
    }
    
    protected Long getQNameId(QName qname)
    {
        Pair<Long, QName> qnamePair = qnameDAO.getQName(qname);
        if (qnamePair == null)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("QName does not exist: " + qname); // possible ... eg. blg:blogPost if a blog has never been posted externally
            }
            return null;
        }
        return qnamePair.getFirst();
    }
    
    protected Long getNodeId(NodeRef nodeRef)
    {
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(tenantService.getName(nodeRef));
        if (nodePair == null)
        {
            throw new InvalidNodeRefException("Node ref does not exist: " + nodeRef, nodeRef);
        }
        return nodePair.getFirst();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        
        PropertyCheck.mandatory(this, "methodSecurity", methodSecurity);
        PropertyCheck.mandatory(this, "nodeDAO", nodeDAO);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);
        PropertyCheck.mandatory(this, "cannedQueryDAO", cannedQueryDAO);
        PropertyCheck.mandatory(this, "tenantService", tenantService);
    }
    
    /**
     * Utility class to sort {@link BlogPostInfo}s on the basis of a Comparable property.
     * Comparisons of two null properties are considered 'equal' by this comparator.
     * Comparisons involving one null and one non-null property will return the null property as
     * being 'before' the non-null property.
     * 
     * Note that it is the responsibility of the calling code to ensure that the specified
     * property values actually implement Comparable themselves.
     */
    protected static class PropertyBasedComparator implements Comparator<BlogEntity>
    {
        private QName comparableProperty;
        
        public PropertyBasedComparator(QName comparableProperty)
        {
            this.comparableProperty = comparableProperty;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public int compare(BlogEntity nr1, BlogEntity nr2)
        {
            Comparable prop1 = null;
            Comparable prop2 = null;
            if (comparableProperty.equals(ContentModel.PROP_PUBLISHED))
            {
                prop1 = nr1.getPublishedDate();
                prop2 = nr2.getPublishedDate();
            }
            else if (comparableProperty.equals(ContentModel.PROP_CREATED))
            {
                prop1 = nr1.getCreatedDate();
                prop1 = nr2.getCreatedDate();
            }
            else if (comparableProperty.equals(BlogIntegrationModel.PROP_POSTED))
            {
                prop1 = nr1.getPostedDate();
                prop1 = nr2.getPostedDate();
            }
            else
            {
                throw new IllegalArgumentException("Unsupported blog sort property: "+comparableProperty);
            }
            
            if (prop1 == null && prop2 == null)
            {
                return 0;
            }
            else if (prop1 == null && prop2 != null)
            {
                return -1;
            }
            else if (prop1 != null && prop2 == null)
            {
                return 1;
            }
            else
            {
                return prop1.compareTo(prop2);
            }
        }
    }
}
