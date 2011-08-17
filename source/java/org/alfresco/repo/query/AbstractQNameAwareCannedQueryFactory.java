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
package org.alfresco.repo.query;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
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
 * An intermediate {@link AbstractCannedQueryFactory} layer, for various 
 * implementations that need to know about QName IDs and similar
 * 
 * @author Nick Burch
 * @since 4.0
 */
public abstract class AbstractQNameAwareCannedQueryFactory<R> extends AbstractCannedQueryFactory<R>
{
    private Log logger = LogFactory.getLog(getClass());

    protected MethodSecurityBean<R> methodSecurity;
    protected NodeDAO nodeDAO;
    protected QNameDAO qnameDAO;
    protected TenantService tenantService;
    protected CannedQueryDAO cannedQueryDAO;

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

    public void setMethodSecurity(MethodSecurityBean<R> methodSecurity)
    {
       this.methodSecurity = methodSecurity;
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
     * Creates a Canned Query sort details, for the given list of properties
     *  and if they should be Ascending or Descending
     */
    protected CannedQuerySortDetails createCQSortDetails(List<Pair<QName,Boolean>> sort)
    {
        List<Pair<? extends Object,SortOrder>> details = new ArrayList<Pair<? extends Object, SortOrder>>();
        for(Pair<QName,Boolean> sortProp : sort)
        {
           details.add(new Pair<QName, SortOrder>(
                 sortProp.getFirst(),
                 (sortProp.getSecond() ? SortOrder.ASCENDING : SortOrder.DESCENDING)
           ));
        }
        return new CannedQuerySortDetails(details);
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
    
    public CannedQuerySortDetails createDateAscendingCQSortDetails()
    {
        List<Pair<? extends Object,SortOrder>> sort = new ArrayList<Pair<? extends Object, SortOrder>>();
        sort.add(new Pair<QName, SortOrder>(ContentModel.PROP_CREATED, SortOrder.ASCENDING)); 
        sort.add(new Pair<QName, SortOrder>(ContentModel.PROP_MODIFIED, SortOrder.ASCENDING));
        
        return new CannedQuerySortDetails(sort);
    }
    
    public CannedQuerySortDetails createDateDescendingCQSortDetails()
    {
        List<Pair<? extends Object,SortOrder>> sort = new ArrayList<Pair<? extends Object, SortOrder>>();
        sort.add(new Pair<QName, SortOrder>(ContentModel.PROP_CREATED, SortOrder.DESCENDING)); 
        sort.add(new Pair<QName, SortOrder>(ContentModel.PROP_MODIFIED, SortOrder.DESCENDING));
        
        return new CannedQuerySortDetails(sort);
    }
    
    /**
     * Utility class to sort Entities on the basis of a Comparable property.
     * Comparisons of two null properties are considered 'equal' by this comparator.
     * Comparisons involving one null and one non-null property will return the null property as
     * being 'before' the non-null property.
     * 
     * Note that it is the responsibility of the calling code to ensure that the specified
     * property values actually implement Comparable themselves.
     */
    public static abstract class PropertyBasedComparator<R> implements Comparator<R>
    {
        protected QName comparableProperty;
        
        public PropertyBasedComparator(QName comparableProperty)
        {
            this.comparableProperty = comparableProperty;
        }
        
        @SuppressWarnings("unchecked")
        protected abstract Comparable getProperty(R entity);
        
        @SuppressWarnings("unchecked")
        @Override
        public int compare(R r1, R r2)
        {
            Comparable prop1 = getProperty(r1);
            Comparable prop2 = getProperty(r2);
            
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
    
    /**
     * An instance of a {@link PropertyBasedComparator} for a {@link NodeBackedEntity}
     */
    public static class NodeBackedEntityComparator extends PropertyBasedComparator<NodeBackedEntity>
    {
       public NodeBackedEntityComparator(QName comparableProperty)
       {
          super(comparableProperty);
       }
       
       @SuppressWarnings("unchecked")
       @Override
       protected Comparable getProperty(NodeBackedEntity entity) {
          if (comparableProperty.equals(ContentModel.PROP_CREATED))
          {
             return entity.getCreatedDate();
          }
          else if (comparableProperty.equals(ContentModel.PROP_MODIFIED))
          {
             return entity.getModifiedDate();
          }
          else if (comparableProperty.equals(ContentModel.PROP_CREATOR))
          {
             return entity.getCreator();
          }
          else if (comparableProperty.equals(ContentModel.PROP_MODIFIER))
          {
             return entity.getModifier();
          }
          else if (comparableProperty.equals(ContentModel.PROP_NAME))
          {
             return entity.getName();
          }
          else
          {
             throw new IllegalArgumentException("Unsupported calendar sort property: "+comparableProperty);
          }
       }
    }
    
    public static class NestedComparator<R> implements Comparator<R>
    {
        private List<Pair<Comparator<R>, SortOrder>> comparators;
        
        public NestedComparator(List<Pair<Comparator<R>, SortOrder>> comparators)
        {
           this.comparators = comparators;
        }

        @Override
        public int compare(R entry1, R entry2) {
           for(Pair<Comparator<R>, SortOrder> pc : comparators)
           {
              int result = pc.getFirst().compare(entry1, entry2);
              if(result != 0)
              {
                 // Sorts differ, return
                 if(pc.getSecond() == SortOrder.ASCENDING)
                 {
                    return result;
                 }
                 else
                 {
                    return 0 - result;
                 }
              }
              else
              {
                 // Sorts are the same, try the next along
              }
           }
           // No difference on any
           return 0;
        }
    }
}
