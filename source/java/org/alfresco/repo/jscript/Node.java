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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

/**
 * Node class implementation, specific for use by ScriptService as part of the object model.
 * <p>
 * The class exposes Node properties, children and assocs as dynamically populated maps and lists.
 * The various collection classes are mirrored as JavaScript properties. So can be accessed using
 * standard JavaScript property syntax, such as <code>node.children[0].properties.name</code>.
 * <p>
 * Various helper methods are provided to access common and useful node variables such
 * as the content url and type information. 
 * 
 * @author Kevin Roast
 */
public final class Node implements Serializable
{
    private static Log logger = LogFactory.getLog(Node.class);
    
    private final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;
    private final static String CONTENT_DEFAULT_URL = "/download/direct/{0}/{1}/{2}/{3}";
    private final static String CONTENT_PROP_URL    = "/download/direct/{0}/{1}/{2}/{3}?property={4}";
    private final static String FOLDER_BROWSE_URL   = "/navigate/browse/{0}/{1}/{2}";
    
    /** The children of this node */
    private Node[] children = null;
    
    /** The associations from this node */
    private ScriptableQNameMap<String, Node[]> assocs = null;
    
    /** Cached values */
    private NodeRef nodeRef;
    private String name;
    private QName type;
    private String path;
    private String id;
    private Set<QName> aspects = null;
    private ScriptableQNameMap<String, Object> properties;
    private boolean propsRetrieved = false;
    private ServiceRegistry services = null;
    private Boolean isDocument = null;
    private Boolean isContainer = null;
    private String displayPath = null;
    private String mimetype = null;
    private Long size = null;
    private TemplateImageResolver imageResolver = null;
    private Node parent = null;
    private ChildAssociationRef primaryParentAssoc = null;
    
    
    /**
     * Constructor
     * 
     * @param nodeRef       The NodeRef this Node wrapper represents
     * @param services      The ServiceRegistry the Node can use to access services
     * @param resolver      Image resolver to use to retrieve icons
     */
    public Node(NodeRef nodeRef, ServiceRegistry services, TemplateImageResolver resolver)
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
    }
    
    /**
     * @return The GUID for the node
     */
    public String getId()
    {
        return this.id;
    }
    
    public String jsGet_id()
    {
        return getId();
    }
    
    /**
     * @return Returns the NodeRef this Node object represents
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }
    
    public NodeRef jsGet_nodeRef()
    {
        return getNodeRef();
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
    
    public QName jsGet_type()
    {
        return getType();
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
    
    public String jsGet_name()
    {
        return getName();
    }
    
    public void jsSet_name(String name)
    {
        this.getProperties().put(ContentModel.PROP_NAME.toString(), name);
    }
    
    /**
     * @return The children of this Node as Node wrappers
     */
    public Node[] getChildren()
    {
        if (this.children == null)
        {
            List<ChildAssociationRef> childRefs = this.services.getNodeService().getChildAssocs(this.nodeRef);
            this.children = new Node[childRefs.size()];
            for (int i=0; i<childRefs.size(); i++)
            {
                // create our Node representation from the NodeRef
                Node child = new Node(childRefs.get(i).getChildRef(), this.services, this.imageResolver);
                this.children[i] = child;
            }
        }
        
        return this.children;
    }
    
    public Node[] jsGet_children()
    {
        return getChildren();
    }
    
    /**
     * @return Returns the Node at the specified 'cm:name' based Path walking the children of this Node.
     *         So a valid call might be <code>mynode.childByNamePath("/QA/Testing/Docs");</code>
     */
    public Node childByNamePath(String path)
    {
        // convert the name based path to a valid XPath query 
        StringBuilder xpath = new StringBuilder(path.length() << 1);
        for (StringTokenizer t = new StringTokenizer(path, "/"); t.hasMoreTokens(); /**/)
        {
            if (xpath.length() != 0)
            {
                xpath.append('/');
            }
            xpath.append("*[@cm:name='")
                 .append(t.nextToken())   // TODO: use QueryParameterDefinition see FileFolderService.search()
                 .append("']");
        }
        
        Node[] nodes = getChildrenByXPath(xpath.toString(), true);
        
        return (nodes.length != 0) ? nodes[0] : null;
    }
    
    // TODO: find out why this doesn't work - the function defs do not seem to get found
    //public Node jsFunction_childByNamePath(String path)
    //{
    //    return getChildByNamePath(path);
    //}
    
    /**
     * @return Returns the Nodes at the specified XPath walking the children of this Node.
     *         So a valid call might be <code>mynode.childrenByXPath("/*[@cm:name='Testing']/*");</code>
     */
    public Node[] childrenByXPath(String xpath)
    {
        return getChildrenByXPath(xpath, false);
    }
    
    // TODO: find out why this doesn't work - the function defs do not seem to get found
    //public Node[] jsFunction_childrenByXPath(String xpath)
    //{
    //    return childrenByXPath(xpath);
    //}
    
    /**
     * Return the associations for this Node. As a Map of assoc name to an Array of Nodes.
     * 
     * The Map returned implements the Scriptable interface to allow access to the assoc arrays via
     * JavaScript associative array access. This means associations of this node can be access thus:
     * <code>node.getAssocs()["translations"][0]</code>
     * 
     * @return associations as a Map of assoc name to an Array of Nodes.
     */
    public Map<String, Node[]> getAssocs()
    {
        if (this.assocs == null)
        {
            // this Map implements the Scriptable interface for native JS syntax property access
            this.assocs = new ScriptableQNameMap<String, Node[]>(this.services.getNamespaceService());
            
            List<AssociationRef> refs = this.services.getNodeService().getTargetAssocs(this.nodeRef, RegexQNamePattern.MATCH_ALL);
            for (AssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                Node[] nodes = (Node[])assocs.get(qname);
                if (nodes == null)
                {
                    // first access for the list for this qname
                    nodes = new Node[1];
                }
                else
                {
                    Node[] newNodes = new Node[nodes.length + 1];
                    System.arraycopy(nodes, 0, newNodes, 0, nodes.length);
                    nodes = newNodes;
                }
                nodes[nodes.length] = new Node(ref.getTargetRef(), this.services, this.imageResolver);
                
                this.assocs.put(ref.getTypeQName().toString(), nodes);
            }
        }
        
        return this.assocs;
    }
    
    public Map<String, Node[]> jsGet_assocs()
    {
        return getAssocs();
    }
    
    /**
     * Return all the properties known about this node.
     * 
     * The Map returned implements the Scriptable interface to allow access to the properties via
     * JavaScript associative array access. This means properties of a node can be access thus:
     * <code>node.getProperties()["name"]</code>
     * 
     * @return Map of properties for this Node.
     */
    public Map<String, Object> getProperties()
    {
        if (this.propsRetrieved == false)
        {
            // this Map implements the Scriptable interface for native JS syntax property access
            this.properties = new ScriptableQNameMap<String, Object>(this.services.getNamespaceService());
            
            Map<QName, Serializable> props = this.services.getNodeService().getProperties(this.nodeRef);
            for (QName qname : props.keySet())
            {
                Serializable propValue = props.get(qname);
                if (propValue instanceof NodeRef)
                {
                    // NodeRef object properties are converted to new Node objects
                    // so they can be used as objects within a template
                    propValue = new Node(((NodeRef)propValue), this.services, this.imageResolver);
                }
                else if (propValue instanceof ContentData)
                {
                    // ContentData object properties are converted to ScriptContentData objects
                    // so the content and other properties of those objects can be accessed
                    propValue = new ScriptContentData((ContentData)propValue, qname);
                }
                this.properties.put(qname.toString(), propValue);
            }
            
            this.propsRetrieved = true;
        }
        
        return this.properties;
    }
    
    public Map<String, Object> jsGet_properties()
    {
        return getProperties();
    }
    
    /**
     * @return true if this Node is a container (i.e. a folder)
     */
    public boolean isContainer()
    {
        if (isContainer == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isContainer = Boolean.valueOf( (dd.isSubClass(getType(), ContentModel.TYPE_FOLDER) == true && 
                    dd.isSubClass(getType(), ContentModel.TYPE_SYSTEM_FOLDER) == false) );
        }
        
        return isContainer.booleanValue();
    }
    
    public boolean jsGet_isContainer()
    {
        return isContainer();
    }
    
    /**
     * @return true if this Node is a Document (i.e. with content)
     */
    public boolean isDocument()
    {
        if (isDocument == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isDocument = Boolean.valueOf(dd.isSubClass(getType(), ContentModel.TYPE_CONTENT));
        }
        
        return isDocument.booleanValue();
    }
    
    public boolean jsGet_isDocument()
    {
        return isDocument();
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
    
    public QName[] jsGet_aspects()
    {
        return getAspects().toArray(new QName[getAspects().size()]);
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
    
    public String jsGet_displayPath()
    {
        return getDisplayPath();
    }
    
    /**
     * @return the small icon image for this node
     */
    public String getIcon16()
    {
        if (this.imageResolver != null)
        {
            if (isDocument())
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
    
    public String jsGet_icon16()
    {
        return getIcon16();
    }
    
    /**
     * @return the large icon image for this node
     */
    public String getIcon32()
    {
        if (this.imageResolver != null)
        {
            if (isDocument())
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
    
    public String jsGet_icon32()
    {
        return getIcon32();
    }
    
    /**
     * @return true if the node is currently locked
     */
    public boolean isLocked()
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
    
    public boolean jsGet_isLocked()
    {
        return isLocked();
    }
    
    /**
     * @return the parent node
     */
    public Node getParent()
    {
        if (parent == null)
        {
            NodeRef parentRef = this.services.getNodeService().getPrimaryParent(nodeRef).getParentRef();
            // handle root node (no parent!)
            if (parentRef != null)
            {
                parent = new Node(parentRef, this.services, this.imageResolver);
            }
        }
        
        return parent;
    }
    
    public Node jsGet_parent()
    {
        return getParent();
    }
    
    /**
     * 
     * @return the primary parent association so we can get at the association QName and the association type QName.
     */
    public ChildAssociationRef getPrimaryParentAssoc()
    {
        if (primaryParentAssoc == null)
        {
            primaryParentAssoc = this.services.getNodeService().getPrimaryParent(nodeRef);
        }
        return primaryParentAssoc;
    }
    
    public ChildAssociationRef jsGet_primaryParentAssoc()
    {
        return getPrimaryParentAssoc();
    }
    
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
    
    public String jsGet_content()
    {
        return getContent();
    }
    
    /**
     * @return For a content document, this method returns the URL to the content stream for
     *         the default content property (@see ContentModel.PROP_CONTENT)
     *         <p>
     *         For a container node, this method return the URL to browse to the folder in the web-client
     */
    public String getUrl()
    {
        if (isDocument() == true)
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
               throw new AlfrescoRuntimeException("Failed to encode content URL for node: " + nodeRef, err);
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
    
    public String jsGet_url()
    {
        return getUrl();
    }
    
    /**
     * @return The mimetype encoding for content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getMimetype()
    {
        if (mimetype == null)
        {
            ScriptContentData content = (ScriptContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
            if (content != null)
            {
                mimetype = content.getMimetype();
            }
        }
        
        return mimetype;
    }
    
    public String jsGet_mimetype()
    {
        return getMimetype();
    }
    
    /**
     * @return The size in bytes of the content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public long getSize()
    {
        if (size == null)
        {
            ScriptContentData content = (ScriptContentData)this.getProperties().get(ContentModel.PROP_CONTENT);
            if (content != null)
            {
                size = content.getSize();
            }
        }
        
        return size != null ? size.longValue() : 0L;
    }
    
    public long jsGet_size()
    {
        return getSize();
    }
    
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
                   "\nNode Properties: " + this.getProperties().toString() + 
                   "\nNode Aspects: " + this.getAspects().toString();
        }
        else
        {
            return "Node no longer exists: " + nodeRef;
        }
    }
    
    /**
     * Return a list or a single Node from executing an xpath against the parent Node.
     * 
     * @param xpath        XPath to execute
     * @param firstOnly    True to return the first result only
     * 
     * @return Node[] can be empty but never null
     */
    private Node[] getChildrenByXPath(String xpath, boolean firstOnly)
    {
        Node[] result = null;
        
        if (xpath.length() != 0)
        {
            if (logger.isDebugEnabled())
                logger.debug("Executing xpath: " + xpath);
            
            NodeRef contextRef;
            if (getParent() != null)
            {
                contextRef = getParent().getNodeRef();
            }
            else
            {
                contextRef = this.services.getNodeService().getRootNode(nodeRef.getStoreRef());
            }
            List<NodeRef> nodes = this.services.getSearchService().selectNodes(
                    contextRef,
                    xpath,
                    null,
                    this.services.getNamespaceService(),
                    false);
            
            // see if we only want the first result
            if (firstOnly == true)
            {
                if (nodes.size() != 0)
                {
                    result = new Node[1];
                    result[0] = new Node(nodes.get(0), this.services, this.imageResolver);
                }
            }
            // or all the results
            else
            {
                result = new Node[nodes.size()];
                for (int i=0; i<nodes.size(); i++)
                {
                    NodeRef ref = nodes.get(i);
                    result[i] = new Node(ref, this.services, this.imageResolver);
                }
            }
        }
        
        return result != null ? result : new Node[0];
    }
    
    
    /**
     * Inner class wrapping and providing access to a ContentData property 
     */
    public class ScriptContentData implements Serializable
    {
       /**
        * Constructor
        * 
        * @param contentData  The ContentData object this object wraps 
        * @param property     The property the ContentData is attached too
        */
        public ScriptContentData(ContentData contentData, QName property)
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
                throw new AlfrescoRuntimeException("Failed to encode content URL for node: " + nodeRef, err);
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