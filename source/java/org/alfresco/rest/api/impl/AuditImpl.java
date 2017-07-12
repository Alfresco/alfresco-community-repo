/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Audit;
import org.alfresco.rest.api.model.AuditApp;
import org.alfresco.rest.api.model.AuditEntry;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditApplication;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.util.Pair;

/**
 * Handles audit (applications & entries)
 *
 * @author janv
 */
public class AuditImpl implements Audit
{

    private final static String DISABLED = "Audit is disabled system-wide";
    private final static int MAX_ITEMS_AUDIT_ENTRIES = 100;

    // list of equals filter's auditEntry (via where clause)
    private final static Set<String> LIST_AUDIT_ENTRY_EQUALS_QUERY_PROPERTIES = new HashSet<>(
            Arrays.asList(new String[] { CREATED_BY_USER, VALUES_KEY, VALUES_VALUE }));

    // map of sort parameters for the moment one createdAt
    private final static Map<String, String> SORT_PARAMS_TO_NAMES;

    static
    {
        Map<String, String> aMap = new HashMap<>(1);
        aMap.put(CREATED_AT, CREATED_AT);
        SORT_PARAMS_TO_NAMES = Collections.unmodifiableMap(aMap);
    }

    private AuditService auditService;

    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    private void checkEnabled()
    {
        if (!auditService.isAuditEnabled())
        {
            throw new DisabledServiceException(DISABLED);
        }
    }

    @Override
    public AuditApp getAuditApp(String auditAppId, Parameters parameters)
    {
        checkEnabled();

        AuditService.AuditApplication auditApplication = findAuditAppById(auditAppId);

        if (auditApplication == null)
        {
            throw new EntityNotFoundException(auditAppId);
        }

        return new AuditApp(auditApplication.getKey().substring(1), auditApplication.getName(), auditApplication.isEnabled());
    }

    private AuditService.AuditApplication findAuditAppById(String auditAppId)
    {
        AuditService.AuditApplication auditApp = null;
        Map<String, AuditService.AuditApplication> auditApplicationsByName = auditService.getAuditApplications();
        if (auditApplicationsByName != null)
        {
            for (AuditService.AuditApplication auditApplication : auditApplicationsByName.values())
            {
                if (auditApplication.getKey().equals("/" + auditAppId))
                {
                    auditApp = auditApplication;
                }
            }
        }
        return auditApp;
    }

    @Override
    public CollectionWithPagingInfo<AuditApp> getAuditApps(Paging paging)
    {
        checkEnabled();

        Map<String, AuditService.AuditApplication> auditApplicationsByName = auditService.getAuditApplications();

        Set<String> audAppsName = new TreeSet<String>(auditApplicationsByName.keySet());
        Iterator<String> audAppsNameIt = audAppsName.iterator();

        int skipCount = paging.getSkipCount();
        int maxItems = paging.getMaxItems();
        int totalItems = audAppsName.size();
        int end = skipCount + maxItems;

        if (skipCount >= totalItems)
        {
            List<AuditApp> empty = Collections.emptyList();
            return CollectionWithPagingInfo.asPaged(paging, empty, false, totalItems);
        }

        List<AuditApp> auditApps = new ArrayList<AuditApp>(totalItems);
        int count = 0;
        for (int i = 0; i < end && audAppsNameIt.hasNext(); i++)
        {
            String auditAppName = audAppsNameIt.next();
            if (i < skipCount)
            {
                continue;
            }
            count++;
            AuditApplication auditApplication = auditApplicationsByName.get(auditAppName);

            auditApps.add(new AuditApp(auditApplication.getKey().substring(1), auditApplication.getName(), auditApplication.isEnabled()));
        }

        boolean hasMoreItems = (skipCount + count < totalItems);

        return CollectionWithPagingInfo.asPaged(paging, auditApps, hasMoreItems, totalItems);
    }

    @Override
    public CollectionWithPagingInfo<AuditEntry> listAuditEntries(String auditAppId, Parameters parameters)
    {
        checkEnabled();

        // adding orderBy property
        Pair<String, Boolean> sortProp = getAuditEntrySortProp(parameters);
        Boolean forward = true;
        if ((sortProp != null) && (sortProp.getFirst().equals(CREATED_AT)))
            forward = sortProp.getSecond();

        // Parse where clause properties.
        List<AuditEntry> entriesAudit = new ArrayList<AuditEntry>();
        Query q = parameters.getQuery();
        if (q != null)
        {
            // filtering via "where" clause
            AuditEntryQueryWalker propertyWalker = new AuditEntryQueryWalker();
            QueryHelper.walk(q, propertyWalker);
            entriesAudit = getQueryResultAuditEntries(auditAppId, propertyWalker, MAX_ITEMS_AUDIT_ENTRIES, forward);
        }

        // paging
        Paging paging = parameters.getPaging();

        int skipCount = paging.getSkipCount();
        int maxItems = paging.getMaxItems();
        int max = skipCount + maxItems; // to detect hasMoreItems
        int totalItems = entriesAudit.size();

        if (skipCount >= totalItems)
        {
            List<AuditEntry> empty = Collections.emptyList();
            return CollectionWithPagingInfo.asPaged(paging, empty, false, totalItems);
        }
        else
        {
            int end = Math.min(max, totalItems);
            boolean hasMoreItems = totalItems > end;

            entriesAudit = entriesAudit.subList(skipCount, end);
            return CollectionWithPagingInfo.asPaged(paging, entriesAudit, hasMoreItems, totalItems);
        }
    }

    /**
     * 
     * @param parameters
     * @return
     * @throws InvalidArgumentException
     */
    private Pair<String, Boolean> getAuditEntrySortProp(Parameters parameters)
    {
        Pair<String, Boolean> sortProp = null;
        List<SortColumn> sortCols = parameters.getSorting();

        if ((sortCols != null) && (sortCols.size() > 0))
        {
            if (sortCols.size() > 1)
            {
                throw new InvalidArgumentException("Multiple sort fields not allowed.");
            }

            SortColumn sortCol = sortCols.get(0);

            String sortPropName = SORT_PARAMS_TO_NAMES.get(sortCol.column);
            if (sortPropName == null)
            {
                throw new InvalidArgumentException("Invalid sort field: " + sortCol.column);
            }

            sortProp = new Pair<>(sortPropName, (sortCol.asc ? Boolean.TRUE : Boolean.FALSE));
        }
        return sortProp;
    }

    /**
     * 
     * @author anechifor
     *
     */
    private static class AuditEntryQueryWalker extends MapBasedQueryWalker
    {
        private Long fromTime;

        private Long toTime;

        public AuditEntryQueryWalker()
        {
            super(LIST_AUDIT_ENTRY_EQUALS_QUERY_PROPERTIES, null);
        }

        @Override
        public void and()
        {
            // allow AND, e.g. isRoot=true AND zones in ('BLAH')
        }

        @Override
        public void between(String propertyName, String firstValue, String secondValue, boolean negated)
        {
            if (propertyName.equals(CREATED_AT))
            {
                fromTime = new Long(firstValue);
                toTime = new Long(secondValue);
            }
        }

        public Long getFromTime()
        {
            return fromTime;
        }

        public Long getToTime()
        {
            return toTime;
        }

        public String getCreatedByUser()
        {
            return getProperty(CREATED_BY_USER, WhereClauseParser.EQUALS, String.class);
        }

        public String getValuesKey()
        {
            return getProperty(VALUES_KEY, WhereClauseParser.EQUALS, String.class);
        }

        public String getValuesValue()
        {
            return getProperty(VALUES_VALUE, WhereClauseParser.EQUALS, String.class);
        }
    }

    /**
     * 
     * @param auditAppId
     * @param propertyWalker
     * @param maxItem
     * @param forward
     * @return
     */
    public List<AuditEntry> getQueryResultAuditEntries(String auditAppId, AuditEntryQueryWalker propertyWalker, int maxItem, Boolean forward)
    {

        final List<AuditEntry> results = new ArrayList<AuditEntry>();

        AuditApplication auditApplication = findAuditAppById(auditAppId);
        if (auditApplication != null)
        {
            String auditApplicationName = auditApplication.getName();

            // Execute the query
            AuditQueryParameters params = new AuditQueryParameters();
            // used to orderBY by field createdAt
            params.setForward(forward);
            params.setApplicationName(auditApplicationName);
            params.setUser(propertyWalker.getCreatedByUser());
            params.setFromTime(propertyWalker.getFromTime());
            params.setToTime(propertyWalker.getToTime());
            if (propertyWalker.getValuesKey() != null && propertyWalker.getValuesValue() != null)
            {
                params.addSearchKey(propertyWalker.getValuesKey(), propertyWalker.getValuesValue());
            }

            // create the callback for auditQuery method
            final AuditQueryCallback callback = new AuditQueryCallback()
            {
                public boolean valuesRequired()
                {
                    return true;
                }

                public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
                {
                    throw new AlfrescoRuntimeException("Failed to retrieve audit data.", error);
                }

                public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values)
                {
                    AuditEntry auditEntry = new AuditEntry(entryId, auditAppId, new UserInfo(null, user, null), new Date(time), values);
                    results.add(auditEntry);
                    return true;
                }
            };

            auditService.auditQuery(callback, params, maxItem);
        }

        return results;
    }

    @Override
    public AuditApp update(String auditAppId, AuditApp auditApp, Parameters parameters)
    {
        checkEnabled();

        AuditService.AuditApplication auditApplication = findAuditAppById(auditAppId);

        // Check if id is valid
        if (auditApplication == null)
        {
            throw new EntityNotFoundException(auditAppId);
        }

        // Enable/Disable audit application
        if (auditApp.getIsEnabled() && !auditApplication.isEnabled())
        {
            auditService.enableAudit(auditApplication.getName(), auditApplication.getKey());
        }
        else if (!auditApp.getIsEnabled() && auditApplication.isEnabled())
        {
            auditService.disableAudit(auditApplication.getName(), auditApplication.getKey());
        }

        return new AuditApp(auditApplication.getKey().substring(1), auditApplication.getName(), auditApp.getIsEnabled());
    }

}
