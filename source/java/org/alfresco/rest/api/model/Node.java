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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
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
	protected String title;
	protected NodeRef guid; // TODO review - do we need for favorites (backwards compat') ?
	protected String description;
    protected Date createdAt;
    protected Date modifiedAt;
    protected String createdBy;
    protected String modifiedBy;
    
    protected String primaryPath;
    protected String prefixTypeQName;

    protected Map<String, Serializable> props;

    private  static final List<QName> EXCLUDED_PROPS = Arrays.asList(
            ContentModel.PROP_NAME,
            ContentModel.PROP_TITLE,
			ContentModel.PROP_DESCRIPTION,
			ContentModel.PROP_MODIFIER,
			ContentModel.PROP_MODIFIED,
			ContentModel.PROP_CREATOR,
			ContentModel.PROP_CREATED,
            ContentModel.PROP_CONTENT,
            ContentModel.PROP_LOCALE,
            ContentModel.PROP_NODE_UUID,
            ContentModel.PROP_STORE_IDENTIFIER,
            ContentModel.PROP_STORE_PROTOCOL,
            ContentModel.PROP_NODE_DBID,
            ContentModel.PROP_INITIAL_VERSION,
            ContentModel.PROP_AUTO_VERSION_PROPS,
            ContentModel.PROP_AUTO_VERSION);

    // TODO fixme !
    public Node(NodeRef nodeRef, Map<QName, Serializable> nodeProps, NamespaceService namespaceService)
    {
    	if(nodeRef == null)
    	{
    		throw new IllegalArgumentException();
    	}

    	this.nodeRef = nodeRef;
        mapProperties(nodeProps, namespaceService);
    }
    
	protected Object getValue(Map<String, PropertyData<?>> props, String name)
	{
		PropertyData<?> prop = props.get(name);
		Object value = (prop != null ? prop.getFirstValue() : null);
		return value;
	}

    /*
	public Node(NodeRef nodeRef, Properties properties)
	{
    	this.nodeRef = nodeRef;

		Map<String, PropertyData<?>> props = properties.getProperties();

        this.guid = nodeRef;

		this.name = (String)getValue(props, PropertyIds.NAME);
		this.title = (String)getValue(props, ContentModel.PROP_TITLE.toString());
		this.description = (String)getValue(props, PropertyIds.DESCRIPTION);

		GregorianCalendar cal = (GregorianCalendar)getValue(props, PropertyIds.CREATION_DATE);
		this.createdAt = cal.getTime();
		cal = (GregorianCalendar)getValue(props, PropertyIds.LAST_MODIFICATION_DATE);
		this.modifiedAt = cal.getTime();
		this.createdBy = (String)getValue(props, PropertyIds.CREATED_BY);
		this.modifiedBy = (String)getValue(props, PropertyIds.LAST_MODIFIED_BY);
	}
	*/

    public Node()
    {
    }

    protected void mapProperties(Map<QName, Serializable> nodeProps, NamespaceService namespaceService)
    {
        // TODO review backwards compat' for favorites & others (eg. set guid explicitly where still needed)
        //this.guid = nodeRef;

    	this.name = (String)nodeProps.get(ContentModel.PROP_NAME);
		this.title = (String)nodeProps.get(ContentModel.PROP_TITLE);
        this.description = (String)nodeProps.get(ContentModel.PROP_DESCRIPTION);

    	this.createdAt = (Date)nodeProps.get(ContentModel.PROP_CREATED);
    	this.createdBy = (String)nodeProps.get(ContentModel.PROP_CREATOR);
    	this.modifiedAt = (Date)nodeProps.get(ContentModel.PROP_MODIFIED);
    	this.modifiedBy = (String)nodeProps.get(ContentModel.PROP_MODIFIER);

        this.props = new HashMap<>(nodeProps.size());

        for (Map.Entry<QName, Serializable> entry : nodeProps.entrySet()) {
            QName propQName = entry.getKey();
            if (! EXCLUDED_PROPS.contains(propQName)) {
                props.put(entry.getKey().toPrefixString(namespaceService), entry.getValue());
            }
        }
    }
    
    public void setGuid(NodeRef guid)
	{
		this.guid = guid;
	}

	public NodeRef getGuid()
	{
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
//    	if(nodeRef == null)
//    	{
//    		throw new IllegalArgumentException();
//    	}
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
    
	public String getPrimaryPath()
	{
		return primaryPath;
	}
	
	public void setPrimaryPath(String primaryPath)
    {	
		this.primaryPath = primaryPath;
    }
	
	public String getType()
	{
		return prefixTypeQName;
	}
	
	public void setType(String prefixType)
    {	
		this.prefixTypeQName = prefixType;
    }

	public Map getProperties() {
        return this.props;
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
				+ createdAt + ", modifiedAt=" + modifiedAt + ", createdBy=" + createdBy + ", modifiedBy="
				+ modifiedBy + ", primaryPath =" + primaryPath +"]";
	}
}
