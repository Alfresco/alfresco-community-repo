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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.repo.content.transform.magick.ImageMagickContentTransformer;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.template.FreeMarkerProcessor;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.springframework.util.StringUtils;

/**
 * Node class implementation, specific for use by ScriptService as part of the object model.
 * <p>
 * The class exposes Node properties, children and assocs as dynamically populated maps and lists. The various collection classes are mirrored as JavaScript properties. So can be
 * accessed using standard JavaScript property syntax, such as <code>node.children[0].properties.name</code>.
 * <p>
 * Various helper methods are provided to access common and useful node variables such as the content url and type information.
 * 
 * @author Kevin Roast
 */
public class Node implements Serializable, Scopeable
{
    private static final long serialVersionUID = -3378946227712939600L;
    
    private static Log logger = LogFactory.getLog(Node.class);
    
    private final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;
    
    private final static String CONTENT_DEFAULT_URL = "/download/direct/{0}/{1}/{2}/{3}";
    private final static String CONTENT_PROP_URL = "/download/direct/{0}/{1}/{2}/{3}?property={4}";
    private final static String FOLDER_BROWSE_URL = "/navigate/browse/{0}/{1}/{2}";
    
    /** Root scope for this object */
    protected Scriptable scope;
    
    /** Node Value Converter */
    protected NodeValueConverter converter = null;
    
    /** Cached values */
    protected NodeRef nodeRef;
    private String name;
    private QName type;
    protected String id;
    
    /** The aspects applied to this node */
    private Set<QName> aspects = null;
    
    /** The associations from this node */
    private ScriptableQNameMap<String, Node[]> assocs = null;
    
    /** The children of this node */
    private Node[] children = null;
    
    /** The properties of this node */
    private ScriptableQNameMap<String, Serializable> properties = null;
    
    protected ServiceRegistry services = null;
    private NodeService nodeService = null;
    private Boolean isDocument = null;
    private Boolean isContainer = null;
    private String displayPath = null;
    protected TemplateImageResolver imageResolver = null;
    protected Node parent = null;
    private ChildAssociationRef primaryParentAssoc = null;
    // NOTE: see the reset() method when adding new cached members!
    
    
    // ------------------------------------------------------------------------------
    // Construction
    
    /**
     * Constructor
     * 
     * @param nodeRef   The NodeRef this Node wrapper represents
     * @param services  The ServiceRegistry the Node can use to access services
     * @param resolver  Image resolver to use to retrieve icons
     */
    public Node(NodeRef nodeRef, ServiceRegistry services)
    {
        this(nodeRef, services, null);
    }
    
    /**
     * Constructor
     * 
     * @param nodeRef   The NodeRef this Node wrapper represents
     * @param services  The ServiceRegistry the Node can use to access services
     * @param resolver  Image resolver to use to retrieve icons
     * @param scope     Root scope for this Node
     */
    public Node(NodeRef nodeRef, ServiceRegistry services, Scriptable scope)
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
        this.nodeService = services.getNodeService();
        this.scope = scope;
    }
    
    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Node other = (Node) obj;
        if (nodeRef == null)
        {
            if (other.nodeRef != null) return false;
        }
        else if (!nodeRef.equals(other.nodeRef)) return false;
        return true;
    }
    
    /**
     * Factory method
     */
    public Node newInstance(NodeRef nodeRef, ServiceRegistry services, Scriptable scope)
    {
        return new Node(nodeRef, services, scope);
    }
    
    /**
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }
    
    
    // ------------------------------------------------------------------------------
    // Node Wrapper API
    
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
    
    public String jsGet_nodeRef()
    {
        return getNodeRef().toString();
    }
    
    /**
     * @return Returns the type.
     */
    public QName getType()
    {
        if (this.type == null)
        {
            this.type = this.nodeService.getType(this.nodeRef);
        }
        
        return type;
    }
    
    public String jsGet_type()
    {
        return getType().toString();
    }
    
    /**
     * @return Helper to return the 'name' property for the node
     */
    public String getName()
    {
        if (this.name == null)
        {
            // try and get the name from the properties first
            this.name = (String) getProperties().get("cm:name");
            
            // if we didn't find it as a property get the name from the association name
            if (this.name == null)
            {
                ChildAssociationRef parentRef = this.nodeService.getPrimaryParent(this.nodeRef);
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
    
    /**
     * Helper to set the 'name' property for the node.
     * 
     * @param name Name to set
     */
    public void setName(String name)
    {
        if (name != null)
        {
            this.getProperties().put(ContentModel.PROP_NAME.toString(), name.toString());
        }
    }
    
    public void jsSet_name(String name)
    {
        setName(name);
    }
    
    /**
     * @return The children of this Node as Node wrappers
     */
    public Node[] getChildren()
    {
        if (this.children == null)
        {
            List<ChildAssociationRef> childRefs = this.nodeService.getChildAssocs(this.nodeRef);
            this.children = new Node[childRefs.size()];
            for (int i = 0; i < childRefs.size(); i++)
            {
                // create our Node representation from the NodeRef
                Node child = newInstance(childRefs.get(i).getChildRef(), this.services, this.scope);
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
     *         So a valid call might be:
     *         <code>mynode.childByNamePath("/QA/Testing/Docs");</code>
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
            xpath.append("*[@cm:name='").append(t.nextToken()) // TODO: use QueryParameterDefinition see FileFolderService.search()
            .append("']");
        }
        
        Node[] nodes = getChildrenByXPath(xpath.toString(), true);
        
        return (nodes.length != 0) ? nodes[0] : null;
    }
    
    // TODO: find out why this doesn't work - the function defs do not seem to get found
    // public Node jsFunction_childByNamePath(String path)
    // {
    // return getChildByNamePath(path);
    // }
    
    /**
     * @return Returns the Nodes at the specified XPath walking the children of this Node. So a valid call might be <code>mynode.childrenByXPath("*[@cm:name='Testing']/*");</code>
     */
    public Node[] childrenByXPath(String xpath)
    {
        return getChildrenByXPath(xpath, false);
    }
    
    /**
     * Return the associations for this Node. As a Map of assoc name to an Array of Nodes.
     * The Map returned implements the Scriptable interface to allow access to the assoc arrays via JavaScript
     * associative array access. This means associations of this node can be access thus:
     * <code>node.assocs["translations"][0]</code>
     * 
     * @return associations as a Map of assoc name to an Array of Nodes.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Node[]> getAssocs()
    {
        if (this.assocs == null)
        {
            // this Map implements the Scriptable interface for native JS syntax property access
            this.assocs = new ScriptableQNameMap<String, Node[]>(this.services.getNamespaceService());
            
            List<AssociationRef> refs = this.nodeService.getTargetAssocs(this.nodeRef, RegexQNamePattern.MATCH_ALL);
            for (AssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                Node[] nodes = (Node[]) this.assocs.get(qname);
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
                nodes[nodes.length - 1] = newInstance(ref.getTargetRef(), this.services, this.scope);
                
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
     * Return all the properties known about this node. The Map returned implements the Scriptable interface to
     * allow access to the properties via JavaScript associative array access. This means properties of a node can
     * be access thus: <code>node.properties["name"]</code>
     * 
     * @return Map of properties for this Node.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getProperties()
    {
        if (this.properties == null)
        {
            // this Map implements the Scriptable interface for native JS syntax property access
            this.properties = new ScriptableQNameMap<String, Serializable>(this.services.getNamespaceService());
            
            Map<QName, Serializable> props = this.nodeService.getProperties(this.nodeRef);
            for (QName qname : props.keySet())
            {
                Serializable propValue = props.get(qname);
                
                // perform the conversion to a script safe value and store
                
                this.properties.put(qname.toString(), getValueConverter().convertValueForScript(qname, propValue));
            }
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
    public boolean getIsContainer()
    {
        if (isContainer == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isContainer = Boolean.valueOf((dd.isSubClass(getType(), ContentModel.TYPE_FOLDER) == true &&
                    dd.isSubClass(getType(), ContentModel.TYPE_SYSTEM_FOLDER) == false));
        }
        
        return isContainer.booleanValue();
    }
    
    public boolean jsGet_isContainer()
    {
        return getIsContainer();
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
    
    public boolean jsGet_isDocument()
    {
        return getIsDocument();
    }
    
    /**
     * @return true if the Node is a Category
     */
    public boolean getIsCategory()
    {
        // this valid is overriden by the CategoryNode sub-class
        return false;
    }
    
    public boolean jsGet_isCategory()
    {
        return getIsCategory();
    }
    
    /**
     * @return The list of aspects applied to this node
     */
    public Set<QName> getAspects()
    {
        if (this.aspects == null)
        {
            this.aspects = this.nodeService.getAspects(this.nodeRef);
        }
        
        return this.aspects;
    }
    
    public String[] jsGet_aspects()
    {
        Set<QName> aspects = getAspects();
        String[] result = new String[aspects.size()];
        int count = 0;
        for (QName qname : aspects)
        {
            result[count++] = qname.toString();
        }
        return result;
    }
    
    /**
     * @param aspect  The aspect name to test for (full qualified or short-name form)
     * @return true if the node has the aspect false otherwise
     */
    public boolean hasAspect(String aspect)
    {
        return getAspects().contains(createQName(aspect));
    }
    
    /**
     * Return true if the user has the specified permission on the node.
     * <p>
     * The default permissions are found in <code>org.alfresco.service.cmr.security.PermissionService</code>.
     * Most commonly used are "Write", "Delete" and "AddChildren".
     * 
     * @param permission as found in <code>org.alfresco.service.cmr.security.PermissionService</code>
     * @return true if the user has the specified permission on the node.
     */
    public boolean hasPermission(String permission)
    {
        ParameterCheck.mandatory("Permission Name", permission);
        
        boolean allowed = false;
        
        if (permission != null && permission.length() != 0)
        {
            AccessStatus status = this.services.getPermissionService().hasPermission(this.nodeRef, permission);
            allowed = (AccessStatus.ALLOWED == status);
        }
        
        return allowed;
    }
    
    /**
     * @return Display path to this node
     */
    public String getDisplayPath()
    {
        if (displayPath == null)
        {
            displayPath = this.nodeService.getPath(this.nodeRef).toDisplayPath(this.nodeService);
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
        return "/images/filetypes/_default.gif";
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
        return "/images/filetypes32/_default.gif";
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
            NodeRef parentRef = getPrimaryParentAssoc().getParentRef();
            // handle root node (no parent!)
            if (parentRef != null)
            {
                parent = newInstance(parentRef, this.services, this.scope);
            }
        }
        
        return parent;
    }
    
    public Node jsGet_parent()
    {
        return getParent();
    }
    
    /**
     * @return the primary parent association so we can get at the association QName and the association type QName.
     */
    public ChildAssociationRef getPrimaryParentAssoc()
    {
        if (primaryParentAssoc == null)
        {
            primaryParentAssoc = this.nodeService.getPrimaryParent(nodeRef);
        }
        return primaryParentAssoc;
    }
    
    public ChildAssociationRef jsGet_primaryParentAssoc()
    {
        return getPrimaryParentAssoc();
    }
    
    
    // ------------------------------------------------------------------------------
    // Content API
    
    /**
     * @return the content String for this node from the default content property (@see ContentModel.PROP_CONTENT)
     */
    public String getContent()
    {
        String content = "";
        
        ScriptContentData contentData = (ScriptContentData) getProperties().get(ContentModel.PROP_CONTENT);
        if (contentData != null)
        {
            content = contentData.getContent();
        }
        
        return content;
    }
    
    public String jsGet_content()
    {
        return getContent();
    }
    
    /**
     * Set the content for this node
     * 
     * @param content    Content string to set
     */
    public void setContent(String content)
    {
        ScriptContentData contentData = (ScriptContentData) getProperties().get(ContentModel.PROP_CONTENT);
        if (contentData == null)
        {
            // guess a mimetype based on the filename
            String mimetype = this.services.getMimetypeService().guessMimetype(getName());
            ContentData cdata = new ContentData(null, mimetype, 0L, "UTF-8");
            contentData = new ScriptContentData(cdata, ContentModel.PROP_CONTENT);
            getProperties().put(ContentModel.PROP_CONTENT.toString(), contentData);
        }
        contentData.setContent(content);
    }
    
    public void jsSet_content(String content)
    {
        setContent(content);
    }
    
    /**
     * @return For a content document, this method returns the URL to the content stream for the default content
     *         property (@see ContentModel.PROP_CONTENT)
     *         <p>
     *         For a container node, this method return the URL to browse to the folder in the web-client
     */
    public String getUrl()
    {
        if (getIsDocument() == true)
        {
            try
            {
                return MessageFormat.format(CONTENT_DEFAULT_URL, new Object[] { nodeRef.getStoreRef().getProtocol(),
                        nodeRef.getStoreRef().getIdentifier(), nodeRef.getId(),
                        StringUtils.replace(URLEncoder.encode(getName(), "UTF-8"), "+", "%20") });
            }
            catch (UnsupportedEncodingException err)
            {
                throw new AlfrescoRuntimeException("Failed to encode content URL for node: " + nodeRef, err);
            }
        }
        else
        {
            return MessageFormat.format(FOLDER_BROWSE_URL, new Object[] { nodeRef.getStoreRef().getProtocol(),
                    nodeRef.getStoreRef().getIdentifier(), nodeRef.getId() });
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
        String mimetype = null;
        ScriptContentData content = (ScriptContentData) this.getProperties().get(ContentModel.PROP_CONTENT);
        if (content != null)
        {
            mimetype = content.getMimetype();
        }
        
        return mimetype;
    }
    
    public String jsGet_mimetype()
    {
        return getMimetype();
    }
    
    /**
     * Set the mimetype encoding for the content attached to the node from the default content property
     * (@see ContentModel.PROP_CONTENT)
     * 
     * @param mimetype   Mimetype to set
     */
    public void setMimetype(String mimetype)
    {
        ScriptContentData content = (ScriptContentData) this.getProperties().get(ContentModel.PROP_CONTENT);
        if (content != null)
        {
            content.setMimetype(mimetype);
        }
    }
    
    public void jsSet_mimetype(String mimetype)
    {
        setMimetype(mimetype);
    }
    
    /**
     * @return The size in bytes of the content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public long getSize()
    {
        long size = 0;
        ScriptContentData content = (ScriptContentData) this.getProperties().get(ContentModel.PROP_CONTENT);
        if (content != null)
        {
            size = content.getSize();
        }
        
        return size;
    }
    
    public long jsGet_size()
    {
        return getSize();
    }
    
    
    // ------------------------------------------------------------------------------
    // Security API
    
    /**
     * @return true if the node inherits permissions from the parent node, false otherwise
     */
    public boolean inheritsPermissions()
    {
        return this.services.getPermissionService().getInheritParentPermissions(this.nodeRef);
    }
    
    /**
     * Set whether this node should inherit permissions from the parent node.
     * 
     * @param inherit True to inherit parent permissions, false otherwise.
     */
    public void setInheritsPermissions(boolean inherit)
    {
        this.services.getPermissionService().setInheritParentPermissions(this.nodeRef, inherit);
    }
    
    /**
     * Apply a permission for ALL users to the node.
     * 
     * @param permission Permission to apply
     * @see org.alfresco.service.cmr.security.PermissionService
     */
    public void setPermission(String permission)
    {
        ParameterCheck.mandatoryString("Permission Name", permission);
        this.services.getPermissionService().setPermission(this.nodeRef, PermissionService.ALL_AUTHORITIES, permission,
                true);
    }
    
    /**
     * Apply a permission for the specified authority (e.g. username or group) to the node.
     * 
     * @param permission Permission to apply @see org.alfresco.service.cmr.security.PermissionService
     * @param authority Authority (generally a username or group name) to apply the permission for
     */
    public void setPermission(String permission, String authority)
    {
        ParameterCheck.mandatoryString("Permission Name", permission);
        ParameterCheck.mandatoryString("Authority", authority);
        this.services.getPermissionService().setPermission(this.nodeRef, authority, permission, true);
    }
    
    /**
     * Remove a permission for ALL user from the node.
     * 
     * @param permission Permission to remove @see org.alfresco.service.cmr.security.PermissionService
     */
    public void removePermission(String permission)
    {
        ParameterCheck.mandatoryString("Permission Name", permission);
        this.services.getPermissionService()
            .deletePermission(this.nodeRef, PermissionService.ALL_AUTHORITIES, permission);
    }
    
    /**
     * Remove a permission for the specified authority (e.g. username or group) from the node.
     * 
     * @param permission Permission to remove @see org.alfresco.service.cmr.security.PermissionService
     * @param authority  Authority (generally a username or group name) to apply the permission for
     */
    public void removePermission(String permission, String authority)
    {
        ParameterCheck.mandatoryString("Permission Name", permission);
        ParameterCheck.mandatoryString("Authority", authority);
        this.services.getPermissionService().deletePermission(this.nodeRef, authority, permission);
    }
    
    
    // ------------------------------------------------------------------------------
    // Ownership API
    
    /**
     * Set the owner of the node
     */
    public void setOwner(String userId)
    {
        this.services.getOwnableService().setOwner(this.nodeRef, userId);
    }
    
    /**
     * Take ownership of the node.
     */
    public void takeOwnership()
    {
        this.services.getOwnableService().takeOwnership(this.nodeRef);
    }
    
    /**
     * Get the owner of the node.
     * 
     * @return
     */
    public String getOwner()
    {
        return this.services.getOwnableService().getOwner(this.nodeRef);
    }
    
    /**
     * Make owner available as a property.
     * 
     * @return
     */
    public String jsGet_owner()
    {
        return getOwner();
    }
    
    
    // ------------------------------------------------------------------------------
    // Create and Modify API
    
    /**
     * Persist the properties of this Node.
     */
    public void save()
    {
        // persist properties back to the node in the DB
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(getProperties().size());
        for (String key : this.properties.keySet())
        {
            Serializable value = (Serializable) this.properties.get(key);
            
            // perform the conversion from script wrapper object to repo serializable values
            value = getValueConverter().convertValueForRepo(value);
            
            props.put(createQName(key), value);
        }
        this.nodeService.setProperties(this.nodeRef, props);
    }
    
    /**
     * Re-sets the type of the node. Can be called in order specialise a node to a sub-type. This should be used
     * with caution since calling it changes the type of the node and thus* implies a different set of aspects,
     * properties and associations. It is the responsibility of the caller to ensure that the node is in a
     * approriate state after changing the type.
     * 
     * @param type Type to specialize the node
     * 
     * @return true if successful, false otherwise
     */
    public boolean specializeType(String type)
    {
        ParameterCheck.mandatoryString("Type", type);
        
        QName qnameType = createQName(type);
        
        // Ensure that we are performing a specialise
        if (getType().equals(qnameType) == false &&
                this.services.getDictionaryService().isSubClass(qnameType, getType()) == true)
        {
            // Specialise the type of the node
            this.nodeService.setType(this.nodeRef, qnameType);
            this.type = qnameType;
            
            return true;
        }
        return false;
    }
    
    /**
     * Create a new File (cm:content) node as a child of this node.
     * <p>
     * Once created the file should have content set using the <code>content</code> property.
     * 
     * @param name Name of the file to create
     * 
     * @return Newly created Node or null if failed to create.
     */
    public Node createFile(String name)
    {
        Node node = null;
        
        if (name != null && name.length() != 0)
        {
            FileInfo fileInfo = this.services.getFileFolderService().create(this.nodeRef, name,
                    ContentModel.TYPE_CONTENT);
            node = newInstance(fileInfo.getNodeRef(), this.services, this.scope);
        }
        
        return node;
    }
    
    /**
     * Create a new folder (cm:folder) node as a child of this node.
     * 
     * @param name Name of the folder to create
     * 
     * @return Newly created Node or null if failed to create.
     */
    public Node createFolder(String name)
    {
        Node node = null;
        
        if (name != null && name.length() != 0)
        {
            FileInfo fileInfo = this.services.getFileFolderService().create(this.nodeRef, name,
                    ContentModel.TYPE_FOLDER);
            node = newInstance(fileInfo.getNodeRef(), this.services, this.scope);
        }
        
        return node;
    }
    
    /**
     * Create a new Node of the specified type as a child of this node.
     * 
     * @param name Name of the node to create
     * @param type QName type (can either be fully qualified or short form such as 'cm:content')
     * 
     * @return Newly created Node or null if failed to create.
     */
    public Node createNode(String name, String type)
    {
        Node node = null;
        
        if (name != null && name.length() != 0 && type != null && type.length() != 0)
        {
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
            props.put(ContentModel.PROP_NAME, name);
            ChildAssociationRef childAssocRef = this.nodeService.createNode(this.nodeRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.ALFRESCO_URI, QName.createValidLocalName(name)),
                    createQName(type), props);
            node = newInstance(childAssocRef.getChildRef(), this.services, this.scope);
        }
        
        return node;
    }
    
    /**
     * Remove this node. Any references to this Node or its NodeRef should be discarded!
     */
    public boolean remove()
    {
        boolean success = false;
        
        if (nodeService.exists(this.nodeRef))
        {
            this.nodeService.deleteNode(this.nodeRef);
            success = true;
        }
        
        reset();
        
        return success;
    }
    
    /**
     * Copy this Node to a new parent destination. Note that children of the source Node are not copied.
     * 
     * @param destination   Node
     * 
     * @return The newly copied Node instance or null if failed to copy.
     */
    public Node copy(Node destination)
    {
        return copy(destination, false);
    }
    
    /**
     * Copy this Node and potentially all child nodes to a new parent destination.
     * 
     * @param destination   Node
     * @param deepCopy      True for a deep copy, false otherwise.
     * 
     * @return The newly copied Node instance or null if failed to copy.
     */
    public Node copy(Node destination, boolean deepCopy)
    {
        ParameterCheck.mandatory("Destination Node", destination);
        
        Node copy = null;
        
        if (destination.getNodeRef().getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE))
        {
            NodeRef copyRef = this.services.getCopyService().copyAndRename(this.nodeRef, destination.getNodeRef(),
                    ContentModel.ASSOC_CONTAINS, getPrimaryParentAssoc().getQName(), deepCopy);
            copy = newInstance(copyRef, this.services, this.scope);
        }
        else
        {
            // NOTE: the deepCopy flag is not respected for this copy mechanism
            copy = getCrossRepositoryCopyHelper().copy(this, destination, getName());
        }
        
        return copy;
    }
    
    /**
     * Move this Node to a new parent destination.
     * 
     * @param destination   Node
     * 
     * @return true on successful move, false on failure to move.
     */
    public boolean move(Node destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);
        
        this.primaryParentAssoc = this.nodeService.moveNode(this.nodeRef, destination.getNodeRef(),
                ContentModel.ASSOC_CONTAINS, getPrimaryParentAssoc().getQName());
        
        // reset cached values
        reset();
        
        return true;
    }
    
    /**
     * Add an aspect to the Node. As no properties are provided in this call, it can only be used to add aspects that do not require any mandatory properties.
     * 
     * @param type    Type name of the aspect to add
     * 
     * @return true if the aspect was added successfully, false if an error occured.
     */
    public boolean addAspect(String type)
    {
        return addAspect(type, null);
    }
    
    /**
     * Add an aspect to the Node.
     * 
     * @param type    Type name of the aspect to add
     * @param props   ScriptableObject (generally an assocative array) providing the named properties for the aspect
     *                - any mandatory properties for the aspect must be provided!
     *                
     * @return true if the aspect was added successfully, false if an error occured.
     */
    public boolean addAspect(String type, Object props)
    {
        ParameterCheck.mandatoryString("Aspect Type", type);
        
        Map<QName, Serializable> aspectProps = null;
        if (props instanceof ScriptableObject)
        {
            ScriptableObject properties = (ScriptableObject) props;
            
            // we need to get all the keys to the properties provided
            // and convert them to a Map of QName to Serializable objects
            Object[] propIds = properties.getIds();
            aspectProps = new HashMap<QName, Serializable>(propIds.length);
            for (int i = 0; i < propIds.length; i++)
            {
                // work on each key in turn
                Object propId = propIds[i];
                
                // we are only interested in keys that are formed of Strings i.e. QName.toString()
                if (propId instanceof String)
                {
                    // get the value out for the specified key - make sure it is Serializable
                    Object value = properties.get((String) propId, properties);
                    value = getValueConverter().convertValueForRepo((Serializable) value);
                    aspectProps.put(createQName((String) propId), (Serializable) value);
                }
            }
        }
        QName aspectQName = createQName(type);
        this.nodeService.addAspect(this.nodeRef, aspectQName, aspectProps);
        
        // reset the relevant cached node members
        reset();
        
        return true;
    }
    
    /**
     * Remove aspect from the node.
     * 
     * @param type  the aspect type
     * 
     * @return      true if successful, false otherwise
     */
    public boolean removeAspect(String type)
    {
        ParameterCheck.mandatoryString("Aspect Type", type);
        
        QName aspectQName = createQName(type);
        this.nodeService.removeAspect(this.nodeRef, aspectQName);
        
        // reset the relevant cached node members
        reset();
        
        return true;
    }
    
    
    // ------------------------------------------------------------------------------
    // Checkout/Checkin Services
    
    /**
     * Perform a check-out of this document into the current parent space.
     * 
     * @return the working copy Node for the checked out document
     */
    public Node checkout()
    {
        NodeRef workingCopyRef = this.services.getCheckOutCheckInService().checkout(this.nodeRef);
        Node workingCopy = newInstance(workingCopyRef, this.services, this.scope);
        
        // reset the aspect and properties as checking out a document causes changes
        this.properties = null;
        this.aspects = null;
        
        return workingCopy;
    }
    
    /**
     * Perform a check-out of this document into the specified destination space.
     * 
     * @param destination
     *            Destination for the checked out document working copy Node.
     * @return the working copy Node for the checked out document
     */
    public Node checkout(Node destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);
        
        ChildAssociationRef childAssocRef = this.nodeService.getPrimaryParent(destination.getNodeRef());
        NodeRef workingCopyRef = this.services.getCheckOutCheckInService().checkout(this.nodeRef,
                destination.getNodeRef(), ContentModel.ASSOC_CONTAINS, childAssocRef.getQName());
        Node workingCopy = newInstance(workingCopyRef, this.services, this.scope);
        
        // reset the aspect and properties as checking out a document causes changes
        this.properties = null;
        this.aspects = null;
        
        return workingCopy;
    }
    
    /**
     * Check-in a working copy document. The current state of the working copy is copied to the original node,
     * this will include any content updated in the working node. Note that this method can only be called on a
     * working copy Node.
     * 
     * @return the original Node that was checked out.
     */
    public Node checkin()
    {
        return checkin("", false);
    }
    
    /**
     * Check-in a working copy document. The current state of the working copy is copied to the original node,
     * this will include any content updated in the working node. Note that this method can only be called on a
     * working copy Node.
     * 
     * @param history    Version history note
     * 
     * @return the original Node that was checked out.
     */
    public Node checkin(String history)
    {
        return checkin(history, false);
    }
    
    /**
     * Check-in a working copy document. The current state of the working copy is copied to the original node,
     * this will include any content updated in the working node. Note that this method can only be called on a
     * working copy Node.
     * 
     * @param history       Version history note
     * @param majorVersion  True to save as a major version increment, false for minor version.
     * 
     * @return the original Node that was checked out.
     */
    public Node checkin(String history, boolean majorVersion)
    {
        Map<String, Serializable> props = new HashMap<String, Serializable>(2, 1.0f);
        props.put(Version.PROP_DESCRIPTION, history);
        props.put(VersionModel.PROP_VERSION_TYPE, majorVersion ? VersionType.MAJOR : VersionType.MINOR);
        NodeRef original = this.services.getCheckOutCheckInService().checkin(this.nodeRef, props);
        return newInstance(original, this.services, this.scope);
    }
    
    /**
     * Cancel the check-out of a working copy document. The working copy will be deleted and any changes made to it
     * are lost. Note that this method can only be called on a working copy Node. The reference to this working copy
     * Node should be discarded.
     * 
     * @return the original Node that was checked out.
     */
    public Node cancelCheckout()
    {
        NodeRef original = this.services.getCheckOutCheckInService().cancelCheckout(this.nodeRef);
        return newInstance(original, this.services, this.scope);
    }
    
    
    // ------------------------------------------------------------------------------
    // Transformation and Rendering API
    
    /**
     * Transform a document to a new document mimetype format. A copy of the document is made and the extension
     * changed to match the new mimetype, then the transformation isapplied.
     * 
     * @param mimetype   Mimetype destination for the transformation
     * 
     * @return Node representing the newly transformed document.
     */
    public Node transformDocument(String mimetype)
    {
        return transformDocument(mimetype, getPrimaryParentAssoc().getParentRef());
    }
    
    /**
     * Transform a document to a new document mimetype format. A copy of the document is made in the specified
     * destination folder and the extension changed to match the new mimetype, then then transformation is applied.
     * 
     * @param mimetype      Mimetype destination for the transformation
     * @param destination   Destination folder location
     * 
     * @return Node representing the newly transformed document.
     */
    public Node transformDocument(String mimetype, Node destination)
    {
        return transformDocument(mimetype, destination.getNodeRef());
    }
    
    private Node transformDocument(String mimetype, NodeRef destination)
    {
        ParameterCheck.mandatoryString("Mimetype", mimetype);
        ParameterCheck.mandatory("Destination Node", destination);
        
        // the delegate definition for transforming a document
        Transformer transformer = new Transformer()
        {
            public Node transform(ContentService contentService, NodeRef nodeRef, ContentReader reader,
                    ContentWriter writer)
            {
                Node transformedNode = null;
                if (contentService.isTransformable(reader, writer))
                {
                    contentService.transform(reader, writer);
                    transformedNode = newInstance(nodeRef, services, scope);
                }
                return transformedNode;
            }
        };
        
        return transformNode(transformer, mimetype, destination);
    }
    
    /**
     * Generic method to transform Node content from one mimetype to another.
     * 
     * @param transformer   The Transformer delegate supplying the transformation logic
     * @param mimetype      Mimetype of the destination content
     * @param destination   Destination folder location for the resulting document
     * 
     * @return Node representing the transformed content - or null if the transform failed
     */
    private Node transformNode(Transformer transformer, String mimetype, NodeRef destination)
    {
        Node transformedNode = null;
        
        // get the content reader
        ContentService contentService = this.services.getContentService();
        ContentReader reader = contentService.getReader(this.nodeRef, ContentModel.PROP_CONTENT);
        
        // only perform the transformation if some content is available
        if (reader != null)
        {
            // Copy the content node to a new node
            String copyName = TransformActionExecuter.transformName(this.services.getMimetypeService(), getName(),
                    mimetype, true);
            NodeRef copyNodeRef = this.services.getCopyService().copy(this.nodeRef, destination,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(ContentModel.PROP_CONTENT.getNamespaceURI(), QName.createValidLocalName(copyName)),
                    false);
            
            // modify the name of the copy to reflect the new mimetype
            this.nodeService.setProperty(copyNodeRef, ContentModel.PROP_NAME, copyName);
            
            // get the writer and set it up
            ContentWriter writer = contentService.getWriter(copyNodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(mimetype); // new mimetype
            writer.setEncoding(reader.getEncoding()); // original encoding
            
            // Try and transform the content using the supplied delegate
            transformedNode = transformer.transform(contentService, copyNodeRef, reader, writer);
        }
        
        return transformedNode;
    }
    
    /**
     * Transform an image to a new image format. A copy of the image document is made and the extension changed to
     * match the new mimetype, then the transformation is applied.
     * 
     * @param mimetype   Mimetype destination for the transformation
     * 
     * @return Node representing the newly transformed image.
     */
    public Node transformImage(String mimetype)
    {
        return transformImage(mimetype, null, getPrimaryParentAssoc().getParentRef());
    }
    
    /**
     * Transform an image to a new image format. A copy of the image document is made and the extension changed to
     * match the new mimetype, then the transformation is applied.
     * 
     * @param mimetype   Mimetype destination for the transformation
     * @param options    Image convert command options
     * 
     * @return Node representing the newly transformed image.
     */
    public Node transformImage(String mimetype, String options)
    {
        return transformImage(mimetype, options, getPrimaryParentAssoc().getParentRef());
    }
    
    /**
     * Transform an image to a new image mimetype format. A copy of the image document is made in the specified
     * destination folder and the extension changed to match the newmimetype, then then transformation is applied.
     * 
     * @param mimetype      Mimetype destination for the transformation
     * @param destination   Destination folder location
     * 
     * @return Node representing the newly transformed image.
     */
    public Node transformImage(String mimetype, Node destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);
        return transformImage(mimetype, null, destination.getNodeRef());
    }
    
    /**
     * Transform an image to a new image mimetype format. A copy of the image document is made in the specified
     * destination folder and the extension changed to match the new
     * mimetype, then then transformation is applied.
     * 
     * @param mimetype      Mimetype destination for the transformation
     * @param options       Image convert command options
     * @param destination   Destination folder location
     * 
     * @return Node representing the newly transformed image.
     */
    public Node transformImage(String mimetype, String options, Node destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);
        return transformImage(mimetype, options, destination.getNodeRef());
    }
    
    private Node transformImage(String mimetype, final String options, NodeRef destination)
    {
        ParameterCheck.mandatoryString("Mimetype", mimetype);
        
        // the delegate definition for transforming an image
        Transformer transformer = new Transformer()
        {
            public Node transform(ContentService contentService, NodeRef nodeRef, ContentReader reader,
                    ContentWriter writer)
            {
                Map<String, Object> opts = new HashMap<String, Object>(1);
                opts.put(ImageMagickContentTransformer.KEY_OPTIONS, options != null ? options : "");
                contentService.getImageTransformer().transform(reader, writer, opts);

                return newInstance(nodeRef, services, scope);
            }
        };
        
        return transformNode(transformer, mimetype, destination);
    }
    
    /**
     * Process a FreeMarker Template against the current node.
     * 
     * @param template      Node of the template to execute
     * 
     * @return output of the template execution
     */
    public String processTemplate(Node template)
    {
        ParameterCheck.mandatory("Template Node", template);
        return processTemplate(template.getContent(), null, null);
    }
    
    /**
     * Process a FreeMarker Template against the current node.
     * 
     * @param template   Node of the template to execute
     * @param args       Scriptable object (generally an associative array) containing the name/value pairs of
     *                   arguments to be passed to the template
     *                   
     * @return output of the template execution
     */
    public String processTemplate(Node template, Object args)
    {
        ParameterCheck.mandatory("Template Node", template);
        return processTemplate(template.getContent(), null, (ScriptableObject) args);
    }
    
    /**
     * Process a FreeMarker Template against the current node.
     * 
     * @param template   The template to execute
     * 
     * @return output of the template execution
     */
    public String processTemplate(String template)
    {
        ParameterCheck.mandatoryString("Template", template);
        return processTemplate(template, null, null);
    }
    
    /**
     * Process a FreeMarker Template against the current node.
     * 
     * @param template   The template to execute
     * @param args       Scriptable object (generally an associative array) containing the name/value pairs of
     *                   arguments to be passed to the template
     *                   
     * @return output of the template execution
     */
    public String processTemplate(String template, Object args)
    {
        ParameterCheck.mandatoryString("Template", template);
        return processTemplate(template, null, (ScriptableObject) args);
    }
    
    private String processTemplate(String template, NodeRef templateRef, ScriptableObject args)
    {
        // build default model for the template processing
        Map<String, Object> model = FreeMarkerProcessor.buildDefaultModel(services, ((Node) ((Wrapper) scope.get(
                "person", scope)).unwrap()).getNodeRef(), ((Node) ((Wrapper) scope.get("companyhome", scope)).unwrap())
                .getNodeRef(), ((Node) ((Wrapper) scope.get("userhome", scope)).unwrap()).getNodeRef(), templateRef, null);
        
        // add the current node as either the document/space as appropriate
        if (this.getIsDocument())
        {
            model.put("document", new TemplateNode(this.nodeRef, this.services, null));
            model.put("space", new TemplateNode(getPrimaryParentAssoc().getParentRef(), this.services, null));
        }
        else
        {
            model.put("space", new TemplateNode(this.nodeRef, this.services, null));
        }
        
        // add the supplied args to the 'args' root object
        if (args != null)
        {
            // we need to get all the keys to the properties provided
            // and convert them to a Map of QName to Serializable objects
            Object[] propIds = args.getIds();
            Map<String, String> templateArgs = new HashMap<String, String>(propIds.length);
            for (int i = 0; i < propIds.length; i++)
            {
                // work on each key in turn
                Object propId = propIds[i];
                
                // we are only interested in keys that are formed of Strings i.e. QName.toString()
                if (propId instanceof String)
                {
                    // get the value out for the specified key - make sure it is Serializable
                    Object value = args.get((String) propId, args);
                    value = getValueConverter().convertValueForRepo((Serializable) value);
                    if (value != null)
                    {
                        templateArgs.put((String) propId, value.toString());
                    }
                }
            }
            // add the args to the model as the 'args' root object
            model.put("args", templateArgs);
        }
        
        // execute template!
        // TODO: check that script modified nodes are reflected...
        return this.services.getTemplateService().processTemplateString(null, template, model);
    }
    
    
    // ------------------------------------------------------------------------------
    // Helper methods
    
    /**
     * Override Object.toString() to provide useful debug output
     */
    public String toString()
    {
        if (this.nodeService.exists(nodeRef))
        {
            // TODO: DC: Allow debug output of property values - for now it's disabled as this could potentially
            // follow a large network of nodes. Unfortunately, JBPM issues unprotected debug statements
            // where node.toString is used - will request this is fixed in next release of JBPM.
            return "Node Type: " + getType() + "\nNode Properties: " + this.getProperties().size() +
                    "\nNode Aspects: " + this.getAspects().toString();
        }
        else
        {
            return "Node no longer exists: " + nodeRef;
        }
    }
    
    /**
     * Helper to create a QName from either a fully qualified or short-name QName string
     * 
     * @param s    Fully qualified or short-name QName string
     * 
     * @return QName
     */
    private QName createQName(String s)
    {
        QName qname;
        if (s.indexOf(NAMESPACE_BEGIN) != -1)
        {
            qname = QName.createQName(s);
        }
        else
        {
            qname = QName.createQName(s, this.services.getNamespaceService());
        }
        return qname;
    }
    
    /**
     * Reset the Node cached state
     */
    public void reset()
    {
        this.name = null;
        this.type = null;
        this.properties = null;
        this.aspects = null;
        this.assocs = null;
        this.children = null;
        this.displayPath = null;
        this.isDocument = null;
        this.isContainer = null;
        this.parent = null;
        this.primaryParentAssoc = null;
    }
    
    /**
     * @return helper object to perform cross repository copy of JavaScript Node objects
     */
    protected CrossRepositoryCopy getCrossRepositoryCopyHelper()
    {
        return (CrossRepositoryCopy)this.services.getService(
                QName.createQName("", CrossRepositoryCopy.BEAN_NAME));
    }
    
    /**
     * Return a list or a single Node from executing an xpath against the parent Node.
     * 
     * @param xpath      XPath to execute
     * @param firstOnly  True to return the first result only
     * 
     * @return Node[] can be empty but never null
     */
    private Node[] getChildrenByXPath(String xpath, boolean firstOnly)
    {
        Node[] result = null;
        
        if (xpath.length() != 0)
        {
            if (logger.isDebugEnabled()) logger.debug("Executing xpath: " + xpath);
            
            List<NodeRef> nodes = this.services.getSearchService().selectNodes(this.nodeRef, xpath, null,
                    this.services.getNamespaceService(), false);
            
            // see if we only want the first result
            if (firstOnly == true)
            {
                if (nodes.size() != 0)
                {
                    result = new Node[1];
                    result[0] = newInstance(nodes.get(0), this.services, this.scope);
                }
            }
            // or all the results
            else
            {
                result = new Node[nodes.size()];
                for (int i = 0; i < nodes.size(); i++)
                {
                    NodeRef ref = nodes.get(i);
                    result[i] = newInstance(ref, this.services, this.scope);
                }
            }
        }
        
        return result != null ? result : new Node[0];
    }
    
    
    // ------------------------------------------------------------------------------
    // Value Conversion
    
    /**
     * Gets the node value converter
     * 
     * @return the node value converter
     */
    protected NodeValueConverter getValueConverter()
    {
        if (converter == null)
        {
            converter = createValueConverter();
        }
        return converter;
    }
    
    /**
     * Constructs the node value converter
     * 
     * @return the node value converter
     */
    protected NodeValueConverter createValueConverter()
    {
        return new NodeValueConverter();
    }
    
    // Set support
    
    /**
     * Value converter with knowledge of Node specific value types
     */
    public class NodeValueConverter extends ValueConverter
    {
        /**
         * Convert an object from any repository serialized value to a valid script object. This includes converting
         * Collection multi-value properties into JavaScript Array objects.
         * 
         * @param qname      QName of the property value for conversion
         * @param value      Property value
         * 
         * @return Value safe for scripting usage
         */
        public Serializable convertValueForScript(QName qname, Serializable value)
        {
            return convertValueForScript(services, scope, qname, value);
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.alfresco.repo.jscript.ValueConverter#convertValueForScript(org.alfresco.service.ServiceRegistry,
         *      org.mozilla.javascript.Scriptable, org.alfresco.service.namespace.QName, java.io.Serializable)
         */
        @Override
        public Serializable convertValueForScript(ServiceRegistry services, Scriptable scope, QName qname,
                Serializable value)
        {
            if (value instanceof ContentData)
            {
                // ContentData object properties are converted to ScriptContentData objects
                // so the content and other properties of those objects can be accessed
                value = new ScriptContentData((ContentData) value, qname);
            }
            else
            {
                value = super.convertValueForScript(services, scope, qname, value);
            }
            return value;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.alfresco.repo.jscript.ValueConverter#convertValueForRepo(java.io.Serializable)
         */
        @Override
        public Serializable convertValueForRepo(Serializable value)
        {
            if (value instanceof ScriptContentData)
            {
                // convert back to ContentData
                value = ((ScriptContentData) value).contentData;
            }
            else
            {
                value = super.convertValueForRepo(value);
            }
            return value;
        }
    }
    
    
    // ------------------------------------------------------------------------------
    // Inner Classes
    
    /**
     * Inner class wrapping and providing access to a ContentData property
     */
    public class ScriptContentData implements Serializable
    {
        private static final long serialVersionUID = -7819328543933312278L;
        
        /**
         * Constructor
         * 
         * @param contentData      The ContentData object this object wraps
         * @param property         The property the ContentData is attached too
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
        
        public String jsGet_content()
        {
            return getContent();
        }
        
        /**
         * Set the content stream
         * 
         * @param content    Content string to set
         */
        public void setContent(String content)
        {
            ContentService contentService = services.getContentService();
            ContentWriter writer = contentService.getWriter(nodeRef, this.property, true);
            writer.setMimetype(getMimetype()); // use existing mimetype value
            writer.putContent(content);
            
            // update cached variables after putContent()
            this.contentData = (ContentData) services.getNodeService().getProperty(nodeRef, this.property);
        }
        
        public void jsSet_content(String content)
        {
            setContent(content);
        }
        
        /**
         * @return download URL to the content
         */
        public String getUrl()
        {
            try
            {
                return MessageFormat.format(CONTENT_PROP_URL, new Object[] { nodeRef.getStoreRef().getProtocol(),
                        nodeRef.getStoreRef().getIdentifier(), nodeRef.getId(),
                        StringUtils.replace(URLEncoder.encode(getName(), "UTF-8"), "+", "%20"),
                        StringUtils.replace(URLEncoder.encode(property.toString(), "UTF-8"), "+", "%20") });
            }
            catch (UnsupportedEncodingException err)
            {
                throw new AlfrescoRuntimeException("Failed to encode content URL for node: " + nodeRef, err);
            }
        }
        
        public String jsGet_url()
        {
            return getUrl();
        }
        
        public long getSize()
        {
            return contentData.getSize();
        }
        
        public long jsGet_size()
        {
            return getSize();
        }
        
        public String getMimetype()
        {
            return contentData.getMimetype();
        }
        
        public String jsGet_mimetype()
        {
            return getMimetype();
        }
        
        public void setMimetype(String mimetype)
        {
            this.contentData = ContentData.setMimetype(this.contentData, mimetype);
            services.getNodeService().setProperty(nodeRef, this.property, this.contentData);
            
            // update cached variables after putContent()
            this.contentData = (ContentData) services.getNodeService().getProperty(nodeRef, this.property);
        }
        
        public void jsSet_mimetype(String mimetype)
        {
            setMimetype(mimetype);
        }
        
        private ContentData contentData;
        
        private QName property;
    }
    
    /**
     * Interface contract for simple anonymous classes that implement document transformations
     */
    private interface Transformer
    {
        /**
         * Transform the reader to the specified writer
         * 
         * @param contentService   ContentService
         * @param noderef          NodeRef of the destination for the transform
         * @param reader           Source reader
         * @param writer           Destination writer
         * 
         * @return Node representing the transformed entity
         */
        Node transform(ContentService contentService, NodeRef noderef, ContentReader reader, ContentWriter writer);
    }
}