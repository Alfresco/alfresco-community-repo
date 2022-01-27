/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.audit;

import java.util.Date;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Class to represent the parameters for a Records Management
 * audit log query.
 *
 * @author Gavin Cornwell
 */
@AlfrescoPublicApi
public final class RecordsManagementAuditQueryParameters
{
    private int maxEntries = Integer.MAX_VALUE;
    private String user;
    private NodeRef nodeRef;
    private Date dateFrom;
    private Date dateTo;
    private String event;
    private QName property;

    /**
     * Default constructor.
     */
    public RecordsManagementAuditQueryParameters()
    {
        //Default constructor
    }

    /**
     *
     * @return The username to filter by
     */
    public String getUser()
    {
        return this.user;
    }

    /**
     * Restricts the retrieved audit trail to entries made by
     * the provided user.
     *
     * @param user The username to filter by
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     *
     * @return The maximum number of audit log entries to retrieve
     */
    public int getMaxEntries()
    {
        return this.maxEntries;
    }

    /**
     * Restricts the retrieved audit trail to the last
     * <code>maxEntries</code> entries.
     *
     * @param maxEntries Maximum number of entries
     */
    public void setMaxEntries(int maxEntries)
    {
        this.maxEntries = maxEntries;
    }

    /**
     *
     * @return The node to get entries for
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }

    /**
     * Restricts the retrieved audit trail to only those entries
     * created by the give node.
     *
     * @param nodeRef The node to get entries for
     */
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    /**
     *
     * @return The date to retrieve entries from
     */
    public Date getDateFrom()
    {
        return this.dateFrom;
    }

    /**
     * Restricts the retrieved audit trail to only those entries
     * that occurred after the given date.
     *
     * @param dateFrom Date to retrieve entries after
     */
    public void setDateFrom(Date dateFrom)
    {
        this.dateFrom = dateFrom;
    }

    /**
     *
     * @return The date to retrive entries to
     */
    public Date getDateTo()
    {
        return this.dateTo;
    }

    /**
     * Restricts the retrieved audit trail to only those entries
     * that occurred before the given date.
     *
     * @param dateTo Date to retrieve entries before
     */
    public void setDateTo(Date dateTo)
    {
        this.dateTo = dateTo;
    }

    /**
     *
     * @return The event to retrive entries for
     */
    public String getEvent()
    {
        return this.event;
    }

    /**
     * Restricts the retrieved audit trail to only those entries
     * that match the given event string.
     *
     * @param event Event to retrieve entries for
     */
    public void setEvent(String event)
    {
        this.event = event;
    }

    /**
     *
     * @return The property to retrieve entries for
     */
    public QName getProperty()
    {
        return this.property;
    }

    /**
     * Restricts the audit trail to only those entries that involve
     * the given property.
     *
     * @param property The property to retrieve entries for
     */
    public void setProperty(QName property)
    {
        this.property = property;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(super.toString());

        builder.append(" (nodeRef='").append(nodeRef).append("', user='")
        .append(user).append("', dateFrom='").append(dateFrom)
        .append("', dateTo='").append(dateTo).append("', maxEntries='")
        .append(maxEntries).append("', event='").append(event)
        .append("', property='").append(property).append("')");

        return builder.toString();
    }
}
