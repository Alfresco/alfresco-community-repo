/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.template;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * Template Node wrapper representing a record in the version history of a node.
 * Provides access to basic properties and version information for the frozen state record. 
 * 
 * @author Kevin Roast
 */
public class VersionHistoryNode extends BaseContentNode implements NamespacePrefixResolverProvider
{
    private QNameMap<String, Serializable> properties;
    private boolean propsRetrieved = false;
    private Version version;
    private TemplateNode parent;
    private Set<QName> aspects = null;
    
    /**
     * Constructor
     * 
     * @param version       Descriptor of the node version information
     */
    public VersionHistoryNode(Version version, TemplateNode parent, ServiceRegistry services)
    {
        if (version == null)
        {
            throw new IllegalArgumentException("Version history descriptor is mandatory.");
        }
        if (parent == null)
        {
            throw new IllegalArgumentException("Parent TemplateNode is mandatory.");
        }
        if (services == null)
        {
            throw new IllegalArgumentException("The ServiceRegistry must be supplied.");
        }
        this.version = version;
        this.parent = parent;
        this.services = services;
        this.properties = new QNameMap<String, Serializable>(this);
    }
    
    /**
     * @return The GUID for the frozen state NodeRef
     */
    public String getId()
    {
        return this.version.getFrozenStateNodeRef().getId();
    }
    
    /**
     * @return Returns the frozen state NodeRef this record represents
     */
    public NodeRef getNodeRef()
    {
        return this.version.getFrozenStateNodeRef();
    }
    
    /**
     * @return Returns the type.
     */
    public QName getType()
    {
        return parent.services.getNodeService().getType(this.version.getFrozenStateNodeRef());
    }
    
    /**
     * Helper method to get the item name.
     * 
     * @return  the item name
     */
    public String getName()
    {
        return (String)this.getProperties().get(ContentModel.PROP_NAME);
    }
    
    /**
     * Helper method to get the item title.
     * 
     * @return  the item name
     */
    public String getTitle()
    {
        return (String)this.getProperties().get(ContentModel.PROP_TITLE);
    }

    /**
     * Helper method to get the created date from the version property data.
     * 
     * @return  the date the version was created
     */
    public Date getCreatedDate()
    {
        return this.version.getCreatedDate();
    }
    
    /**
     * Helper method to get the creator of the version.
     * 
     * @return  the creator of the version
     */
    public String getCreator()
    {
        return this.version.getCreator();
    }

    /**
     * Helper method to get the version label from the version property data.
     * 
     * @return  the version label
     */
    public String getVersionLabel()
    {
        return this.version.getVersionLabel();
    }
    
    /**
     * Helper method to get the version type.
     * 
     * @return  true if this is a major version, false otherwise.
     */
    public boolean getIsMajorVersion()
    {
        return (this.version.getVersionType() == VersionType.MAJOR);
    }
    
    /**
     * Helper method to get the version description.
     * 
     * @return the version description
     */
    public String getDescription()
    {
        return this.version.getDescription();
    }

    /**
     * Get the map containing the version property values.
     * 
     * @return  the map containing the version properties
     */
    public Map<String, Serializable> getProperties()
    {
        if (propsRetrieved == false)
        {
            Map<QName, Serializable> props = parent.services.getNodeService().getProperties(
                    this.version.getFrozenStateNodeRef());
            
            for (QName qname : props.keySet())
            {
                Serializable propValue = parent.new TemplatePropertyConverter().convertProperty(props.get(qname), qname, parent.services, parent.imageResolver);
                this.properties.put(qname.toString(), propValue);
            }
            
            propsRetrieved = true;
        }
        
        return this.properties;
    }
    
    /**
     * @return The list of aspects applied to this node
     */
    @Override
    public Set<QName> getAspects()
    {
        if (this.aspects == null)
        {
            this.aspects = parent.services.getNodeService().getAspects(this.version.getFrozenStateNodeRef());
        }
        
        return this.aspects;
    }
    
    /**
     * @param aspect The aspect name to test for
     * 
     * @return true if the node has the aspect false otherwise
     */
    @Override
    public boolean hasAspect(String aspect)
    {
        if (this.aspects == null)
        {
            getAspects();
        }
        
        if (aspect.startsWith(parent.NAMESPACE_BEGIN))
        {
            return aspects.contains((QName.createQName(aspect)));
        }
        else
        {
            boolean found = false;
            for (QName qname : this.aspects)
            {
                if (qname.toPrefixString(parent.services.getNamespaceService()).equals(aspect))
                {
                    found = true;
                    break;
                }
            }
            return found;
        }
    }
    
    /**
     * @see org.alfresco.repo.template.TemplateProperties#getChildren()
     */
    @Override
    public List<TemplateProperties> getChildren()
    {
        return null;
    }
    
    /**
     * @see org.alfresco.repo.template.TemplateProperties#getParent()
     */
    public TemplateProperties getParent()
    {
        return null;
    }
    
    
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return this.services.getNamespaceService();
    }
    
    
    // ------------------------------------------------------------------------------
    // Content API 
    
    /**
     * @return Returns the URL to the content stream for the frozen state of the node from
     *         the default content property (@see ContentModel.PROP_CONTENT)
     */
    @Override
    public String getUrl()
    {
        NodeRef nodeRef = this.version.getFrozenStateNodeRef();
        return MessageFormat.format(parent.CONTENT_GET_URL, new Object[] {
                nodeRef.getStoreRef().getProtocol(),
                nodeRef.getStoreRef().getIdentifier(),
                nodeRef.getId(),
                URLEncoder.encode(getName()) } );
    }
}
