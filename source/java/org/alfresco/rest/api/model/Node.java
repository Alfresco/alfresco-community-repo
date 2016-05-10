/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

/**
 * Concrete class carrying general information for <b>alf_node</b> data
 *
 * @author steveglover
 * @author Gethin James
 * @author janv
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Node implements Comparable<Node>
{
    protected NodeRef nodeRef;
    protected String name;

    protected Date createdAt;
    protected Date modifiedAt;
    protected UserInfo createdByUser;
    protected UserInfo modifiedByUser;

    protected Boolean isFolder;
    protected Boolean isLink;

    protected NodeRef parentNodeRef;
    protected PathInfo pathInfo;
    protected String prefixTypeQName;

    protected List<String> aspectNames;

    protected Map<String, Object> properties;

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

    public static UserInfo lookupUserInfo(String userName, Map<String, UserInfo> mapUserInfo, PersonService personService) {

        UserInfo userInfo = mapUserInfo.get(userName);
        if ((userInfo == null) && (userName != null))
        {
            String sysUserName = AuthenticationUtil.getSystemUserName();
            if (userName.equals(sysUserName) || (AuthenticationUtil.isMtEnabled() && userName.startsWith(sysUserName + "@")))
            {
                userInfo = new UserInfo(userName, userName, "");
            }
            else
            {
                try
                {
                    PersonService.PersonInfo pInfo = personService.getPerson(personService.getPerson(userName));
                    userInfo = new UserInfo(userName, pInfo.getFirstName(), pInfo.getLastName());
                }
                catch (NoSuchPersonException nspe)
                {
                    // belts-and-braces (seen in dev/test env, eg. userName = Bobd58ba329-b702-41ee-a9ae-2b3c7029b5bc
                    userInfo = new UserInfo(userName, userName, "");
                }

            }

            mapUserInfo.put(userName, userInfo);
        }
        return userInfo;
    }

    private String getNodeRefAsString(NodeRef nRef) {
        return (nRef != null ? nRef.getId() : null);
    }

    private NodeRef getStringAsNodeRef(String nRefString) {
        if (nRefString == null)
        {
            return null;
        }

        if( ! NodeRef.isNodeRef(nRefString))
        {
            return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nRefString);
        }
        else
        {
            return new NodeRef(nRefString);
        }
    }

    public String getId()
    {
        return getNodeRefAsString(this.nodeRef);
    }

    public void setId(String id)
    {
       this.nodeRef = getStringAsNodeRef(id);
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

    public String getParentId()
    {
        return getNodeRefAsString(parentNodeRef);
    }

    public void setParentId(String parentId)
    {
        this.parentNodeRef = getStringAsNodeRef(parentId);
    }

    public Boolean getIsFolder()
    {
        return isFolder;
    }

    public void setIsFolder(Boolean isFolder)
    {
        this.isFolder = isFolder;
    }

    public Boolean getIsLink()
    {
        return isLink;
    }

    public void setIsLink(Boolean isLink)
    {
        this.isLink = isLink;
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
        return EqualsHelper.nullSafeEquals(getId(), node.getId());
    }

    @Override
    public int compareTo(Node node)
    {
        return getId().toString().compareTo(node.getId().toString());
    }

    @Override
    public String toString()
    {
        return "Node [nodeRef=" + nodeRef + ", type=" + prefixTypeQName + ", name=" + name + ", title="
                + title + ", description=" + description + ", createdAt="
                + createdAt + ", modifiedAt=" + modifiedAt + ", createdByUser=" + createdByUser + ", modifiedBy="
                + modifiedByUser + ", pathInfo =" + pathInfo +"]";
    }

    protected ContentInfo contentInfo;

    public ContentInfo getContent()
    {
        return contentInfo;
    }

    public void setContent(ContentInfo contentInfo)
    {
        this.contentInfo = contentInfo;
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
     * @deprecated note: used when creating (via POST) favourite target
     */
    public void setGuid(NodeRef guid)
    {
        this.guid = guid;
        setId(guid.toString());
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