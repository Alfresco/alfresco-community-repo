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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.template.LuceneSearchResultsMap;
import org.alfresco.repo.template.NamePathResultsMap;
import org.alfresco.repo.template.NodeSearchResultsMap;
import org.alfresco.repo.template.SavedSearchResultsMap;
import org.alfresco.repo.template.XPathResultsMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
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
 * 
 * @author Kevin Roast
 */
public final class TemplateNode implements Serializable
{
    private static final long serialVersionUID = 1234390333739034171L;
    
    private static Log logger = LogFactory.getLog(TemplateNode.class);
    
    private final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;
    private final static String CONTENT_DEFAULT_URL = "/download/direct/{0}/{1}/{2}/{3}";
    private final static String CONTENT_PROP_URL    = "/download/direct/{0}/{1}/{2}/{3}?property={4}";
    private final static String FOLDER_BROWSE_URL   = "/navigate/browse/{0}/{1}/{2}";
    
    /** The children of this node */
    private List<TemplateNode> children = null;
    
    /** The associations from this node */
    private Map<String, List<TemplateNode>> assocs = null;
    
    /** Cached values */
    private NodeRef nodeRef;
    private String name;
    private QName type;
    private String path;
    private String id;
    private Set<QName> aspects = null;
    private QNameMap<String, Object> properties;
    private List<String> permissions = null;
    private boolean propsRetrieved = false;
    private ServiceRegistry services = null;
    private Boolean isDocument = null;
    private Boolean isContainer = null;
    private String displayPath = null;
    private String mimetype = null;
    private Long size = null;
    private TemplateImageResolver imageResolver = null;
    private TemplateNode parent = null;
    private ChildAssociationRef primaryParentAssoc = null;
    
    
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
        
        this.properties = new QNameMap<String, Object>(this.services.getNamespaceService());
    }
    
    
    // ------------------------------------------------------------------------------
    // Node API
    
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
    
    /**
     * @return The children of this Node as TemplateNode wrappers
     */
    public List<TemplateNode> getChildren()
    {
        if (this.children == null)
        {
            List<ChildAssociationRef> childRefs = this.services.getNodeService().getChildAssocs(this.nodeRef);
            this.children = new ArrayList<TemplateNode>(childRefs.size());
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
     * @return The associations for this Node. As a Map of assoc name to a List of TemplateNodes. 
     */
    public Map<String, List<TemplateNode>> getAssocs()
    {
        if (this.assocs == null)
        {
            List<AssociationRef> refs = this.services.getNodeService().getTargetAssocs(this.nodeRef, RegexQNamePattern.MATCH_ALL);
            this.assocs = new QNameMap<String, List<TemplateNode>>(this.services.getNamespaceService());
            for (AssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                List<TemplateNode> nodes = assocs.get(qname);
                if (nodes == null)
                {
                    // first access for the list for this qname
                    nodes = new ArrayList<TemplateNode>(4);
                    this.assocs.put(ref.getTypeQName().toString(), nodes);
                }
                nodes.add( new TemplateNode(ref.getTargetRef(), this.services, this.imageResolver) );
            }
        }
        
        return this.assocs;
    }
    
    /**
     * @return All the properties known about this node.
     */
    public Map<String, Object> getProperties()
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
     * @return true if this Node is a container (i.e. a folder)
     */
    public boolean getIsContainer()
    {
        if (isContainer == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isContainer = Boolean.valueOf( (dd.isSubClass(getType(), ContentModel.TYPE_FOLDER) == true && 
                    dd.isSubClass(getType(), ContentModel.TYPE_SYSTEM_FOLDER) == false) );
        }
        
        return isContainer.booleanValue();
    }
    
    /**
     * @return true if this Node is a Document (i.e. with content)
     */
    public boolean getIsDocument()
    {
        if (isDocument == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isDocument = Boolean.valueOf(dd.isSubClass(getType(), ContentModel.TYPE_CONTENT));
        }
        
        return isDocument.booleanValue();
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
    
    
    // ------------------------------------------------------------------------------
    // Content API 
    
    /**
     * @return the content String for this node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getContent()
    {
        ContentService contentService = this.services.getContentService();
        ContentReader reader = contentService.getReader(this.nodeRef, ContentModel.PROP_CONTENT);
        return (reader != null && reader.exists()) ? reader.getContentString() : "";
    }
    
    /**
     * @return For a content document, this method returns the URL to the content stream for
     *         the default content property (@see ContentModel.PROP_CONTENT)
     *         <p>
     *         For a container node, this method return the URL to browse to the folder in the web-client
     */
    public String getUrl()
    {
        if (getIsDocument() == true)
        {
           try
           {
               return MessageFormat.format(CONTENT_DEFAULT_URL, new Object[] {
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
        else
        {
           return MessageFormat.format(FOLDER_BROWSE_URL, new Object[] {
                       nodeRef.getStoreRef().getProtocol(),
                       nodeRef.getStoreRef().getIdentifier(),
                       nodeRef.getId() } );
        }
    }
    
    /**
     * @return The mimetype encoding for content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getMimetype()
    {
        if (mimetype == null)
        {
            TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
            if (content != null)
            {
                mimetype = content.getMimetype();
            }
        }
        
        return mimetype;
    }
    
    /**
     * @return The size in bytes of the content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public long getSize()
    {
        if (size == null)
        {
            TemplateContentData content = (TemplateContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
            if (content != null)
            {
                size = content.getSize();
            }
        }
        
        return size != null ? size.longValue() : 0L;
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
     * @return Display path to this node
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
                return "/images/icons/space_small.gif";
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
    // Security API 
    
    /**
     * @return List of permissions applied to this Node.
     *         Strings returned are of the format [ALLOWED|DENIED];[USERNAME|GROUPNAME];PERMISSION for example
     *         ALLOWED;kevinr;Consumer so can be easily tokenized on the ';' character.
     */
    public List<String> getPermissions()
    {
        if (this.permissions == null)
        {
            String userName = this.services.getAuthenticationService().getCurrentUserName();
            this.permissions = new ArrayList<String>(4);
            Set<AccessPermission> acls = this.services.getPermissionService().getAllSetPermissions(this.nodeRef);
            for (AccessPermission permission : acls)
            {
                StringBuilder buf = new StringBuilder(64);
                buf.append(permission.getAccessStatus())
                .append(';')
                .append(permission.getAuthority())
                .append(';')
                .append(permission.getPermission());
                this.permissions.add(buf.toString());
            }
        }
        return this.permissions;
    }
    
    /**
     * @return true if this node inherits permissions from its parent node, false otherwise.
     */
    public boolean getInheritsPermissions()
    {
        return this.services.getPermissionService().getInheritParentPermissions(this.nodeRef);
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
    // Misc helpers 
    
    /**
     * @return the image resolver instance used by this node
     */
    public TemplateImageResolver getImageResolver()
    {
        return this.imageResolver;
    }
    
    /**
     * Override Object.toString() to provide useful debug output
     */
    public String toString()
    {
        if (this.services.getNodeService().exists(nodeRef))
        {
            return "Node Type: " + getType() + 
                   "\tNode Ref: " + this.nodeRef.toString();
        }
        else
        {
            return "Node no longer exists: " + nodeRef;
        }
    }
    
    
    // ------------------------------------------------------------------------------
    // Inner classes 
    
    /**
     * Inner class wrapping and providing access to a ContentData property 
     */
    public class TemplateContentData implements Serializable
    {
       /**
        * Constructor
        * 
        * @param contentData  The ContentData object this object wraps 
        * @param property     The property the ContentData is attached too
        */
        public TemplateContentData(ContentData contentData, QName property)
        {
            this.contentData = contentData;
            this.property = property;
        }
        
        /**
         * @return the content stream
         */
        public String getContent()
        {
            ContentService contentService = services.getContentService();
            ContentReader reader = contentService.getReader(nodeRef, property);
            
            return (reader != null && reader.exists()) ? reader.getContentString() : "";
        }
        
        /**
         * @return 
         */
        public String getUrl()
        {
            try
            {
                return MessageFormat.format(CONTENT_PROP_URL, new Object[] {
                       nodeRef.getStoreRef().getProtocol(),
                       nodeRef.getStoreRef().getIdentifier(),
                       nodeRef.getId(),
                       StringUtils.replace(URLEncoder.encode(getName(), "UTF-8"), "+", "%20"),
                       StringUtils.replace(URLEncoder.encode(property.toString(), "UTF-8"), "+", "%20") } );
            }
            catch (UnsupportedEncodingException err)
            {
                throw new TemplateException("Failed to encode content URL for node: " + nodeRef, err);
            }
        }
        
        public long getSize()
        {
            return contentData.getSize();
        }
        
        public String getMimetype()
        {
            return contentData.getMimetype();
        }
        
        private ContentData contentData;
        private QName property;
    }
}