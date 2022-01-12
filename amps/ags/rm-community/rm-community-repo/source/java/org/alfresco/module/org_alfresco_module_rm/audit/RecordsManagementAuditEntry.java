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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * Class to represent a Records Management audit entry.
 *
 * @author Gavin Cornwell
 */
@AlfrescoPublicApi
public final class RecordsManagementAuditEntry
{
    private final Date timestamp;
    private final String userName;
    private final String fullName;
    private final String userRole;
    private final NodeRef nodeRef;
    private final String nodeName;
    private final String nodeType;
    private final String event;
    private final String identifier;
    private final String path;
    private final Map<QName, Serializable> beforeProperties;
    private final Map<QName, Serializable> afterProperties;
    private Map<QName, Pair<Serializable, Serializable>> changedProperties;

    /**
     * Default constructor
     */
    public RecordsManagementAuditEntry(Date timestamp,
                String userName, String fullName, String userRole,
                NodeRef nodeRef, String nodeName, String nodeType,
                String event, String identifier, String path,
                Map<QName, Serializable> beforeProperties,
                Map<QName, Serializable> afterProperties)
    {
        ParameterCheck.mandatory("timestamp", timestamp);
        ParameterCheck.mandatory("userName", userName);

        this.timestamp = timestamp;
        this.userName = userName;
        this.userRole = userRole;
        this.fullName = fullName;
        this.nodeRef = nodeRef;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.event = event;
        this.identifier = identifier;
        this.path = path;
        this.beforeProperties = beforeProperties;
        this.afterProperties = afterProperties;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(")
          .append("timestamp=").append(timestamp)
          .append(", userName=").append(userName)
          .append(", userRole=").append(userRole)
          .append(", fullName=").append(fullName)
          .append(", nodeRef=").append(nodeRef)
          .append(", nodeName=").append(nodeName)
          .append(", event=").append(event)
          .append(", identifier=").append(identifier)
          .append(", path=").append(path)
          .append(", beforeProperties=").append(beforeProperties)
          .append(", afterProperties=").append(afterProperties)
          .append(", changedProperties=").append(changedProperties)
          .append(")");
        return sb.toString();
    }

    /**
     *
     * @return The date of the audit entry
     */
    public Date getTimestamp()
    {
        return this.timestamp;
    }

    /**
     *
     * @return The date of the audit entry as an ISO8601 formatted String
     */
    public String getTimestampString()
    {
        return ISO8601DateFormat.format(this.timestamp);
    }

    /**
     *
     * @return The username of the user that caused the audit log entry to be created
     */
    public String getUserName()
    {
        return this.userName;
    }

    /**
     *
     * @return The full name of the user that caused the audit log entry to be created
     */
    public String getFullName()
    {
        return this.fullName;
    }

    /**
     *
     * @return The role of the user that caused the audit log entry to be created
     */
    public String getUserRole()
    {
        return this.userRole;
    }

    /**
     *
     * @return The NodeRef of the node the audit log entry is for
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }

    /**
     *
     * @return The name of the node the audit log entry is for
     */
    public String getNodeName()
    {
        return this.nodeName;
    }

    /**
     *
     * @return The type of the node the audit log entry is for
     */
    public String getNodeType()
    {
        return this.nodeType;
    }

    /**
     *
     * @return The human readable description of the reason for the audit log
     *         entry i.e. metadata updated, record declared
     */
    public String getEvent()
    {
        return this.event;
    }

    /**
     * An identifier for the item being audited, for example for a record
     * it will be the unique record identifier, for a user it would be the
     * username etc.
     *
     * @return Ad identifier for the thing being audited
     */
    public String getIdentifier()
    {
        return this.identifier;
    }

    /**
     *
     * @return The path to the object being audited
     */
    public String getPath()
    {
        return this.path;
    }

    /**
     *
     * @return Map of properties before the audited action
     */
    public Map<QName, Serializable> getBeforeProperties()
    {
        return this.beforeProperties;
    }

    /**
     *
     * @return Map of properties after the audited action
     */
    public Map<QName, Serializable> getAfterProperties()
    {
        return this.afterProperties;
    }

    /**
     *
     * @return Map of changed properties
     */
    public Map<QName, Pair<Serializable, Serializable>> getChangedProperties()
    {
        if (this.changedProperties == null)
        {
            initChangedProperties();
        }

        return this.changedProperties;
    }

    /**
     * Initialises the map of changed values given the before and after properties
     */
    private void initChangedProperties()
    {
        if (this.beforeProperties != null && this.afterProperties != null)
        {
            this.changedProperties = new HashMap<>(
                    this.beforeProperties.size() + this.afterProperties.size());

            // add all the properties present before the audited action
            for (Map.Entry<QName, Serializable> entry : this.beforeProperties.entrySet())
            {
                final QName valuePropName = entry.getKey();
                Pair<Serializable, Serializable> values = new Pair<>(
                        entry.getValue(),
                        this.afterProperties.get(valuePropName));
                this.changedProperties.put(valuePropName, values);
            }

            // add all the properties present after the audited action that
            // have not already been added
            for (Map.Entry<QName, Serializable> entry : this.afterProperties.entrySet())
            {
                final QName valuePropName = entry.getKey();
                if (!this.beforeProperties.containsKey(valuePropName))
                {
                    Pair<Serializable, Serializable> values = new Pair<>(null, entry.getValue());
                    this.changedProperties.put(valuePropName, values);
                }
            }
        }
    }
}
