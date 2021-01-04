/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.rm.rest.api.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * POJO object carrying information of a transfer node
 * 
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
public class Transfer
{
    public static final String PARAM_TRANSFER_ACCESSION_INDICATOR = "transferAccessionIndicator";
    public static final String PARAM_TRANSFER_LOCATION = "transferLocation";
    public static final String PARAM_TRANSFER_PDF_INDICATOR = "transferPDFIndicator";

    protected NodeRef nodeRef;
    protected NodeRef parentNodeRef;
    protected String name;
    protected String nodeType;

    protected Date createdAt;
    protected UserInfo createdByUser;
    // optional properties
    protected List<String> aspectNames;
    protected Map<String, Object> properties;
    protected List<String> allowableOperations;
    private Boolean transferPDFIndicator;
    private String transferLocation;
    private Boolean transferAccessionIndicator;

    public Transfer()
    {
        //Default constructor
    }

    @JsonProperty ("id")
    @UniqueId
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    public NodeRef getParentId()
    {
        return parentNodeRef;
    }

    public void setParentId(NodeRef parentNodeRef)
    {
        this.parentNodeRef = parentNodeRef;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public UserInfo getCreatedByUser()
    {
        return createdByUser;
    }

    public void setCreatedByUser(UserInfo createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    public List<String> getAspectNames()
    {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames)
    {
        this.aspectNames = aspectNames;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }

    public List<String> getAllowableOperations()
    {
        return allowableOperations;
    }

    public void setAllowableOperations(List<String> allowableOperations)
    {
        this.allowableOperations = allowableOperations;
    }

    public Boolean getTransferPDFIndicator()
    {
        return transferPDFIndicator;
    }

    public void setTransferPDFIndicator(Boolean transferPDFIndicator)
    {
        this.transferPDFIndicator = transferPDFIndicator;
    }

    public String getTransferLocation()
    {
        return transferLocation;
    }

    public void setTransferLocation(String transferLocation)
    {
        this.transferLocation = transferLocation;
    }

    public Boolean getTransferAccessionIndicator()
    {
        return transferAccessionIndicator;
    }

    public void setTransferAccessionIndicator(Boolean transferAccessionIndicator)
    {
        this.transferAccessionIndicator = transferAccessionIndicator;
    }
}
