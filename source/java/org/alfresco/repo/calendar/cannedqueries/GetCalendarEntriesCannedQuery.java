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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.calendar.CalendarModel;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory.NestedComparator;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory.PropertyBasedComparator;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarRecurrenceHelper;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides support for {@link CannedQuery canned queries} used by the
 * {@link CalendarService}.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetCalendarEntriesCannedQuery extends AbstractCannedQueryPermissions<CalendarEntry>
{
    private Log logger = LogFactory.getLog(getClass());
    
    private static final String QUERY_NAMESPACE = "alfresco.query.calendar";
    private static final String QUERY_SELECT_GET_BLOGS = "select_GetCalendarEntriesCannedQuery";
    
    private final CannedQueryDAO cannedQueryDAO;
    private final TaggingService taggingService;
    private final NodeService nodeService;
    private GetCalendarEntriesCannedQueryTestHook testHook;
    
    public GetCalendarEntriesCannedQuery(
            CannedQueryDAO cannedQueryDAO,
            NodeService nodeService,
            TaggingService taggingService,
            MethodSecurityBean<CalendarEntry> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);
        this.cannedQueryDAO = cannedQueryDAO;
        this.taggingService = taggingService;
        this.nodeService = nodeService;
    }
    
    @Override
    protected List<CalendarEntry> queryAndFilter(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        Object paramBeanObj = parameters.getParameterBean();
        if (paramBeanObj == null)
            throw new NullPointerException("Null GetCalendarEntries query params");
        
        GetCalendarEntriesCannedQueryParams paramBean = (GetCalendarEntriesCannedQueryParams) paramBeanObj;
        Date entriesFromDate = paramBean.getEntriesFromDate();
        Date entriesToDate = paramBean.getEntriesToDate();
        
        // note: refer to SQL for specific DB filtering (eg.parent nodes etc)
        List<CalendarEntity> results = cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_BLOGS, paramBean, 0, Integer.MAX_VALUE);
        
        List<CalendarEntity> filtered = new ArrayList<CalendarEntity>(results.size());
        for (CalendarEntity result : results)
        {
            boolean nextNodeIsAcceptable = true;
            
            Date fromDate = DefaultTypeConverter.INSTANCE.convert(Date.class, result.getFromDate()); 
            Date toDate = DefaultTypeConverter.INSTANCE.convert(Date.class, result.getToDate());
            if(toDate == null)
            {
               toDate = fromDate;
            }
            
            String recurringRule = result.getRecurrenceRule();
            Date recurringLastDate = DefaultTypeConverter.INSTANCE.convert(Date.class, result.getRecurrenceLastMeeting());
            
            // Only return entries in the right period
            if(entriesFromDate != null)
            {
               // Needs to end on or after the Filter From date
               if(toDate == null || toDate.before(entriesFromDate))
               {
                  nextNodeIsAcceptable = false;
               }
            }
            if(entriesToDate != null)
            {
               // Needs have started by the Filter To date
               if(fromDate == null || fromDate.after(entriesToDate))
               {
                  nextNodeIsAcceptable = false;
               }
            }
            
            // Handle recurring events specially
            if(recurringRule != null && !nextNodeIsAcceptable)
            {
               if(entriesToDate != null || recurringLastDate != null)
               {
                  Date searchFrom = entriesFromDate;
                  if(searchFrom == null)
                  {
                     searchFrom = fromDate;
                  }
                  Date searchTo = entriesToDate;
                  if(searchTo == null)
                  {
                     searchTo = recurringLastDate;
                  }
                     
                  List<Date> dates = CalendarRecurrenceHelper.getRecurrencesOnOrAfter(
                        recurringRule, fromDate, toDate, recurringLastDate,
                        searchFrom, searchTo, false
                  );
                  if(dates != null && dates.size() > 0)
                  {
                     // Do any of these fit?
                     for(Date date : dates)
                     {
                        if(entriesFromDate != null && entriesToDate != null)
                        {
                           // From and To date given, needs to sit between them
                           if(entriesFromDate.getTime() <= date.getTime() &&
                              date.getTime() <= entriesToDate.getTime())
                           {
                              nextNodeIsAcceptable = true;
                              break;
                           }
                        }
                        else if(entriesFromDate != null)
                        {
                           // From date but no end date, needs to be after the from
                           if(entriesFromDate.getTime() <= date.getTime())
                           {
                              nextNodeIsAcceptable = true;
                              break;
                           }
                        }
                        else if(entriesToDate != null)
                        {
                           // End date but no start date, needs to be before the from
                           if(date.getTime() <= entriesToDate.getTime())
                           {
                              nextNodeIsAcceptable = true;
                              break;
                           }
                        }
                     }
                  }
               }
            }
            
            // Did it make the cut
            if (nextNodeIsAcceptable)
            {
                filtered.add(result);
            }
        }
        
        List<Pair<? extends Object, SortOrder>> sortPairs = parameters.getSortDetails().getSortPairs();
        
        // For now, the CalendarService only sorts by a single property.
        if (sortPairs != null && !sortPairs.isEmpty())
        {
            List<Pair<Comparator<CalendarEntity>, SortOrder>> comparators =
               new ArrayList<Pair<Comparator<CalendarEntity>,SortOrder>>();
            for(Pair<? extends Object, SortOrder> sortPair : sortPairs)
            {
               final QName sortProperty = (QName)sortPair.getFirst();
               final CalendarEntityComparator comparator = new CalendarEntityComparator(sortProperty);
               comparators.add(new Pair<Comparator<CalendarEntity>, SortOrder>(comparator, sortPair.getSecond()));
            }
            NestedComparator<CalendarEntity> comparator = new NestedComparator<CalendarEntity>(comparators);
            
            // Sort
            Collections.sort(filtered, comparator); 
        }
        
        List<CalendarEntry> calendarEntries = new ArrayList<CalendarEntry>(filtered.size());
        for (CalendarEntity result : filtered)
        {
            calendarEntries.add(new CalendarEntryImpl(result));
        }
        
        if (start != null)
        {
            logger.debug("Base query: "+calendarEntries.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        if(testHook != null)
        {
           testHook.notifyComplete(results, filtered);
        }
        
        return calendarEntries;
    }
    
    @Override
    protected boolean isApplyPostQuerySorting()
    {
        // No post-query sorting. It's done within the queryAndFilter() method above.
        return false;
    }
    
    public void setTestHook(GetCalendarEntriesCannedQueryTestHook hook)
    {
       this.testHook = hook;
    }
    
    private class CalendarEntryImpl extends org.alfresco.repo.calendar.CalendarEntryImpl
    {
       private static final long serialVersionUID = 5717119409619436964L;
       private CalendarEntryImpl(CalendarEntity entity)
       {
          super(
                entity.getNodeRef(), 
                // TODO Fetch this from the database layer when querying
                nodeService.getPrimaryParent(entity.getNodeRef()).getParentRef(), 
                entity.getName()
          );
          super.populate(nodeService.getProperties(entity.getNodeRef()));
          super.setTags(taggingService.getTags(entity.getNodeRef()));
       }
    }
    
    /**
     * Utility class to sort {@link CalendarEntry}s on the basis of a Comparable property.
     * Comparisons of two null properties are considered 'equal' by this comparator.
     * Comparisons involving one null and one non-null property will return the null property as
     * being 'before' the non-null property.
     * 
     * Note that it is the responsibility of the calling code to ensure that the specified
     * property values actually implement Comparable themselves.
     */
    protected static class CalendarEntityComparator extends PropertyBasedComparator<CalendarEntity>
    {
        protected CalendarEntityComparator(QName property)
        {
           super(property);
        }
       
        @SuppressWarnings("unchecked")
        @Override
        protected Comparable getProperty(CalendarEntity entity) {
            if (comparableProperty.equals(CalendarModel.PROP_FROM_DATE))
            {
               return entity.getFromDate();
            }
            else if (comparableProperty.equals(CalendarModel.PROP_TO_DATE))
            {
                return entity.getToDate();
            }
            else if (comparableProperty.equals(ContentModel.PROP_CREATED))
            {
                return entity.getCreatedDate();
            }
            else
            {
                throw new IllegalArgumentException("Unsupported calendar sort property: "+comparableProperty);
            }
        }
    }
}
