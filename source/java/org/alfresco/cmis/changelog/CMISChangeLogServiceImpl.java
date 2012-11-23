/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.cmis.changelog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.cmis.CMISBaseObjectTypeIds;
import org.alfresco.cmis.CMISCapabilityChanges;
import org.alfresco.cmis.CMISChangeEvent;
import org.alfresco.cmis.CMISChangeLog;
import org.alfresco.cmis.CMISChangeLogService;
import org.alfresco.cmis.CMISChangeType;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.CMISChangeLogDataExtractor;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * ChangeLog Service Implementation
 * 
 * @author Dmitry Velichkevich
 */
public class CMISChangeLogServiceImpl implements CMISChangeLogService
{
    private static final String PATH_DELIMITER = "/";
    private static final int DEFAULT_RETURN_SIZE = 100;

    private AuditService auditService;
    private String cmisAuditApplicationName;
    private List<CMISBaseObjectTypeIds> changesOnTypeCapability;

    /**
     * Set the AuditService.
     * 
     * @param auditService AuditService
     */
    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    /**
     * Set the Audit Application Name.
     * 
     * @param cmisAuditApplicationName Audit Application Name
     */
    public void setCmisAuditApplicationName(String cmisAuditApplicationName)
    {
        this.cmisAuditApplicationName = cmisAuditApplicationName;
    }

    /**
     * Set the ChangesOnTypeCapability.
     * 
     * @param changesOnTypeCapability list of CMISBaseObjectTypeIds
     */
    public void setChangesOnTypeCapability(List<CMISBaseObjectTypeIds> changesOnTypeCapability)
    {
        this.changesOnTypeCapability = changesOnTypeCapability;
    }

    /**
     * @see org.alfresco.cmis.CMISChangeLogService#getCapability()
     */
    public CMISCapabilityChanges getCapability()
    {
        return (auditService.isAuditEnabled(cmisAuditApplicationName, PATH_DELIMITER + cmisAuditApplicationName)) ? (CMISCapabilityChanges.OBJECTIDSONLY)
                : (CMISCapabilityChanges.NONE);
    }

    /**
     * @throws CMISInvalidArgumentException 
     * @see org.alfresco.cmis.CMISChangeLogService#getChangeLogEvents(java.lang.String, java.lang.Integer)
     */
    public CMISChangeLog getChangeLogEvents(String changeLogToken, Integer maxItems) throws CMISInvalidArgumentException
    {
        if (!auditService.isAuditEnabled(cmisAuditApplicationName, ("/" + cmisAuditApplicationName)))
        {
            throw new AlfrescoRuntimeException("Auditing for " + cmisAuditApplicationName + " is disabled!");
        }
        CMISChangeLogImpl result = new CMISChangeLogImpl();
        final List<CMISChangeEvent> changeEvents = result.getChangeEvents();
        EntryIdCallback changeLogCollectingCallback = new EntryIdCallback(true)
        {
            @Override
            public boolean handleAuditEntry(Long entryId, String user, long time, Map<String, Serializable> values)
            {
                List<CMISChangeEvent> changeLogEvents = convertValuesMapToChangeLogEvents(values, time);
                changeEvents.addAll(changeLogEvents);
                return super.handleAuditEntry(entryId, user, time, values);
            }
        };
        Long from;
        try
        {
            from = changeLogToken != null ? Long.parseLong(changeLogToken) : null;
        }
        catch (NumberFormatException e)
        {
            throw new CMISInvalidArgumentException("Invalid change log token " + changeLogToken);
        }
        AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(cmisAuditApplicationName);
        params.setForward(true);
        params.setFromId(from);

        // Query one past the last item, so that we know what the next ID is
        int maxAmount = ((null == maxItems) || (0 == maxItems)) ? (0) : (maxItems + 1);
        auditService.auditQuery(changeLogCollectingCallback, params, maxAmount);

        if ((0 != maxAmount) && (changeEvents.size() > maxItems))
        {
            changeEvents.remove(changeEvents.size() - 1);
            result.setNextChangeToken(changeLogCollectingCallback.getEntryId().toString());
            result.setHasMoreItems(true);
        }
        return result;
    }

    /**
     * @see org.alfresco.cmis.CMISChangeLogService#getChangesIncomplete()
     */
    public boolean getChangesIncomplete()
    {
        return true;
    }

    /**
     * @see org.alfresco.cmis.CMISChangeLogService#getLastChangeLogToken()
     */
    public String getLastChangeLogToken()
    {
        EntryIdCallback auditQueryCallback = new EntryIdCallback(false);
        AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(cmisAuditApplicationName);
        params.setForward(false);
        auditService.auditQuery(auditQueryCallback, params, 1);
        return auditQueryCallback.getEntryId();
    }

    /**
     * @see org.alfresco.cmis.CMISChangeLogService#getPreviousPageChangeLogToken(java.lang.String, java.lang.Integer)
     */
    public String getPreviousPageChangeLogToken(String currentPageToken, Integer maxItems)
    {
        if (currentPageToken == null)
        {
            return null;
        }
        maxItems = maxItems == null ? DEFAULT_RETURN_SIZE : maxItems;
        EntryIdCallback auditQueryCallback = new EntryIdCallback(false);
        AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(cmisAuditApplicationName);
        params.setForward(false);
        params.setToId(Long.parseLong(currentPageToken));
        auditService.auditQuery(auditQueryCallback, params, maxItems);
        return auditQueryCallback.getEntryId();
    }

    /**
     * @see org.alfresco.cmis.CMISChangeLogService#getLastPageChangeLogToken(java.lang.String, java.lang.Integer)
     */
    public String getLastPageChangeLogToken(String currentPageToken, Integer maxItems)
    {
        maxItems = maxItems == null ? DEFAULT_RETURN_SIZE : maxItems;
        EntryIdCallback auditQueryCallback = new PageStartEntryIdCallback(maxItems);
        AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(cmisAuditApplicationName);
        if (currentPageToken != null)
        {
            params.setFromId(Long.parseLong(currentPageToken));
        }
        auditService.auditQuery(auditQueryCallback, params, -1);
        return auditQueryCallback.getEntryId();
    }

    /**
     * @see org.alfresco.cmis.CMISChangeLogService#getChangesOnTypeCapability()
     */
    public List<CMISBaseObjectTypeIds> getChangesOnTypeCapability()
    {
        if (null == changesOnTypeCapability)
        {
            changesOnTypeCapability = new LinkedList<CMISBaseObjectTypeIds>();
        }
        return changesOnTypeCapability;
    }

    /**
     * Converts audit values map to list of CMISChangeEvents.
     * 
     * @param values audit values map
     * @param time audit event time
     * @return list of CMISChangeEvent
     */
    @SuppressWarnings("unchecked")
    private List<CMISChangeEvent> convertValuesMapToChangeLogEvents(Map<String, Serializable> values, long time)
    {
        List<CMISChangeEvent> result = new ArrayList<CMISChangeEvent>();
        if (values != null && values.size() > 0)
        {
            for (Entry<String, Serializable> entry : values.entrySet())
            {
                if (entry.getKey() != null && entry.getValue() != null)
                {
                    String path = entry.getKey();
                    CMISChangeType changeType = getCMISChangeType(path);
                    if (changeType != null && entry.getValue() instanceof Map)
                    {
                        Map<String, Serializable> valueMap = (Map<String, Serializable>)entry.getValue();
                        result.add(new CMISChangeEventImpl(changeType, new Date(time), (NodeRef) valueMap
                                .get(CMISChangeLogDataExtractor.KEY_NODE_REF), (String) valueMap
                                .get(CMISChangeLogDataExtractor.KEY_OBJECT_ID)));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets CMISChangeType by audit path.
     * 
     * @param auditPath audit path
     * @return CMISChangeType
     */
    private CMISChangeType getCMISChangeType(String auditPath)
    {
        CMISChangeType result = null;
        if (auditPath != null)
        {
            if (auditPath.startsWith(PATH_DELIMITER))
            {
                auditPath = auditPath.substring(PATH_DELIMITER.length());
            }
            if (auditPath.startsWith(cmisAuditApplicationName))
            {
                auditPath = auditPath.substring(cmisAuditApplicationName.length());
            }
            if (auditPath.startsWith(PATH_DELIMITER))
            {
                auditPath = auditPath.substring(PATH_DELIMITER.length());
            }
            auditPath = auditPath.toLowerCase();
            for (CMISChangeType changeType : CMISChangeType.values())
            {
                if (auditPath.startsWith(changeType.getLabel()))
                {
                    result = changeType;
                    break;
                }
            }
        }
        return result;
    }

    private class EntryIdCallback implements AuditQueryCallback
    {
        private final boolean valuesRequired;
        private Long entryId;

        public EntryIdCallback(boolean valuesRequired)
        {
            this.valuesRequired = valuesRequired;
        }

        public String getEntryId()
        {
            return entryId == null ? null : entryId.toString();
        }

        public boolean valuesRequired()
        {
            return this.valuesRequired;
        }

        public final boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values)
        {
            if (applicationName.equals(CMISChangeLogServiceImpl.this.cmisAuditApplicationName))
            {
                return handleAuditEntry(entryId, user, time, values);
            }
            return true;
        }

        public boolean handleAuditEntry(Long entryId, String user, long time, Map<String, Serializable> values)
        {
            this.entryId = entryId;
            return true;
        }

        public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
        {
            throw new AlfrescoRuntimeException(errorMsg, error);
        }
    };

    private class PageStartEntryIdCallback extends EntryIdCallback
    {
        private final int pageSize;
        private int indexWithinPage;

        public PageStartEntryIdCallback(int pageSize)
        {
            super(false);
            this.pageSize = pageSize;
            this.indexWithinPage = -1;
        }

        @Override
        public boolean handleAuditEntry(Long entryId, String user, long time, Map<String, Serializable> values)
        {
            if (++this.indexWithinPage == this.pageSize)
            {
                this.indexWithinPage = 0;
                return super.handleAuditEntry(entryId, user, time, values);
            }
            return true;
        }
    }
}
