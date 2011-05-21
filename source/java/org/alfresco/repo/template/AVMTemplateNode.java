/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.template;

import java.io.Serializable;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLockingService.LockState;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.URLEncoder;
import org.xml.sax.InputSource;

import freemarker.ext.dom.NodeModel;

/**
 * AVM node class for use by a Template model.
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
@SuppressWarnings("serial")
public class AVMTemplateNode extends BasePermissionsNode implements NamespacePrefixResolverProvider
{
    private static Log logger = LogFactory.getLog(AVMTemplateNode.class);
    
    /** Cached values */
    private NodeRef nodeRef;
    private String name;
    private QName type;
    private String path;
    private int version;
    private boolean deleted;
    private QNameMap<String, Serializable> properties;
    private boolean propsRetrieved = false;
    private AVMTemplateNode parent = null;
    private AVMNodeDescriptor avmRef;
    
    
    // ------------------------------------------------------------------------------
    // Construction 
    
    /**
     * Constructor
     * 
     * @param nodeRef       The NodeRef for the AVM node this wrapper represents
     * @param services      The ServiceRegistry the Node can use to access services
     * @param resolver      Image resolver to use to retrieve icons
     */
    public AVMTemplateNode(NodeRef nodeRef, ServiceRegistry services, TemplateImageResolver resolver)
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
        Pair<Integer, String> pair = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        this.services = services;
        this.imageResolver = resolver;
        init(pair.getFirst(), pair.getSecond(), null);
    }
    
    /**
     * Constructor
     * 
     * @param path          AVM path to the node
     * @param version       Version number for avm path
     * @param services      The ServiceRegistry the Node can use to access services
     * @param resolver      Image resolver to use to retrieve icons
     */
    public AVMTemplateNode(String path, int version, ServiceRegistry services, TemplateImageResolver resolver)
    {
        if (path == null)
        {
            throw new IllegalArgumentException("Path must be supplied.");
        }
        
        if (services == null)
        {
            throw new IllegalArgumentException("The ServiceRegistry must be supplied.");
        }
        
        this.nodeRef = AVMNodeConverter.ToNodeRef(version, path);
        this.services = services;
        this.imageResolver = resolver;
        init(version, path, null);
    }
    
    /**
     * Constructor
     * 
     * @param descriptor    AVMNodeDescriptior
     * @param services      
     * @param resolver
     */
    public AVMTemplateNode(AVMNodeDescriptor descriptor, ServiceRegistry services, TemplateImageResolver resolver)
    {
        if (descriptor == null)
        {
            throw new IllegalArgumentException("AVMNodeDescriptor must be supplied.");
        }
        
        if (services == null)
        {
            throw new IllegalArgumentException("The ServiceRegistry must be supplied.");
        }
        
        this.version = -1;
        this.path = descriptor.getPath();
        this.nodeRef = AVMNodeConverter.ToNodeRef(this.version, this.path);
        this.services = services;
        this.imageResolver = resolver;
        init(this.version, this.path, descriptor);
    }
    
    private void init(int version, String path, AVMNodeDescriptor descriptor)
    {
        this.version = version;
        this.path = path;
        this.properties = new QNameMap<String, Serializable>(this);
        if (descriptor == null)
        {
            descriptor = this.services.getAVMService().lookup(version, path, true);
            if (descriptor == null)
            {
                throw new IllegalArgumentException("Invalid node specified: " + nodeRef.toString());
            }
        }
        this.avmRef = descriptor;
        this.deleted = descriptor.isDeleted();
    }
    
    
    // ------------------------------------------------------------------------------
    // AVM Node API
    
    /**
     * @return ID for the AVM path - the path.
     */
    public String getId()
    {
        return this.path;
    }
    
    /**
     * @return the path for this AVM node.
     */
    public String getPath()
    {
        return this.path;
    }
    
    /**
     * @return the version part of the AVM path.
     */
    public int getVersion()
    {
        return this.version;
    }

    /**
     * @return file/folder name of the AVM path.
     */
    public String getName()
    {
        if (this.name == null)
        {
            this.name = AVMNodeConverter.SplitBase(this.path)[1];
        }
        return this.name;
    }
    
    /**
     * @return AVM path to the parent node
     */
    public String getParentPath()
    {
        return AVMNodeConverter.SplitBase(this.path)[0];
    }

    /**
     * @see org.alfresco.repo.template.TemplateNodeRef#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }
    
    /**
     * @see org.alfresco.repo.template.TemplateNodeRef#getType()
     */
    public QName getType()
    {
        if (this.type == null)
        {
            if (this.deleted == false)
            {
                this.type = this.services.getNodeService().getType(this.nodeRef);
            }
            else
            {
                this.type = this.avmRef.isDeletedDirectory() ? WCMModel.TYPE_AVM_FOLDER : WCMModel.TYPE_AVM_CONTENT;
            }
        }

        return type;
    }
    
    /**
     * @return true if the item is a deleted node, false otherwise
     */
    public boolean getIsDeleted()
    {
        return this.avmRef.isDeleted();
    }
    
    /**
     * @return true if the node is currently locked
     */
    public boolean getIsLocked()
    {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        LockState lockStatus = this.services.getAVMLockingService().getLockState(
                getWebProject(), path.substring(path.indexOf("/")), currentUser);
        return lockStatus != LockState.NO_LOCK;
    }
    
    /**
     * @return true if this node is locked and the current user is the lock owner
     */
    public boolean getIsLockOwner()
    {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        LockState lockStatus = this.services.getAVMLockingService().getLockState(
                getWebProject(), path.substring(path.indexOf("/")), currentUser);
        return lockStatus == LockState.LOCK_OWNER;
    }
    
    /**
     * @return true if this user can perform operations on the node when locked.
     *         This is true if the item is either unlocked, or locked and the current user is the lock owner,
     *         or locked and the current user has Content Manager role in the associated web project.
     */
    public boolean getHasLockAccess()
    {
        return this.services.getAVMLockingService().hasAccess(
                getWebProject(), path, this.services.getAuthenticationService().getCurrentUserName());
    }

    
    // ------------------------------------------------------------------------------
    // TemplateProperties API
    
    /**
     * @return the immediate parent in the node path (null if root of store)
     */
    public TemplateProperties getParent()
    {
        if (this.parent == null)
        {
            String parentPath = this.getParentPath();
            if (parentPath != null)
            {
                this.parent = new AVMTemplateNode(parentPath, this.version, this.services, this.imageResolver);
            }
        }
        return this.parent;
    }
    
    /**
     * @return true if this Node is a container (i.e. a folder)
     */
    @Override
    public boolean getIsContainer()
    {
        return this.avmRef.isDirectory() || this.avmRef.isDeletedDirectory();
    }
    
    /**
     * @return true if this Node is a document (i.e. a file)
     */
    @Override
    public boolean getIsDocument()
    {
        return this.avmRef.isFile() || this.avmRef.isDeletedFile();
    }
    
    /**
     * @see org.alfresco.repo.template.TemplateProperties#getChildren()
     */
    public List<TemplateProperties> getChildren()
    {
        if (this.children == null)
        {
            // use the NodeService so appropriate permission checks are performed
            List<ChildAssociationRef> childRefs = this.services.getNodeService().getChildAssocs(this.nodeRef);
            this.children = new ArrayList<TemplateProperties>(childRefs.size());
            for (ChildAssociationRef ref : childRefs)
            {
                // create our Node representation from the NodeRef
                AVMTemplateNode child = new AVMTemplateNode(ref.getChildRef(), this.services, this.imageResolver);
                this.children.add(child);
            }
        }
        
        return this.children;
    }

    /**
     * @see org.alfresco.repo.template.TemplateProperties#getProperties()
     */
    @SuppressWarnings("unchecked")
    public Map<String, Serializable> getProperties()
    {
        if (!this.propsRetrieved)
        {
            if (!this.deleted)
            {
                Map<QName, PropertyValue> props = this.services.getAVMService().getNodeProperties(this.version, this.path);
                for (QName qname: props.keySet())
                {
                    PropertyDefinition propertyDefinition = services.getDictionaryService().getProperty(qname);
                    QName currentPropertyType = DataTypeDefinition.ANY;
                    if (null != propertyDefinition)
                    {
                        currentPropertyType = propertyDefinition.getDataType().getName();
                    }
                    Serializable propValue = props.get(qname).getValue(currentPropertyType);
                    if (propValue instanceof NodeRef)
                    {
                        // NodeRef object properties are converted to new TemplateNode objects
                        // so they can be used as objects within a template
                        NodeRef nodeRef = (NodeRef)propValue;
                        if (StoreRef.PROTOCOL_AVM.equals(nodeRef.getStoreRef().getProtocol()))
                        {
                            propValue = new AVMTemplateNode(nodeRef, this.services, this.imageResolver);
                        }
                        else
                        {
                            propValue = new TemplateNode(nodeRef, this.services, this.imageResolver);
                        }
                    }
                    else if (propValue instanceof ContentData)
                    {
                        // ContentData object properties are converted to TemplateContentData objects
                        // so the content and other properties of those objects can be accessed
                        propValue = new TemplateContentData((ContentData)propValue, qname);
                    }
                    this.properties.put(qname.toString(), propValue);
                }
            }
            
            // AVM node properties not available in usual getProperties() call
            this.properties.put("name", this.avmRef.getName());
            this.properties.put("created", new Date(this.avmRef.getCreateDate()));
            this.properties.put("modified", new Date(this.avmRef.getModDate()));
            this.properties.put("creator", this.avmRef.getCreator());
            this.properties.put("modifier", this.avmRef.getLastModifier());
            
            this.propsRetrieved = true;
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
            this.aspects = this.services.getAVMService().getAspects(this.version, this.path);
        }
        
        return this.aspects;
    }
    
    
    // ------------------------------------------------------------------------------
    // Content API 
    
    /**
     * @return the content String for this node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getContent()
    {
        ContentReader reader = this.services.getAVMService().getContentReader(this.version, this.path);
        
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
            return MessageFormat.format(CONTENT_GET_URL, new Object[] {
                    getNodeRef().getStoreRef().getProtocol(),
                    getNodeRef().getStoreRef().getIdentifier(),
                    getNodeRef().getId(),
                    URLEncoder.encode(getName()) } );
        }
        else
        {
            return MessageFormat.format(FOLDER_BROWSE_URL, new Object[] {
                    getNodeRef().getStoreRef().getProtocol(),
                    getNodeRef().getStoreRef().getIdentifier(),
                    getNodeRef().getId() } );
        }
    }
    
    /**
     * @return For a content document, this method returns the download URL to the content for
     *         the default content property (@see ContentModel.PROP_CONTENT)
     *         <p>
     *         For a container node, this method returns an empty string
     */
    public String getDownloadUrl()
    {
        if (getIsDocument() == true)
        {
            return MessageFormat.format(CONTENT_DOWNLOAD_URL, new Object[] {
                    getNodeRef().getStoreRef().getProtocol(),
                    getNodeRef().getStoreRef().getIdentifier(),
                    getNodeRef().getId(),
                    URLEncoder.encode(getName()) } );
        }
        else
        {
            return "";
        }
    }
    
    /**
     * @return The mimetype encoding for content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getMimetype()
    {
        if (getIsContainer())
        {
            return null;
        }
        return this.services.getAVMService().getContentDataForRead(this.avmRef).getMimetype();
    }
    
    /**
     * @return The display label of the mimetype encoding for content attached to the node from the default
     *         content property (@see ContentModel.PROP_CONTENT)
     */
    public String getDisplayMimetype()
    {
        if (getIsContainer())
        {
            return null;
        }
        final String mimetype = this.services.getAVMService().getContentDataForRead(this.avmRef).getMimetype();
        return services.getMimetypeService().getDisplaysByMimetype().get(mimetype);
    }
    
    /**
     * @return The character encoding for content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public String getEncoding()
    {
        if (getIsContainer())
        {
            return null;
        }
        return this.services.getAVMService().getContentDataForRead(this.avmRef).getEncoding();
    }
    
    /**
     * @return The size in bytes of the content attached to the node from the default content property
     *         (@see ContentModel.PROP_CONTENT)
     */
    public long getSize()
    {
        if (getIsContainer())
        {
            return -1;
        }
        return this.services.getAVMService().getContentDataForRead(this.avmRef).getSize();
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
    @Override
    public String getDisplayPath()
    {
        return this.path;
    }
    
    
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return this.services.getNamespaceService();
    }
    
    
    // ------------------------------------------------------------------------------
    // Private helpers
    
    /**
     * @return the WebProject identifier for the current path
     */
    private String getWebProject()
    {
        String webProject = this.path.substring(0, this.path.indexOf(':'));
        int index = webProject.indexOf("--");
        if (index != -1)
        {
            webProject = webProject.substring(0, index);
        }
        return webProject;
    }
}
