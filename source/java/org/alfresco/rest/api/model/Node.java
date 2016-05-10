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
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
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
public class Node implements Comparable<Node>
{
    protected NodeRef nodeRef;
    protected String name;

    // TODO needed for favourties - backwards compat' - we could also choose to split of NodeInfo / Node impl's etc
    protected String title;
    protected NodeRef guid;
    protected String description;
    protected String createdBy;
    protected String modifiedBy;

    protected Date createdAt;
    protected Date modifiedAt;
    protected UserInfo createdByUser;
    protected UserInfo modifiedByUser;

    protected NodeRef parentNodeRef;
    protected PathInfo pathInfo;
    protected String prefixTypeQName;

    protected List<String> aspectNames;

    protected Map<String, Serializable> props;

    // TODO fixme !
    // also need to optionally pass in user map - eg. when listing children (to avoid multiple lookups for same user)
    public Node(NodeRef nodeRef, NodeRef parentNodeRef, Map<QName, Serializable> nodeProps, ServiceRegistry sr)
    {
        if(nodeRef == null)
        {
            throw new IllegalArgumentException();
        }

        this.nodeRef = nodeRef;
        this.parentNodeRef = parentNodeRef;

        mapBasicInfo(nodeProps, sr);
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

    protected void mapBasicInfo(Map<QName, Serializable> nodeProps, ServiceRegistry sr)
    {
        PersonService personService = sr.getPersonService();

        // TODO review backwards compat' for favorites & others (eg. set guid explicitly where still needed)
        //this.guid = nodeRef;
        //this.title = (String)nodeProps.get(ContentModel.PROP_TITLE);
        //this.description = (String)nodeProps.get(ContentModel.PROP_DESCRIPTION);
        //this.createdBy = (String)nodeProps.get(ContentModel.PROP_CREATOR);
        //this.modifiedBy = (String)nodeProps.get(ContentModel.PROP_MODIFIER);

        this.name = (String)nodeProps.get(ContentModel.PROP_NAME);

        this.createdAt = (Date)nodeProps.get(ContentModel.PROP_CREATED);
        this.createdByUser = lookupUserInfo((String)nodeProps.get(ContentModel.PROP_CREATOR), personService);

        this.modifiedAt = (Date)nodeProps.get(ContentModel.PROP_MODIFIED);
        this.modifiedByUser = lookupUserInfo((String)nodeProps.get(ContentModel.PROP_MODIFIER), personService);
    }

    // TODO refactor & optimise to avoid multiple person lookups
    private UserInfo lookupUserInfo(final String userName, final PersonService personService) {

        String sysUserName = AuthenticationUtil.getSystemUserName();
        if (userName.equals(sysUserName) || (AuthenticationUtil.isMtEnabled() && userName.startsWith(sysUserName+"@")))
        {
            return new UserInfo(userName, userName, "");
        }
        else
        {
            PersonService.PersonInfo pInfo = personService.getPerson(personService.getPerson(userName));
            return new UserInfo(userName, pInfo.getFirstName(), pInfo.getLastName());
        }
    }

    public void setGuid(NodeRef guid)
    {
        this.guid = guid;
    }

    public NodeRef getGuid() {
        return guid;
    }

    public String getTitle()
    {
        return title;
    }

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

    public String getModifiedBy()
    {
        return modifiedBy;
    }

    public UserInfo getModifiedByUser() {
        return modifiedByUser;
    }

    public UserInfo getCreatedByUser() {
        return createdByUser;
    }

    public String getDescription()
    {
        return description;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCreatedBy()
    {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
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

    public Map getProperties() {
        return this.props;
    }

    public void setProperties(Map props) {
        this.props = props;
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

    @Override
    public int compareTo(Node node)
    {
        return getNodeRef().toString().compareTo(node.getNodeRef().toString());
    }

    @Override
    public String toString()
    {
        return "Node [nodeRef=" + nodeRef + ", type=" + prefixTypeQName + ", name=" + name + ", title="
                + title + ", description=" + description + ", createdAt="
                + createdAt + ", modifiedAt=" + modifiedAt + ", createdByUser=" + createdByUser + ", modifiedBy="
                + modifiedByUser + ", pathInfo =" + pathInfo +"]";
    }
}