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
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
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
    
    public GetCalendarEntriesCannedQuery(
            CannedQueryDAO cannedQueryDAO,
            MethodSecurityBean<CalendarEntry> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);
        this.cannedQueryDAO = cannedQueryDAO;
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
               // Needs to start on or after the Filter End date
               if(fromDate == null || fromDate.after(entriesToDate))
               {
                  nextNodeIsAcceptable = false;
               }
            }
            
            if (nextNodeIsAcceptable)
            {
                filtered.add(result);
            }
        }
        
        List<Pair<? extends Object, SortOrder>> sortPairs = parameters.getSortDetails().getSortPairs();
        
        // For now, the BlogService only sorts by a single property.
        if (sortPairs != null && !sortPairs.isEmpty())
        {
            List<Pair<Comparator<CalendarEntity>, SortOrder>> comparators =
               new ArrayList<Pair<Comparator<CalendarEntity>,SortOrder>>();
            for(Pair<? extends Object, SortOrder> sortPair : sortPairs)
            {
               QName sortProperty = (QName) sortPair.getFirst();
               final PropertyBasedComparator comparator = new PropertyBasedComparator(sortProperty);
               comparators.add(new Pair<Comparator<CalendarEntity>, SortOrder>(comparator, sortPair.getSecond()));
            }
            NestedComparator comparator = new NestedComparator(comparators);
            
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
        
        return calendarEntries;
    }
    
    @Override
    protected boolean isApplyPostQuerySorting()
    {
        // No post-query sorting. It's done within the queryAndFilter() method above.
        return false;
    }
    
    private class CalendarEntryImpl extends org.alfresco.repo.calendar.CalendarEntryImpl
    {
       private CalendarEntryImpl(CalendarEntity entity)
       {
          super(entity.getNodeRef(), entity.getName());
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
    protected static class PropertyBasedComparator implements Comparator<CalendarEntity>
    {
        private QName comparableProperty;
        
        public PropertyBasedComparator(QName comparableProperty)
        {
            this.comparableProperty = comparableProperty;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public int compare(CalendarEntity nr1, CalendarEntity nr2)
        {
            Comparable prop1 = null;
            Comparable prop2 = null;
            if (comparableProperty.equals(CalendarModel.PROP_FROM_DATE))
            {
                prop1 = nr1.getFromDate();
                prop2 = nr2.getFromDate();
            }
            else if (comparableProperty.equals(CalendarModel.PROP_TO_DATE))
            {
                prop1 = nr1.getToDate();
                prop2 = nr2.getToDate();
            }
            else if (comparableProperty.equals(ContentModel.PROP_CREATED))
            {
                prop1 = nr1.getCreatedDate();
                prop2 = nr2.getCreatedDate();
            }
            else
            {
                throw new IllegalArgumentException("Unsupported calendar sort property: "+comparableProperty);
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
    
    protected static class NestedComparator implements Comparator<CalendarEntity>
    {
        private List<Pair<Comparator<CalendarEntity>, SortOrder>> comparators;
        
        private NestedComparator(List<Pair<Comparator<CalendarEntity>, SortOrder>> comparators)
        {
           this.comparators = comparators;
        }

        @Override
        public int compare(CalendarEntity entry1, CalendarEntity entry2) {
           for(Pair<Comparator<CalendarEntity>, SortOrder> pc : comparators)
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
