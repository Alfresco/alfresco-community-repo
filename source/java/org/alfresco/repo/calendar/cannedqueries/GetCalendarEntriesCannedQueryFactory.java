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
package org.alfresco.repo.calendar.cannedqueries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.calendar.CalendarModel;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;

/**
 * A {@link CannedQueryFactory} for various queries relating to {@link CalendarEntry calendar entries}.
 * 
 * @author Nick Burch
 * @since 4.0
 * 
 * @see CalendarService#listCalendarEntries(String, PagingRequest)
 * @see CalendarService#listCalendarEntries(String[], PagingRequest)
 * @see CalendarService#listCalendarEntries(String[], Date, Date, PagingRequest)
 */
public class GetCalendarEntriesCannedQueryFactory extends AbstractQNameAwareCannedQueryFactory<CalendarEntry>
{
    protected NodeService nodeService;
    protected TaggingService taggingService;

    public void setNodeService(NodeService nodeService)
    {
       this.nodeService = nodeService;
    }

    public void setTaggingService(TaggingService taggingService)
    {
       this.taggingService = taggingService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "taggingService", taggingService);
    }
    
    @Override
    public CannedQuery<CalendarEntry> getCannedQuery(CannedQueryParameters parameters)
    {
        final GetCalendarEntriesCannedQuery cq = new GetCalendarEntriesCannedQuery(
              cannedQueryDAO, nodeService, taggingService, methodSecurity, parameters
        );
        
        return (CannedQuery<CalendarEntry>) cq;
    }
    
    public CannedQuery<CalendarEntry> getCannedQuery(NodeRef[] containerNodes, Date fromDate, Date toDate, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("containerNodes", containerNodes);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        int requestTotalCountMax = pagingReq.getRequestTotalCountMax();
        
        Long[] containerIds = new Long[containerNodes.length];
        for(int i=0; i<containerIds.length; i++)
        {
           containerIds[i] = getNodeId(containerNodes[i]);
        }
        
        //FIXME Need tenant service like for GetChildren?
        GetCalendarEntriesCannedQueryParams paramBean = new GetCalendarEntriesCannedQueryParams(
              containerIds, 
              getQNameId(ContentModel.PROP_NAME),
              getQNameId(CalendarModel.TYPE_EVENT),
              getQNameId(CalendarModel.PROP_FROM_DATE),
              getQNameId(CalendarModel.PROP_TO_DATE),
              getQNameId(CalendarModel.PROP_RECURRENCE_RULE),
              getQNameId(CalendarModel.PROP_RECURRENCE_LAST_MEETING),
              fromDate, 
              toDate
        );
        
        CannedQueryPageDetails cqpd = createCQPageDetails(pagingReq);
        CannedQuerySortDetails cqsd = createCQSortDetails();
        
        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, cqsd, requestTotalCountMax, pagingReq.getQueryExecutionId());
        
        // return canned query instance
        return getCannedQuery(params);
    }
    
    protected CannedQuerySortDetails createCQSortDetails()
    {
        List<Pair<? extends Object,SortOrder>> sort = new ArrayList<Pair<? extends Object, SortOrder>>();
        sort.add(new Pair<QName, SortOrder>(CalendarModel.PROP_FROM_DATE, SortOrder.ASCENDING)); 
        sort.add(new Pair<QName, SortOrder>(CalendarModel.PROP_TO_DATE, SortOrder.ASCENDING));
        
        return new CannedQuerySortDetails(sort);
    }
}
