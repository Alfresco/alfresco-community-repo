/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.TemplateNode.TemplateContentData;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;
import org.springframework.util.StringUtils;

/**
 * Template Node wrapper representing a record in the version history of a node.
 * Provides access to basic properties and version information for the frozen state record. 
 * 
 * @author Kevin Roast
 */
public class VersionHistoryNode implements Serializable
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
    public VersionHistoryNode(Version version, TemplateNode parent)
    {
        if (version == null)
        {
            throw new IllegalArgumentException("Version history descriptor is mandatory.");
        }
        if (parent == null)
        {
            throw new IllegalArgumentException("Parent TemplateNode is mandatory.");
        }
        this.version = version;
        this.parent = parent;
        this.properties = new QNameMap<String, Serializable>(parent.services.getNamespaceService());
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
                Serializable propValue = props.get(qname);
                if (propValue instanceof NodeRef)
                {
                    // NodeRef object properties are converted to new TemplateNode objects
                    // so they can be used as objects within a template
                    propValue = new TemplateNode(((NodeRef)propValue), parent.services, parent.imageResolver);
                }
                else if (propValue instanceof ContentData)
                {
                    // ContentData object properties are converted to TemplateContentData objects
                    // so the content and other properties of those objects can be accessed
                    propValue = parent.new TemplateContentData((ContentData)propValue, qname);
                }
                this.properties.put(qname.toString(), propValue);
            }
            
            propsRetrieved = true;
        }
        
        return this.properties;
    }
    
    /**
     * @return The list of aspects applied to this node
     */
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
    
    
    // ------------------------------------------------------------------------------
    // Content API 
    
    /**
     * @return the content String for this node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getContent()
    {
        TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
        return content != null ? content.getContent() : "";
    }
    
    /**
     * @return For a content document, this method returns the URL to the content stream for
     *         the default content property (@see ContentModel.PROP_CONTENT)
     *         <p>
     *         For a container node, this method return the URL to browse to the folder in the web-client
     */
    public String getUrl()
    {
        NodeRef nodeRef = this.version.getFrozenStateNodeRef();
        try
        {
            return MessageFormat.format(parent.CONTENT_DEFAULT_URL, new Object[] {
                    nodeRef.getStoreRef().getProtocol(),
                    nodeRef.getStoreRef().getIdentifier(),
                    nodeRef.getId(),
                    StringUtils.replace(URLEncoder.encode(getName(), "UTF-8"), "+", "%20") } );
        }
        catch (UnsupportedEncodingException err)
        {
            throw new TemplateException("Failed to encode content URL for node: " + nodeRef, err);
        }
    }
    
    /**
     * @return The mimetype encoding for content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getMimetype()
    {
        TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
        return (content != null ? content.getMimetype() : null);
    }
    
    /**
     * @return The size in bytes of the content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public long getSize()
    {
        TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
        return (content != null ? content.getSize() : 0L);
    }
}
