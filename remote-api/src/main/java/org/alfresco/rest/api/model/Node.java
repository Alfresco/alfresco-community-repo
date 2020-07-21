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
package org.alfresco.rest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.api.search.model.SearchEntry;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Concrete class carrying general information for <b>alf_node</b> data
 *
 * @author steveglover
 * @author Gethin James
 * @author janv
 */
public class Node implements Comparable<Node>
{
    private static final Log logger = LogFactory.getLog(Node.class);

    protected NodeRef nodeRef;
    protected String name;

    protected Date createdAt;
    protected Date modifiedAt;
    protected UserInfo createdByUser;
    protected UserInfo modifiedByUser;

    // Archived info - specifically for archive (deleted) node - see Trashcan API
    protected Date archivedAt;
    protected UserInfo archivedByUser;

    // Version info - specifically for version node - see Version History API
    protected String versionLabel;
    protected String versionComment;
    protected String nodeId; //This is the frozen node id NOT the current node id

    protected Boolean isFolder;
    protected Boolean isFile;
    protected Boolean isLink;
    protected Boolean isLocked;

    protected NodeRef parentNodeRef;
    protected PathInfo pathInfo;
    protected String prefixTypeQName;

    // please note: these are currently only used (optionally) for node create request
    protected String relativePath;
    protected List<AssocChild> secondaryChildren;
    protected List<AssocTarget> targets;


    protected List<String> aspectNames;
    protected Map<String, Object> properties;

    protected List<String> allowableOperations;
    protected NodePermissions nodePermissions;

    //optional SearchEntry (only ever returned from a search)
    protected SearchEntry search = null;
    protected String location;
    protected Boolean isFavorite;

    public Node(NodeRef nodeRef, NodeRef parentNodeRef, Map<QName, Serializable> nodeProps, Map<String, UserInfo> mapUserInfo, ServiceRegistry sr)
    {
        if(nodeRef == null)
        {
            throw new IllegalArgumentException();
        }

        this.nodeRef = nodeRef;
        this.parentNodeRef = parentNodeRef;

        mapMinimalInfo(nodeProps, mapUserInfo, sr);
    }

    protected Object getValue(Map<String, PropertyData<?>> props, String name)
    {
        PropertyData<?> prop = props.get(name);
        Object value = (prop != null ? prop.getFirstValue() : null);
        return value;
    }

    public Node()
    {
    }

    protected void mapMinimalInfo(Map<QName, Serializable> nodeProps,  Map<String, UserInfo> mapUserInfo, ServiceRegistry sr)
    {
        PersonService personService = sr.getPersonService();

        this.name = (String)nodeProps.get(ContentModel.PROP_NAME);

        if (mapUserInfo == null) {
            // minor: save one lookup if creator & modifier are the same
            mapUserInfo = new HashMap<>(2);
        }

        this.createdAt = (Date)nodeProps.get(ContentModel.PROP_CREATED);
        this.createdByUser = lookupUserInfo((String)nodeProps.get(ContentModel.PROP_CREATOR), mapUserInfo, personService);

        this.modifiedAt = (Date)nodeProps.get(ContentModel.PROP_MODIFIED);
        this.modifiedByUser = lookupUserInfo((String)nodeProps.get(ContentModel.PROP_MODIFIER), mapUserInfo, personService);
    }

    public static UserInfo lookupUserInfo(String userName, Map<String, UserInfo> mapUserInfo, PersonService personService)
    {
        return lookupUserInfo(userName, mapUserInfo, personService, false);
    }

    public static UserInfo lookupUserInfo(String userName, Map<String, UserInfo> mapUserInfo, PersonService personService, boolean displayNameOnly)
    {
        UserInfo userInfo = mapUserInfo.get(userName);
        if ((userInfo == null) && (userName != null))
        {
            String sysUserName = AuthenticationUtil.getSystemUserName();
            if (userName.equals(sysUserName) || (AuthenticationUtil.isMtEnabled() && userName.startsWith(sysUserName + "@")))
            {
                userInfo = new UserInfo((displayNameOnly ? null : userName), userName, "");
            }
            else
            {
                PersonService.PersonInfo pInfo = null;
                try
                {
                    NodeRef pNodeRef = personService.getPersonOrNull(userName);
                    if (pNodeRef != null)
                    {
                        pInfo = personService.getPerson(pNodeRef);
                    }
                }
                catch (NoSuchPersonException nspe)
                {
                    // drop-through
                }
                catch (AccessDeniedException ade)
                {
                    // SFS-610
                    // drop-through
                }

                if (pInfo != null)
                {
                    userInfo = new UserInfo((displayNameOnly ? null : userName), pInfo.getFirstName(), pInfo.getLastName());
                }
                else
                {
                    logger.warn("Unknown person: "+userName);
                    userInfo = new UserInfo((displayNameOnly ? null : userName), userName, "");
                }
            }

            mapUserInfo.put(userName, userInfo);
        }
        return userInfo;
    }

    // note: nodeRef maps to json "id" (when serializing/deserializng)

    @JsonProperty("id")
    @UniqueId
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    public Date getCreatedAt()
    {
        return this.createdAt;
    }

    public void setCreated(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt()
    {
        return modifiedAt;
    }

    public UserInfo getModifiedByUser() {
        return modifiedByUser;
    }

    public UserInfo getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(UserInfo createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public PathInfo getPath()
    {
        return pathInfo;
    }

    public void setPath(PathInfo pathInfo)
    {
        this.pathInfo = pathInfo;
    }

    public String getNodeType()
    {
        return prefixTypeQName;
    }

    public void setNodeType(String prefixType)
    {
        this.prefixTypeQName = prefixType;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, Object> props) {
        this.properties = props;
    }

    public List<String> getAspectNames() {
        return aspectNames;
    }

    public void setAspectNames(List<String> aspectNames) {
        this.aspectNames = aspectNames;
    }

    public NodeRef getParentId()
    {
        return parentNodeRef;
    }

    public void setParentId(NodeRef parentNodeRef)
    {
        this.parentNodeRef = parentNodeRef;
    }

    public Boolean getIsFolder()
    {
        return isFolder;
    }

    public void setIsFolder(Boolean isFolder)
    {
        this.isFolder = isFolder;
    }

    public Boolean getIsFile()
    {
        return isFile;
    }

    public void setIsFile(Boolean isFile)
    {
        this.isFile = isFile;
    }

    public Boolean getIsLink()
    {
        return isLink;
    }

    public void setIsLink(Boolean isLink)
    {
        this.isLink = isLink;
    }

    public Boolean getIsLocked()
    {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked)
    {
        this.isLocked = isLocked;
    }

    public Boolean getIsFavorite()
    {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite)
    {
        this.isFavorite = isFavorite;
    }

    public List<String> getAllowableOperations()
    {
        return allowableOperations;
    }

    public void setAllowableOperations(List<String> allowableOperations)
    {
        this.allowableOperations = allowableOperations;
    }

    public NodePermissions getPermissions()
    {
        return nodePermissions;
    }

    public void setPermissions(NodePermissions nodePermissions)
    {
        this.nodePermissions = nodePermissions;
    }

    public List<AssocTarget> getTargets()
    {
        return targets;
    }

    public void setTargets(List<AssocTarget> targets)
    {
        this.targets = targets;
    }

    public Date getArchivedAt()
    {
        return archivedAt;
    }

    public void setArchivedAt(Date archivedAt)
    {
        this.archivedAt = archivedAt;
    }

    public UserInfo getArchivedByUser()
    {
        return archivedByUser;
    }

    public void setArchivedByUser(UserInfo archivedByUser)
    {
        this.archivedByUser = archivedByUser;
    }

    public String getVersionLabel()
    {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel)
    {
        this.versionLabel = versionLabel;
    }

    public String getVersionComment()
    {
        return versionComment;
    }

    public void setVersionComment(String versionComment)
    {
        this.versionComment = versionComment;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
    }

    public boolean equals(Object other)
    {
        if(this == other)
        {
            return true;
        }

        if(!(other instanceof Node))
        {
            return false;
        }

        Node node = (Node)other;
        return EqualsHelper.nullSafeEquals(getNodeRef(), node.getNodeRef());
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public void setRelativePath(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public List<AssocChild> getSecondaryChildren()
    {
        return secondaryChildren;
    }

    public void setSecondaryChildren(List<AssocChild> secondaryChildren)
    {
        this.secondaryChildren = secondaryChildren;
    }

    @Override
    public int compareTo(Node node)
    {
        return getNodeRef().toString().compareTo(node.getNodeRef().toString());
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Node [id=").append(getNodeRef().getId());
        sb.append(", parentId=").append(getParentId());
        sb.append(", type=").append(getNodeType());
        sb.append(", name=").append(getName());
        sb.append(", isFolder=").append(getIsFolder());
        sb.append(", isFile=").append(getIsFile());
        sb.append(", modifiedAt=").append(getModifiedAt());
        sb.append(", modifiedByUser=").append(getModifiedByUser());
        sb.append(", createdAt=").append(getCreatedAt());
        sb.append(", createdByUser=").append(getCreatedByUser());
        if (getArchivedAt() != null)
        {
            sb.append(", archivedAt=").append(getArchivedAt());
        }
        if (getArchivedByUser() != null)
        {
            sb.append(", archivedByUser=").append(getArchivedByUser());
        }
        if (getVersionLabel() != null)
        {
            sb.append(", versionLabel=").append(getVersionLabel());
        }
        if (getVersionComment() != null)
        {
            sb.append(", versionComment=").append(getVersionComment());
        }
        if (getLocation() != null)
        {
            sb.append(", location=").append(getLocation());
        }
        if (getNodeId() != null)
        {
            sb.append(", nodeId=").append(getNodeId());
        }
        if (getIsLink() != null)
        {
            sb.append(", isLink=").append(getIsLink()); // note: symbolic link (not shared link)
        }
        if (getPath() != null)
        {
            sb.append(", path=").append(getPath());
        }
        if (getContent() != null)
        {
            sb.append(", content=").append(getContent());
        }
        if (getAspectNames() != null)
        {
            sb.append(", aspectNames=").append(getAspectNames());
        }
        if (getProperties() != null)
        {
            //sb.append(", properties=").append(getProperties());
        }
        if (getRelativePath() != null)
        {
            sb.append(", relativePath=").append(getRelativePath());
        }
        if (getAllowableOperations() != null)
        {
            sb.append(", allowableOperations=").append(getAllowableOperations());
        }
        if (getSearch() != null)
        {
            sb.append(", search=").append(getSearch());
        }
        sb.append("]");
        return sb.toString();
    }

    // here to allow POST /nodes/{id}/children when creating empty file with specified content.mimeType
    // also allows list of results to be returned as "nodes"

    protected ContentInfo contentInfo;

    public void setContent(ContentInfo contentInfo)
    {
        this.contentInfo = contentInfo;
    }

    public ContentInfo getContent()
    {
        return this.contentInfo;
    }

    // when appropriate, can be used to show association (in the context of a listing), for example
    // GET /nodes/parentId/children, /nodes/parentId/secondary-children, /nodes/childId/parents
    // GET /nodes/sourceId/targets, /nodes/targetId/sources
    protected Assoc association;

    public Assoc getAssociation()
    {
        return association;
    }

    public void setAssociation(Assoc association)
    {
        this.association = association;
    }

    public SearchEntry getSearch()
    {
        return search;
    }

    public void setSearch(SearchEntry search)
    {
        this.search = search;
    }
    // TODO for backwards compat' - set explicitly when needed (ie. favourites) (note: we could choose to have separate old Node/NodeImpl etc)

    protected String title;
    protected NodeRef guid;
    protected String description;
    protected String createdBy;
    protected String modifiedBy;

    /**
     * @deprecated
     */
    public NodeRef getGuid() {
        return guid;
    }

    /**
     * @deprecated
     */
    public void setGuid(NodeRef guid)
    {
        this.guid = guid;
    }

    /**
     * @deprecated
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @deprecated
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @deprecated
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @deprecated
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @deprecated
     */
    public String getCreatedBy()
    {
        return this.createdBy;
    }

    /**
     * @deprecated
     */
    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }

    /**
     * @deprecated
     */
    public String getModifiedBy()
    {
        return modifiedBy;
    }

    /**
     * @deprecated
     */
    public void setModifiedBy(String modifiedBy)
    {
        this.modifiedBy = modifiedBy;
    }

}