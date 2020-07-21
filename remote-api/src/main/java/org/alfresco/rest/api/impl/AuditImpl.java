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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Audit;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.model.AuditApp;
import org.alfresco.rest.api.model.AuditEntry;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;

/**
 * Handles audit (applications & entries)
 *
 * @author janv, anechifor, eknizat
 */
public class AuditImpl implements Audit
{

    private final static String DISABLED = "Audit is disabled system-wide";
    private final static String DEFAULT_USER = "-me-";

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

    private PersonService personService;

    private NodeService nodeService;

    private NamespaceService namespaceService;

    private Nodes nodes;

    private People people;

    public void setPeople(People people)
    {
        this.people = people;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
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

        AuditService.AuditApplication auditApplication = findAuditAppByIdOr404(auditAppId);

        return new AuditApp(auditApplication.getKey().substring(1), auditApplication.getName(), auditApplication.isEnabled());
    }

    private AuditService.AuditApplication findAuditAppByIdOr404(String auditAppId)
    {
        AuditService.AuditApplication auditApplication = findAuditAppById(auditAppId);

        if (auditApplication == null)
        {
            throw new EntityNotFoundException(auditAppId);
        }

        return auditApplication;
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

        AuditService.AuditApplication auditApplication = findAuditAppByIdOr404(auditAppId);

        // adding orderBy property
        Pair<String, Boolean> sortProp = getAuditEntrySortProp(parameters);
        Boolean forward = true;
        if ((sortProp != null) && (sortProp.getFirst().equals(CREATED_AT)))
        {
            forward = sortProp.getSecond();
        }

        // Parse where clause properties.
        List<AuditEntry> entriesAudit = new ArrayList<>();
        Query q = parameters.getQuery();
        // paging
        Paging paging = parameters.getPaging();
        int skipCount = paging.getSkipCount();
        int maxItems = paging.getMaxItems();
        int limit = skipCount + maxItems + 1; // to detect hasMoreItems

        if (q != null)
        {
            // filtering via "where" clause
            AuditEntryQueryWalker propertyWalker = new AuditEntryQueryWalker();
            QueryHelper.walk(q, propertyWalker);
            entriesAudit = getQueryResultAuditEntries(auditApplication, propertyWalker, parameters.getInclude(), limit, forward);
        }

        // clear null elements
        entriesAudit.removeAll(Collections.singleton(null));
        int totalItems = entriesAudit.size();

        if (skipCount >= totalItems)
        {
            List<AuditEntry> empty = Collections.emptyList();
            return CollectionWithPagingInfo.asPaged(paging, empty, false, totalItems);
        }
        else
        {
            int end = Math.min(limit - 1, totalItems);
            boolean hasMoreItems = totalItems > end;

            entriesAudit = entriesAudit.subList(skipCount, end);
            return CollectionWithPagingInfo.asPaged(paging, entriesAudit, hasMoreItems, totalItems);
        }
    }

    /**
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
     * @author anechifor
     */
    private class AuditEntryQueryWalker extends MapBasedQueryWalker
    {
        private Long fromTime;
        private Long toTime;

        private Long fromId;
        private Long toId;

        public AuditEntryQueryWalker()
        {
            super(LIST_AUDIT_ENTRY_EQUALS_QUERY_PROPERTIES, null);
        }

        @Override
        public void and()
        {
            // allow AND, e.g. createdByUser='jbloggs' AND createdAt BETWEEN ('...','...)
        }

        @Override
        public void between(String propertyName, String firstValue, String secondValue, boolean negated)
        {
            if (propertyName.equals(CREATED_AT))
            {
                fromTime = getTime(firstValue);
                toTime = getTime(secondValue) + 1;
            }

            if (propertyName.equals(ID))
            {
                fromId = Long.valueOf(firstValue);
                toId = Long.valueOf(secondValue) + 1;
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
            String propertyValue = getProperty(CREATED_BY_USER, WhereClauseParser.EQUALS, String.class);

            // Check if '-me-' alias is used and replace it with userId
            if ((propertyValue != null) && propertyValue.equalsIgnoreCase(DEFAULT_USER))
            {
                propertyValue = AuditImpl.this.people.validatePerson(propertyValue);
            }
            return propertyValue;
        }

        public String getValuesKey()
        {
            return getProperty(VALUES_KEY, WhereClauseParser.EQUALS, String.class);
        }

        public String getValuesValue()
        {
            return getProperty(VALUES_VALUE, WhereClauseParser.EQUALS, String.class);
        }

        public Long getFromId()
        {
            return fromId;
        }

        public Long getToId()
        {
            return toId;
        }
    }

    /**
     * @param auditAppId
     * @param propertyWalker
     * @param includeParams
     * @param maxItem
     * @param forward
     * @return
     */
    public List<AuditEntry> getQueryResultAuditEntries(AuditService.AuditApplication auditApplication, AuditEntryQueryWalker propertyWalker,
            List<String> includeParam, int maxItem, Boolean forward)
    {
        final List<AuditEntry> results = new ArrayList<>();

        final String auditAppId = auditApplication.getKey().substring(1);
        String auditApplicationName = auditApplication.getName();

        // Execute the query
        AuditQueryParameters params = new AuditQueryParameters();

        // used to orderBY by field createdAt
        params.setForward(forward);

        params.setApplicationName(auditApplicationName);
        params.setUser(propertyWalker.getCreatedByUser());

        Long fromId = propertyWalker.getFromId();
        Long toId = propertyWalker.getToId();

        validateWhereBetween(auditAppId, fromId, toId);

        Long fromTime = propertyWalker.getFromTime();
        Long toTime = propertyWalker.getToTime();

        validateWhereBetween(auditAppId, fromTime, toTime);

        params.setFromTime(fromTime);
        params.setToTime(toTime);

        params.setFromId(fromId);
        params.setToId(toId);

        if (propertyWalker.getValuesKey() != null && propertyWalker.getValuesValue() != null)
        {
            params.addSearchKey(propertyWalker.getValuesKey(), propertyWalker.getValuesValue());
        }

        final Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        // create the callback for auditQuery method
        final AuditQueryCallback callback = new AuditQueryCallback()
        {
            public boolean valuesRequired()
            {
                return ((includeParam != null) && (includeParam.contains(PARAM_INCLUDE_VALUES)));
            }

            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve audit data.", error);
            }

            public boolean handleAuditEntry(Long entryId, String applicationName, String userName, long time, Map<String, Serializable> values)
            {
                UserInfo userInfo = Node.lookupUserInfo(userName, mapUserInfo, personService);
                AuditEntry auditEntry = new AuditEntry(entryId, auditAppId, userInfo, new Date(time), values);
                results.add(auditEntry);
                return true;
            }
        };

        auditService.auditQuery(callback, params, maxItem);
        return results;
    }

    @Override
    public AuditApp update(String auditAppId, AuditApp auditApp, Parameters parameters)
    {
        checkEnabled();

        AuditService.AuditApplication auditApplication = findAuditAppByIdOr404(auditAppId);

        // Enable/Disable audit application
        if (auditApp.getIsEnabled() && !auditApplication.isEnabled())
        {
            auditService.enableAudit(auditApplication.getName(), null);
        }
        else if (!auditApp.getIsEnabled() && auditApplication.isEnabled())
        {
            auditService.disableAudit(auditApplication.getName(), null);
        }

        return new AuditApp(auditApplication.getKey().substring(1), auditApplication.getName(), auditApp.getIsEnabled());
    }

    @Override
    public AuditEntry getAuditEntry(String auditAppId, long auditEntryId, Parameters parameters)
    {
        checkEnabled();

        AuditService.AuditApplication auditApplication = findAuditAppByIdOr404(auditAppId);

        // Execute the query
        AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(auditApplication.getName());
        params.setFromId(auditEntryId);
        params.setToId(auditEntryId + 1);
        
        List<String> includeParam = new ArrayList<>();
        if (parameters != null)
        {
            includeParam.addAll(parameters.getInclude());
        }

        // Add values for single get
        includeParam.add(PARAM_INCLUDE_VALUES);

        final List<AuditEntry> results = new ArrayList<>();

        // create the callback for auditQuery method
        final AuditQueryCallback callback = new AuditQueryCallback()
        {
            public boolean valuesRequired()
            {
                return ((includeParam != null) && (includeParam.contains(PARAM_INCLUDE_VALUES)));
            }

            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve audit data.", error);
            }

            public boolean handleAuditEntry(Long entryId, String applicationName, String userName, long time, Map<String, Serializable> values)
            {
                UserInfo userInfo = Node.lookupUserInfo(userName, new HashMap<>(0), personService);
                AuditEntry auditEntry = new AuditEntry(entryId, auditAppId, userInfo, new Date(time), values);
                results.add(auditEntry);
                return true;
            }
        };

        auditService.auditQuery(callback, params, 1);

        if (results.size() != 1)
        {
            throw new EntityNotFoundException("" + auditEntryId);
        }
        return results.get(0);
    }

    @Override
    public void deleteAuditEntry(String auditAppId, long auditEntryId, Parameters parameters)
    {
        checkEnabled();

        AuditService.AuditApplication auditApplication = findAuditAppByIdOr404(auditAppId);

        int deleted = auditService.clearAuditByIdRange(auditApplication.getName(), auditEntryId, auditEntryId + 1);
        if (deleted != 1)
        {
            throw new EntityNotFoundException("" + auditEntryId);
        }
    }

    @Override
    public void deleteAuditEntries(String auditAppId, Parameters parameters)
    {
        checkEnabled();

        AuditService.AuditApplication auditApplication = findAuditAppByIdOr404(auditAppId);

        Query q = parameters.getQuery();
        if ((q == null) || (q.getTree() == null))
        {
            throw new InvalidArgumentException("where clause is required to delete audit entries (" + auditAppId + ")");
        }

        // delete via "where" clause
        DeleteAuditEntriesQueryWalker walker = new DeleteAuditEntriesQueryWalker();
        QueryHelper.walk(q, walker);

        Long fromId = walker.getFromId();
        Long toId = walker.getToId();

        validateWhereBetween(auditAppId, fromId, toId);

        Long fromTime = walker.getFromTime();
        Long toTime = walker.getToTime();

        validateWhereBetween(auditAppId, fromTime, toTime);

        if ((fromId != null) && (fromTime != null))
        {
            throw new InvalidArgumentException("where clause is invalid - cannot specify both createdAt & id (" + auditAppId + ")");
        }

        if (fromId != null)
        {
            auditService.clearAuditByIdRange(auditApplication.getName(), fromId, toId); // ignore
                                                                                        // response
        }
        else if (fromTime != null)
        {
            auditService.clearAudit(auditApplication.getName(), fromTime, toTime); // ignore
                                                                                   // response
        }

        // return success (even if nothing is deleted)
    }

    private static class DeleteAuditEntriesQueryWalker extends MapBasedQueryWalker
    {
        private Long fromTime;
        private Long toTime;

        private Long fromId;
        private Long toId;

        public DeleteAuditEntriesQueryWalker()
        {
            super(null, null);
        }

        @Override
        public void between(String propertyName, String firstValue, String secondValue, boolean negated)
        {
            if (propertyName.equals(CREATED_AT))
            {
                fromTime = getTime(firstValue);
                toTime = getTime(secondValue) + 1;
            }

            if (propertyName.equals(ID))
            {
                fromId = Long.valueOf(firstValue);
                toId = Long.valueOf(secondValue) + 1;
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

        public Long getFromId()
        {
            return fromId;
        }

        public Long getToId()
        {
            return toId;
        }
    }

    private static long getTime(String iso8601String)
    {
        return ISO8601DateFormat.parse(iso8601String.replace(" ", "+")).getTime();
    }

    private void validateWhereBetween(String auditAppId, Long from, Long to)
    {
        if ((from != null) || (to != null))
        {
            if ((from == null) || (to == null))
            {
                // belts-and-braces
                throw new InvalidArgumentException("where BETWEEN is invalid - must contain range (" + auditAppId + ")");
            }

            if (from >= to)
            {
                throw new InvalidArgumentException("where BETWEEN is invalid - range start greater than end (" + auditAppId + ")");
            }
        }
    }

    @Override
    public CollectionWithPagingInfo<AuditEntry> listAuditEntriesByNodeId(String nodeId, Parameters parameters)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                checkEnabled();
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

        // note: node read permission is checked later - see nodeService.getPath
        NodeRef nodeRef = nodes.validateNode(nodeId);
        List<AuditEntry> entriesAudit = new ArrayList<>();

        // adding orderBy property
        Pair<String, Boolean> sortProp = getAuditEntrySortProp(parameters);
        Boolean forward = true;
        if ((sortProp != null) && (sortProp.getFirst().equals(CREATED_AT)))
        {
            forward = sortProp.getSecond();
        }

        // paging
        Paging paging = parameters.getPaging();
        int skipCount = paging.getSkipCount();
        int maxItems = paging.getMaxItems();
        int limit = skipCount + maxItems + 1; // to detect hasMoreItems

        Query q = parameters.getQuery();

        if (q != null)
        {
            // filtering via "where" clause
            AuditEntriesByNodeIdQueryWalker propertyWalker = new AuditEntriesByNodeIdQueryWalker();
            QueryHelper.walk(q, propertyWalker);
            entriesAudit = getQueryResultAuditEntriesByNodeRef(nodeRef, propertyWalker, parameters.getInclude(), forward, limit);
        }

        // clear null elements
        entriesAudit.removeAll(Collections.singleton(null));
        int totalItems = entriesAudit.size();

        if (skipCount >= totalItems)
        {
            List<AuditEntry> empty = Collections.emptyList();
            return CollectionWithPagingInfo.asPaged(paging, empty, false, totalItems);
        }
        else
        {
            int end = Math.min(limit - 1, totalItems);
            boolean hasMoreItems = totalItems > end;

            entriesAudit = entriesAudit.subList(skipCount, end);
            return CollectionWithPagingInfo.asPaged(paging, entriesAudit, hasMoreItems, totalItems);
        }
    }

    private List<AuditEntry> getQueryResultAuditEntriesByNodeRef(NodeRef nodeRef, AuditEntriesByNodeIdQueryWalker propertyWalker,
            List<String> includeParam, boolean forward, int limit)
    {
        final List<AuditEntry> results = new ArrayList<>();

        String auditAppId = "alfresco-access";
        String auditApplicationName = AuthenticationUtil.runAs(new RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                return findAuditAppByIdOr404(auditAppId).getName();
            }
        }, AuthenticationUtil.getSystemUserName());

        // create the callback for auditQuery method
        final AuditQueryCallback callback = new AuditQueryCallback()
        {
            public boolean valuesRequired()
            {
                return ((includeParam != null) && (includeParam.contains(PARAM_INCLUDE_VALUES)));
            }

            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve audit data.", error);
            }

            public boolean handleAuditEntry(Long entryId, String applicationName, String userName, long time, Map<String, Serializable> values)
            {
                UserInfo userInfo = Node.lookupUserInfo(userName, new HashMap<>(0), personService);
                AuditEntry auditEntry = new AuditEntry(entryId, auditAppId, userInfo, new Date(time), values);
                results.add(auditEntry);
                return true;
            }
        };

        // resolve the path of the node - note: this will also check read permission for current user
        final String nodePath = ISO9075.decode(nodeService.getPath(nodeRef).toPrefixString(namespaceService));
        Long fromTime = propertyWalker.getFromTime();
        Long toTime = propertyWalker.getToTime();
        validateWhereBetween(nodeRef.getId(), fromTime, toTime);

        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // QueryParameters
                AuditQueryParameters pathParams = new AuditQueryParameters();
                // used to orderBY by field createdAt
                pathParams.setForward(forward);
                pathParams.setUser(propertyWalker.getCreatedByUser());
                pathParams.setFromTime(fromTime);
                pathParams.setToTime(toTime);
                pathParams.setApplicationName(auditApplicationName);
                pathParams.addSearchKey("/"+auditAppId+"/transaction/path", nodePath);
                auditService.auditQuery(callback, pathParams, limit);

                AuditQueryParameters copyFromPathParams = new AuditQueryParameters();
                // used to orderBY by field createdAt
                copyFromPathParams.setForward(forward);
                copyFromPathParams.setUser(propertyWalker.getCreatedByUser());
                copyFromPathParams.setFromTime(fromTime);
                copyFromPathParams.setToTime(toTime);
                copyFromPathParams.setApplicationName(auditApplicationName);
                copyFromPathParams.addSearchKey("/"+auditAppId+"/transaction/copy/from/path", nodePath);
                auditService.auditQuery(callback, copyFromPathParams, limit);

                AuditQueryParameters moveFromPathParams = new AuditQueryParameters();
                // used to orderBY by field createdAt
                moveFromPathParams.setForward(forward);
                moveFromPathParams.setUser(propertyWalker.getCreatedByUser());
                moveFromPathParams.setFromTime(fromTime);
                moveFromPathParams.setToTime(toTime);
                moveFromPathParams.setApplicationName(auditApplicationName);
                moveFromPathParams.addSearchKey("/"+auditAppId+"/transaction/move/from/path", nodePath);
                auditService.auditQuery(callback, moveFromPathParams, limit);

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

        return results;
    }

    private class AuditEntriesByNodeIdQueryWalker extends MapBasedQueryWalker
    {
        private Long fromTime;
        private Long toTime;

        public AuditEntriesByNodeIdQueryWalker()
        {
            super(new HashSet<>(Arrays.asList(new String[] { CREATED_BY_USER })), null);
        }

        @Override
        public void and()
        {
            // allow AND, e.g. createdByUser='jbloggs' AND createdAt BETWEEN ('...','...)
        }

        @Override
        public void between(String propertyName, String firstValue, String secondValue, boolean negated)
        {
            if (propertyName.equals(CREATED_AT))
            {
                fromTime = getTime(firstValue);
                toTime = getTime(secondValue) + 1;
            }

        }

        public String getCreatedByUser()
        {
            String propertyValue = getProperty(CREATED_BY_USER, WhereClauseParser.EQUALS, String.class);

            // Check if '-me-' alias is used and replace it with userId
            if ((propertyValue != null) && propertyValue.equalsIgnoreCase(DEFAULT_USER))
            {
                propertyValue = AuditImpl.this.people.validatePerson(propertyValue);
            }
            return propertyValue;
        }

        public Long getFromTime()
        {
            return fromTime;
        }

        public Long getToTime()
        {
            return toTime;
        }

    }
}
