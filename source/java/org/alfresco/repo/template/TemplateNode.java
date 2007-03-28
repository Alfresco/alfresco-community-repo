/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.template;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.audit.AuditInfo;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import freemarker.ext.dom.NodeModel;

/**
 * Node class specific for use by Template pages that support Bean objects as part of the model.
 * The default template engine FreeMarker can use these objects and they are provided to support it.
 * A single method is completely freemarker specific - getXmlNodeModel()
 * <p>
 * The class exposes Node properties, children as dynamically populated maps and lists.
 * <p>
 * Various helper methods are provided to access common and useful node variables such
 * as the content url and type information. 
 * <p>
 * See {@link http://wiki.alfresco.com/wiki/Template_Guide}
 * 
 * @author Kevin Roast
 */
public class TemplateNode extends BasePermissionsNode
{
    private static final long serialVersionUID = 1234390333739034171L;
    
    private static Log logger = LogFactory.getLog(TemplateNode.class);
    
    protected final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;
    
    /** The children of this node */
    private List<TemplateProperties> children = null;
    
    /** The target associations from this node */
    private Map<String, List<TemplateNode>> targetAssocs = null;
    
    /** The child associations from this node */
    private Map<String, List<TemplateNode>> childAssocs = null;
    
    /** All associations from this node */
    private Map<String, List<TemplateNode>> assocs = null;
    
    /** Cached values */
    protected NodeRef nodeRef;
    private String name;
    private QName type;
    private String path;
    private String id;
    private Set<QName> aspects = null;
    private QNameMap<String, Serializable> properties;
    private boolean propsRetrieved = false;
    private String displayPath = null;
    protected TemplateImageResolver imageResolver = null;
    private TemplateNode parent = null;
    private ChildAssociationRef primaryParentAssoc = null;
    private Boolean isCategory = null;
    
    
    // ------------------------------------------------------------------------------
    // Construction 
    
    /**
     * Constructor
     * 
     * @param nodeRef       The NodeRef this Node wrapper represents
     * @param services      The ServiceRegistry the TemplateNode can use to access services
     * @param resolver      Image resolver to use to retrieve icons
     */
    public TemplateNode(NodeRef nodeRef, ServiceRegistry services, TemplateImageResolver resolver)
    {
        if (nodeRef == null)
        {
            throw new IllegalArgumentException("NodeRef must be supplied.");
        }
      
        if (services == null)
        {
            throw new IllegalArgumentException("The ServiceRegistry must be supplied.");
        }
        
        this.nodeRef = nodeRef;
        this.id = nodeRef.getId();
        this.services = services;
        this.imageResolver = resolver;
        
        this.properties = new QNameMap<String, Serializable>(this.services.getNamespaceService());
    }
    
    
    // ------------------------------------------------------------------------------
    // TemplateNodeRef contract implementation
    
    /**
     * @return The GUID for the node
     */
    public String getId()
    {
        return this.id;
    }
    
    /**
     * @return Returns the NodeRef this Node object represents
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }
    
    /**
     * @return Returns the type.
     */
    public QName getType()
    {
        if (this.type == null)
        {
            this.type = this.services.getNodeService().getType(this.nodeRef);
        }
        
        return type;
    }
    
    /**
     * @return The display name for the node
     */
    public String getName()
    {
        if (this.name == null)
        {
            // try and get the name from the properties first
            this.name = (String)getProperties().get("cm:name");
            
            // if we didn't find it as a property get the name from the association name
            if (this.name == null)
            {
                ChildAssociationRef parentRef = this.services.getNodeService().getPrimaryParent(this.nodeRef);
                if (parentRef != null && parentRef.getQName() != null)
                {
                    this.name = parentRef.getQName().getLocalName();
                }
                else
                {
                    this.name = "";
                }
            }
        }
        
        return this.name;
    }
    
    
    // ------------------------------------------------------------------------------
    // TemplateProperties contract implementation
    
    /**
     * @return The children of this Node as objects that support the TemplateProperties contract.
     */
    public List<TemplateProperties> getChildren()
    {
        if (this.children == null)
        {
            List<ChildAssociationRef> childRefs = this.services.getNodeService().getChildAssocs(this.nodeRef);
            this.children = new ArrayList<TemplateProperties>(childRefs.size());
            for (ChildAssociationRef ref : childRefs)
            {
                // create our Node representation from the NodeRef
                TemplateNode child = new TemplateNode(ref.getChildRef(), this.services, this.imageResolver);
                this.children.add(child);
            }
        }
        
        return this.children;
    }
    
    /**
     * @return All the properties known about this node.
     */
    public Map<String, Serializable> getProperties()
    {
        if (this.propsRetrieved == false)
        {
            Map<QName, Serializable> props = this.services.getNodeService().getProperties(this.nodeRef);
            
            for (QName qname : props.keySet())
            {
                Serializable propValue = props.get(qname);
                if (propValue instanceof NodeRef)
                {
                    // NodeRef object properties are converted to new TemplateNode objects
                    // so they can be used as objects within a template
                    propValue = new TemplateNode(((NodeRef)propValue), this.services, this.imageResolver);
                }
                else if (propValue instanceof ContentData)
                {
                    // ContentData object properties are converted to TemplateContentData objects
                    // so the content and other properties of those objects can be accessed
                    propValue = new TemplateContentData((ContentData)propValue, qname);
                }
                this.properties.put(qname.toString(), propValue);
            }
            
            this.propsRetrieved = true;
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
            this.aspects = this.services.getNodeService().getAspects(this.nodeRef);
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
            this.aspects = this.services.getNodeService().getAspects(this.nodeRef);
        }
        
        if (aspect.startsWith(NAMESPACE_BEGIN))
        {
            return aspects.contains((QName.createQName(aspect)));
        }
        else
        {
            boolean found = false;
            for (QName qname : this.aspects)
            {
                if (qname.toPrefixString(this.services.getNamespaceService()).equals(aspect))
                {
                    found = true;
                    break;
                }
            }
            return found;
        }
    }
    
    
    // ------------------------------------------------------------------------------
    // Repository Node API
    
    /**
     * @return The child associations for this Node. As a Map of assoc name to a List of TemplateNodes. 
     */
    public Map<String, List<TemplateNode>> getChildAssocs()
    {
        if (this.childAssocs == null)
        {
            List<ChildAssociationRef> refs = this.services.getNodeService().getChildAssocs(this.nodeRef);
            this.childAssocs = new QNameMap<String, List<TemplateNode>>(this.services.getNamespaceService());
            for (ChildAssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                List<TemplateNode> nodes = this.childAssocs.get(qname);
                if (nodes == null)
                {
                    // first access for the list for this qname
                    nodes = new ArrayList<TemplateNode>(4);
                    this.childAssocs.put(ref.getTypeQName().toString(), nodes);
                }
                nodes.add( new TemplateNode(ref.getChildRef(), this.services, this.imageResolver) );
            }
        }
        
        return this.childAssocs;
    }
    
    /**
     * @return The target associations for this Node. As a Map of assoc name to a List of TemplateNodes. 
     */
    public Map<String, List<TemplateNode>> getTargetAssocs()
    {
        if (this.targetAssocs == null)
        {
            List<AssociationRef> refs = this.services.getNodeService().getTargetAssocs(this.nodeRef, RegexQNamePattern.MATCH_ALL);
            this.targetAssocs = new QNameMap<String, List<TemplateNode>>(this.services.getNamespaceService());
            for (AssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                List<TemplateNode> nodes = this.targetAssocs.get(qname);
                if (nodes == null)
                {
                    // first access for the list for this qname
                    nodes = new ArrayList<TemplateNode>(4);
                    this.targetAssocs.put(ref.getTypeQName().toString(), nodes);
                }
                nodes.add( new TemplateNode(ref.getTargetRef(), this.services, this.imageResolver) );
            }
        }
        
        return this.targetAssocs;
    }
    
    /**
     * @return All associations for this Node. As a Map of assoc name to a List of TemplateNodes. 
     */
    public Map<String, List<TemplateNode>> getAssocs()
    {
        if (this.assocs == null)
        {
            this.assocs = new QNameMap<String, List<TemplateNode>>(this.services.getNamespaceService());
            this.assocs.putAll(getTargetAssocs());
            this.assocs.putAll(getChildAssocs());
        }
        return this.assocs;
    }
    
    /**
     * @return true if the node is currently locked
     */
    public boolean getIsLocked()
    {
        boolean locked = false;
        
        if (getAspects().contains(ContentModel.ASPECT_LOCKABLE))
        {
            LockStatus lockStatus = this.services.getLockService().getLockStatus(this.nodeRef);
            if (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER)
            {
                locked = true;
            }
        }
        
        return locked;
    }
    
    /**
     * @return true if the node is a Category instance
     */
    public boolean getIsCategory()
    {
        if (isCategory == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isCategory = Boolean.valueOf(dd.isSubClass(getType(), ContentModel.TYPE_CATEGORY));
        }

        return isCategory.booleanValue();
    }
    
    /**
     * @return the parent node
     */
    public TemplateNode getParent()
    {
        if (parent == null)
        {
            NodeRef parentRef = this.services.getNodeService().getPrimaryParent(nodeRef).getParentRef();
            // handle root node (no parent!)
            if (parentRef != null)
            {
                parent = new TemplateNode(parentRef, this.services, this.imageResolver);
            }
        }
        
        return parent;
    }
    
    /**
     * @return the primary parent association so we can access the association QName and association type QName.
     */
    public ChildAssociationRef getPrimaryParentAssoc()
    {
        if (primaryParentAssoc == null)
        {
            primaryParentAssoc = this.services.getNodeService().getPrimaryParent(nodeRef);
        }
        return primaryParentAssoc;
    }
    
    /**
     * @return a list of objects representing the version history of this node.
     *         @see VersionHistoryNode
     */
    public List<VersionHistoryNode> getVersionHistory()
    {
        List<VersionHistoryNode> records = Collections.<VersionHistoryNode>emptyList();
        
        if (this.getAspects().contains(ContentModel.ASPECT_VERSIONABLE))
        {
            VersionHistory history = this.services.getVersionService().getVersionHistory(this.nodeRef);
            if (history != null)
            {
                records = new ArrayList<VersionHistoryNode>(8);
                for (Version version : history.getAllVersions())
                {
                    // create a wrapper for the version information
                    VersionHistoryNode record = new VersionHistoryNode(version, this, this.services);
                    
                    // add the client side version to the list
                    records.add(record);
                }
            }
        }
        
        return records;
    }
    
    
    // ------------------------------------------------------------------------------
    // Node Helper API 
    
    /**
     * @return FreeMarker NodeModel for the XML content of this node, or null if no parsable XML found
     */
    public NodeModel getXmlNodeModel()
    {
        try
        {
            return NodeModel.parse(new InputSource(new StringReader(getContent())));
        }
        catch (Throwable err)
        {
            if (logger.isDebugEnabled())
                logger.debug(err.getMessage(), err);
            
            return null;
        }
    }
    
    /**
     * @return Display path to this node - the path built of 'cm:name' attribute values.
     */
    public String getDisplayPath()
    {
        if (displayPath == null)
        {
            try
            {
                displayPath = this.services.getNodeService().getPath(this.nodeRef).toDisplayPath(this.services.getNodeService());
            }
            catch (AccessDeniedException err)
            {
                displayPath = "";
            }
        }
        
        return displayPath;
    }
    
    /**
     * @return QName path to this node. This can be used for Lucene PATH: style queries
     */
    public String getQnamePath()
    {
        return this.services.getNodeService().getPath(this.nodeRef).toPrefixString(this.services.getNamespaceService());
    }
    
    /**
     * @return the small icon image for this node
     */
    public String getIcon16()
    {
        if (this.imageResolver != null)
        {
            if (getIsDocument())
            {
                return this.imageResolver.resolveImagePathForName(getName(), true);
            }
            else
            {
                String icon = (String)getProperties().get("app:icon");
                if (icon != null)
                {
                    return "/images/icons/" + icon + "-16.gif";
                }
                else
                {
                    return "/images/icons/space_small.gif";
                }
            }
        }
        else
        {
            return "/images/filetypes/_default.gif";
        }
    }
    
    /**
     * @return the large icon image for this node
     */
    public String getIcon32()
    {
        if (this.imageResolver != null)
        {
            if (getIsDocument())
            {
                return this.imageResolver.resolveImagePathForName(getName(), false);
            }
            else
            {
                String icon = (String)getProperties().get("app:icon");
                if (icon != null)
                {
                    return "/images/icons/" + icon + ".gif";
                }
                else
                {
                    return "/images/icons/space-icon-default.gif";
                }
            }
        }
        else
        {
            return "/images/filetypes32/_default.gif";
        }
    }
    
    
    // ------------------------------------------------------------------------------
    // Search API
    
    /**
     * @return A map capable of returning the TemplateNode at the specified Path as a child of this node.
     */
    public Map getChildByNamePath()
    {
        return new NamePathResultsMap(this, this.services);
    }
    
    /**
     * @return A map capable of returning a List of TemplateNode objects from an XPath query
     *         as children of this node.
     */
    public Map getChildrenByXPath()
    {
        return new XPathResultsMap(this, this.services);
    }
    
    /**
     * @return A map capable of returning a List of TemplateNode objects from an NodeRef to a Saved Search
     *         object. The Saved Search is executed and the resulting nodes supplied as a sequence.
     */
    public Map getChildrenBySavedSearch()
    {
        return new SavedSearchResultsMap(this, this.services);
    }
    
    /**
     * @return A map capable of returning a List of TemplateNode objects from an NodeRef to a Lucene search
     *         string. The Saved Search is executed and the resulting nodes supplied as a sequence.
     */
    public Map getChildrenByLuceneSearch()
    {
        return new LuceneSearchResultsMap(this, this.services);
    }
    
    /**
     * @return A map capable of returning a TemplateNode for a single specified NodeRef reference.
     */
    public Map getNodeByReference()
    {
        return new NodeSearchResultsMap(this, this.services);
    }
    
    
    // ------------------------------------------------------------------------------
    // Audit API
    
    /**
     * @return a list of AuditInfo objects describing the Audit Trail for this node instance
     */
    public List<AuditInfo> getAuditTrail()
    {
        return this.services.getAuditService().getAuditTrail(this.nodeRef);
    }
    
    
    // ------------------------------------------------------------------------------
    // Misc helpers 
    
    /**
     * @return the image resolver instance used by this node
     */
    public TemplateImageResolver getImageResolver()
    {
        return this.imageResolver;
    }
}