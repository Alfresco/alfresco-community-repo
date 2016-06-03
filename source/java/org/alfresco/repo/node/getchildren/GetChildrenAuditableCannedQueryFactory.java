/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.node.getchildren;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory;
import org.alfresco.repo.query.NodeBackedEntity;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * A {@link CannedQueryFactory} for various queries relating to getting
 * {@link NodeBackedEntity} entires filtering by auditable properties.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetChildrenAuditableCannedQueryFactory extends AbstractQNameAwareCannedQueryFactory<NodeBackedEntity>
{
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
    }
    
    @Override
    public CannedQuery<NodeBackedEntity> getCannedQuery(CannedQueryParameters parameters)
    {
        final GetChildrenAuditableCannedQuery cq = new GetChildrenAuditableCannedQuery(
              cannedQueryDAO, methodSecurity, parameters
        );
        
        return (CannedQuery<NodeBackedEntity>) cq;
    }
    
    public CannedQuery<NodeBackedEntity> getCannedQuery(NodeRef parentNodeRef, QName contentType, 
          String createdBy, Date createdFrom, Date createdTo,
          String modifiedBy, Date modifiedFrom, Date modifiedTo, 
          CannedQuerySortDetails sortDetails, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("parentNodeRef", parentNodeRef);
        ParameterCheck.mandatory("contentType", contentType);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        //FIXME Need tenant service like for GetChildren?
        GetChildrenAuditableCannedQueryParams paramBean = new GetChildrenAuditableCannedQueryParams(
              getNodeId(parentNodeRef), 
              getQNameId(ContentModel.PROP_NAME),
              getQNameId(contentType),
              createdBy, createdFrom, createdTo,
              modifiedBy, modifiedFrom, modifiedTo
        );
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(
              paramBean, cqpd, sortDetails, requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
}
