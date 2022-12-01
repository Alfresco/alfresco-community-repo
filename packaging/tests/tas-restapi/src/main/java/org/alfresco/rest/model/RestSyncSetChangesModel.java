/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
/*
 * Copyright 2017 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */

package org.alfresco.rest.model;

import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base Path {@linkplain /alfresco/api/-default-/private/alfresco/versions/1}
 * 
 * @author Meenal Bhave
 * Example:
 * "changes": [
    {
        "conflict": false,
        "skip": false,
        "id": "1",
        "username": "admin",
        "type": "DELETE_REPOS",
        "name": "2",
        "toName": null,
        "parentNodeIds": [
            "1007f928-3d95-402f-a9ac-35300c66f3fa",
            "02a172dd-3156-4443-a216-d11bcd082ccc",
            "e1f3777a-4eb3-4e49-9e65-ec52dbf21032",
            "859d721e-07b4-4b8f-8a9e-122d45a1f9bb",
            "5c9b0f55-eb3a-48ba-8532-c2877548291e",
            "c0a5ad7b-cf36-47f1-85a6-6c0aafe242df"
            ],
        "toParentNodeIds": [],
        "path": "/Company Home/Sites/primary/documentLibrary/5/2",
        "toPath": null,
        "nodeId": "aa667a0c-03bb-44b8-a65c-bdc95a90a6ac",
        "eventTimestamp": 1505838692779,
        "checksum": "dummyChecksum",
        "size": -1,
        "nodeType": "cm:folder",
        "nodeTimestamp": 1505838597992,
        "error": false,
        "aspects": [
            "cm:titled",
            "cm:auditable",
            "sys:referenceable",
            "sys:pendingDelete",
            "sys:localized"
            ],
        "seqNo": 0,
        "parentGroup": null,
        "permission": null,
        "async": false,
        "authority": null,
        "cascade": false
        "folderChange": false
    }
    ]
 */

public class RestSyncSetChangesModel extends TestModel
{

    public RestSyncSetChangesModel()
    {
    }

    @JsonProperty(required = true)
    private Boolean conflict;

    @JsonProperty(required = true)
    private Boolean skip;

    @JsonProperty(required = true)
    private String id;

    @JsonProperty(required = true)
    private String username;

    @JsonProperty(required = true)
    private String type;

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String toName;

    @JsonProperty(required = true)
    private String[] parentNodeIds;

    @JsonProperty(required = true)
    private String[] toParentNodeIds;

    @JsonProperty(required = true)
    private String path;

    @JsonProperty(required = true)
    private String toPath;

    @JsonProperty(required = true)
    private String nodeId;

    @JsonProperty(required = true)
    private String eventTimestamp;

    @JsonProperty(required = true)
    private String checksum;

    @JsonProperty(required = true)
    private Integer size;

    @JsonProperty(required = true)
    private String nodeType;

    @JsonProperty(required = true)
    private String nodeTimestamp;

    @JsonProperty(required = true)
    private Boolean error;

    @JsonProperty(required = true)
    private String[] aspects;

    @JsonProperty(required = true)
    private Integer seqNo;

    @JsonProperty(required = true)
    private String parentGroup;

    @JsonProperty(required = true)
    private String permission;

    @JsonProperty(required = true)
    private Boolean async;

    @JsonProperty(required = true)
    private String authority;

    @JsonProperty(required = true)
    private Boolean cascade;
    
    @JsonProperty(required = true)
    private boolean recordFromCollabSite;
    
    @JsonProperty(required = true)
    private int numberOfSecondaryAssocs;

    @JsonProperty(required = true)
    private boolean folderChange;
    
    

    public Boolean getConflict()
    {
        return conflict;
    }

    public Boolean getSkip()
    {
        return skip;
    }

    public String getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public String getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public String getToName()
    {
        return toName;
    }

    public String[] getParentNodeIds()
    {
        return parentNodeIds;
    }

    public String[] getToParentNodeIds()
    {
        return toParentNodeIds;
    }

    public String getPath()
    {
        return path;
    }

    public String getToPath()
    {
        return toPath;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public String getEventTimestamp()
    {
        return eventTimestamp;
    }

    public String getCheckSum()
    {
        return checksum;
    }

    public Integer getSize()
    {
        return size;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    public String getNodeTimeStamp()
    {
        return nodeTimestamp;
    }

    public Boolean getError()
    {
        return error;
    }

    public String[] getAspects()
    {
        return aspects;
    }

    public Integer getSeqNo()
    {
        return seqNo;
    }

    public String getParentGroup()
    {
        return parentGroup;
    }

    public String getPermissions()
    {
        return permission;
    }

    public Boolean getAsync()
    {
        return async;
    }

    public String getAuthority()
    {
        return authority;
    }

    public Boolean getCascade()
    {
        return cascade;
    }

    public boolean getRecordFromCollabSite()
    {
        return recordFromCollabSite;
    }

    public void setRecordFromCollabSite(boolean recordFromCollabSite)
    {
        this.recordFromCollabSite = recordFromCollabSite;
    }

    public int getNumberOfSecondaryAssocs()
    {
        return numberOfSecondaryAssocs;
    }

    public void setNumberOfSecondaryAssocs(int numberOfSecondaryAssocs)
    {
        this.numberOfSecondaryAssocs = numberOfSecondaryAssocs;
    }

    public boolean isFolderChange() {
        return folderChange;
    }

    public void setFolderChange(boolean folderChange) {
        this.folderChange = folderChange;
    }
}
